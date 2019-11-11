package org.bitcorej.chain.zcash;

import com.rfksystems.blake2b.Blake2b;
import org.bitcoinj.core.*;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcorej.chain.KeyPair;
import org.bitcorej.chain.bitcoin.BitcoinStateProvider;
import org.bitcorej.core.Network;
import org.bitcorej.utils.BitUtils;
import org.bitcorej.utils.ByteUtil;
import org.bitcorej.utils.NumericUtil;
import org.json.JSONObject;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.bitcoinj.script.ScriptOpCodes.*;

public class ZcashStateProvider extends BitcoinStateProvider {

    private static final byte[] ZERO = NumericUtil.hexToBytes("0000000000000000000000000000000000000000000000000000000000000000");

    private static final long VERSION_GROUP_ID = 2301567109L; // 0x03C48270 (63210096) for overwinter and 0x892F2085 (2301567109) for sapling

    protected long consensusBranchId;

    public ZcashStateProvider(Network network) {
        super(network);
        switch (network) {
            case MAIN:
                super.params = ZcashNetParams.get();
                break;
            case TEST:
                super.params = ZcashTestNetParams.get();
                break;
            default:
                super.params = ZcashNetParams.get();
                break;
        }

        super.network = network;
        consensusBranchId = 1991772603l;
    }

    public String generateP2PKHScript(String address) {
        byte[] versionAndDataBytes = Base58.decodeChecked(address);
        byte[] bytes = new byte[versionAndDataBytes.length - 2];
        System.arraycopy(versionAndDataBytes, 2, bytes, 0, versionAndDataBytes.length - 2);
        Script script = new ScriptBuilder()
                .op(OP_DUP)
                .op(OP_HASH160)
                .data(bytes)
                .op(OP_EQUALVERIFY)
                .op(OP_CHECKSIG)
                .build();
        return NumericUtil.bytesToHex(script.getProgram());
    }

    @Override
    protected String selectPrivateKeys(Script script, List<String> keys) {
        for (int i = 0; i < keys.size(); i++) {
            if (NumericUtil.bytesToHex(script.getProgram()).equals(generateP2PKHScript(this.generateKeyPair(keys.get(i)).getPublic()))) {
                return keys.get(i);
            }
        }
        return null;
    }

    public String calcTAddress(byte[] pubKeyHash) {
        byte[] version = ByteBuffer.allocate(4).putInt(this.params.getAddressHeader()).array();
        byte[] hash160 = ByteUtil.trimLeadingZeroes(BitUtils.concatenate(version, pubKeyHash));
        byte[] hash = Sha256Hash.hashTwice(hash160);
        byte[] checksum = Arrays.copyOfRange(hash, 0, 4);
        return Base58.encode(BitUtils.concatenate(hash160, checksum));
    }

    @Override
    public KeyPair generateKeyPair(String secret) {
        ECKey ecKey = ECKey.fromPrivate(NumericUtil.hexToBytes(secret));
        byte[] pubKeyHash = ecKey.getPubKeyHash();
        return new KeyPair(ecKey.getPrivateKeyAsHex(), this.calcTAddress(pubKeyHash));
    }

    @Override
    public KeyPair generateKeyPair() {
        return this.generateKeyPair(new ECKey().getPrivateKeyAsHex());
    }

    private byte[] getBlake2bHash(byte[] bufferToHash, byte[] personalization) {
        byte[] out = new byte[32];
        Blake2b blake2b = new Blake2b(null, out.length, null, personalization);
        blake2b.update(bufferToHash, 0, bufferToHash.length);
        blake2b.digest(out, 0);
        return out;
    }

    private int varSliceSize(byte[] someScript) {
        int length = someScript.length;
        return (
        length < 0xfd ? 1
        : length <= 0xffff ? 3
        : length <= 0xffffffff ? 5
        : 9
        ) + length;
    }

    private int encodingLength(int number) {
        return (
        number < 0xfd ? 1
        : number <= 0xffff ? 3
        : number <= 0xffffffff ? 5
        : 9
        );
    }

    private int zcashTransactionByteLength(Transaction tx) {
        int byteLength = 0;
        byteLength += 4;  // Header
        byteLength += 4;
        byteLength += encodingLength(tx.getInputs().size());
        int sum;
        sum = 0;
        for (TransactionInput input: tx.getInputs()) {
            sum += 40 + varSliceSize(input.getScriptBytes());
        }
        byteLength += encodingLength(tx.getInputs().size());
        return byteLength;
    }

    @Override
    public String signRawTransaction(String rawTx, List<String> keys) {
        Transaction tx = buildTransaction(rawTx);
        tx.setVersion(4);

        try {
            byte[] hashPrevouts;
            byte[] hashOutputs;
            byte[] hashSequence;
            byte[] hashJoinSplits = ZERO;
            byte[] hashShieldedSpends = ZERO;
            byte[] hashShieldedOutputs = ZERO;

            UnsafeByteArrayOutputStream stream;

            // calc hash inputs
            stream = new UnsafeByteArrayOutputStream();
            for (TransactionInput input : tx.getInputs()) {
                input.getOutpoint().bitcoinSerialize(stream);
            }
            hashPrevouts = this.getBlake2bHash(stream.toByteArray(), "ZcashPrevoutHash".getBytes());

            // calc hash outputs
            stream = new UnsafeByteArrayOutputStream();
            for (TransactionOutput output : tx.getOutputs()) {
                output.bitcoinSerialize(stream);
            }
            hashOutputs = this.getBlake2bHash(stream.toByteArray(), "ZcashOutputsHash".getBytes());

            // calc hash sequence
            stream = new UnsafeByteArrayOutputStream();
            for (TransactionInput input : tx.getInputs()) {
                Utils.uint32ToByteStreamLE(input.getSequenceNumber(), stream);
            }
            hashSequence = this.getBlake2bHash(stream.toByteArray(), "ZcashSequencHash".getBytes());

            List<byte[]> signatures = new ArrayList<>();
            for (int i = 0; i < tx.getInputs().size(); i++) {
                TransactionInput input = tx.getInput(i);

                Script scriptPubKey = new Script(input.getScriptBytes());

                ECKey ecKey = ECKey.fromPrivate(NumericUtil.hexToBytes(this.selectPrivateKeys(scriptPubKey, keys)));

                int baseBufferSize = 0;
                baseBufferSize += 4 * 5;  // header, nVersionGroupId, lock_time, nExpiryHeight, hashType
                baseBufferSize += 32 * 4;  // 256 hashes: hashPrevouts, hashSequence, hashOutputs, hashJoinSplits

                baseBufferSize += 4 * 2;  // input.index, input.sequence
                baseBufferSize += 8;  // value
                baseBufferSize += 32;  // input.hash
                baseBufferSize += this.varSliceSize(scriptPubKey.getProgram());  // prevOutScript

                baseBufferSize += 32 * 2;  // hashShieldedSpends and hashShieldedOutputs
                baseBufferSize += 8;  // valueBalance

                stream = new UnsafeByteArrayOutputStream(baseBufferSize);
                // Header
                // int header = 4 | (1 << 31); // -2147483644 = 4 | (1 << 31);
                // stream.write(ByteBuffer.allocate(4).putInt(header).array());    // 04 000080 04000080
                stream.write(NumericUtil.hexToBytes("04000080"));
                // versionGroupId
                Utils.uint32ToByteStreamLE(VERSION_GROUP_ID, stream);
                stream.write(hashPrevouts);
                stream.write(hashSequence);
                stream.write(hashOutputs);
                stream.write(hashJoinSplits);
                stream.write(hashShieldedSpends);
                stream.write(hashShieldedOutputs);

                // Locktime
                Utils.uint32ToByteStreamLE(tx.getLockTime(), stream);
                // ExpiryHeight
                Utils.uint32ToByteStreamLE(0, stream);
                // ValueBalance, ZCash version >= 4 default = 0
                Utils.uint64ToByteStreamLE(BigInteger.valueOf(0), stream);
                // Sighash type 1 = all
                Utils.uint32ToByteStreamLE(1L, stream);

                // If this hash is for a transparent input signature (i.e. not for txTo.joinSplitSig):
                stream.write(BitUtils.reverseBytes(input.getOutpoint().getHash().getBytes()));
                Utils.uint32ToByteStreamLE(input.getOutpoint().getIndex(), stream);

                stream.write(scriptPubKey.getProgram().length);
                stream.write(scriptPubKey.getProgram());

                long amount = Objects.requireNonNull(input.getValue()).getValue();
                Utils.uint64ToByteStreamLE(BigInteger.valueOf(amount), stream);
                Utils.uint32ToByteStreamLE(input.getSequenceNumber(), stream);

                byte[] hashPreimage = stream.toByteArray();

                // "ZcashSigHash".getBytes() + NumericUtil.hexToBytes("930b540d")
                // byte[] personalization = ArrayUtils.addAll("ZcashSigHash".getBytes(), NumericUtil.hexToBytes("76b809bb"));
                stream = new UnsafeByteArrayOutputStream(16);
                stream.write("ZcashSigHash".getBytes());
                Utils.uint32ToByteStreamLE(consensusBranchId, stream);
                byte[] personalization = stream.toByteArray();
                byte[] signatureHash = this.getBlake2bHash(hashPreimage, personalization);
                ECKey.ECDSASignature signature = ecKey.sign(Sha256Hash.wrap(signatureHash));
                byte hashType = 0x01;
                // sig
                byte[] sig = ByteUtil.concat(signature.encodeToDER(), new byte[]{hashType});
                signatures.add(sig);
            }

            // the second stream is used to calc the traditional txhash

            stream = new UnsafeByteArrayOutputStream();
            stream.write(NumericUtil.hexToBytes("04000080"));   // Set overwinter bit
            Utils.uint32ToByteStreamLE(VERSION_GROUP_ID, stream);
            stream.write(tx.getInputs().size());
            for (int i = 0; i < tx.getInputs().size(); i++) {
                TransactionInput input = tx.getInput(i);
                stream.write(BitUtils.reverseBytes(input.getOutpoint().getHash().getBytes()));
                Utils.uint32ToByteStreamLE(input.getOutpoint().getIndex(), stream);
                Script scriptPubKey = new Script(input.getScriptBytes());
                ECKey ecKey = ECKey.fromPrivate(NumericUtil.hexToBytes(this.selectPrivateKeys(scriptPubKey, keys)));
                byte[] script = new ScriptBuilder().data(signatures.get(i)).data(ecKey.getPubKey()).build().getProgram();
                stream.write(script.length);
                stream.write(script);
                Utils.uint32ToByteStreamLE(input.getSequenceNumber(), stream);
            }
            stream.write(tx.getOutputs().size());
            for (int i = 0; i < tx.getOutputs().size(); i++) {
                TransactionOutput output = tx.getOutput(i);
                Utils.uint64ToByteStreamLE(BigInteger.valueOf(output.getValue().value), stream);
                stream.write(new VarInt(output.getScriptBytes().length).encode());
                stream.write(output.getScriptBytes());
            }
            // Locktime
            Utils.uint32ToByteStreamLE(tx.getLockTime(), stream);
            // ExpiryHeight
            Utils.uint32ToByteStreamLE(0, stream);
            // ValueBalance, ZCash version >= 4 default = 0
            Utils.uint64ToByteStreamLE(BigInteger.valueOf(0), stream);
            stream.write(0);    // ShieldedSpend
            stream.write(0);    // ShieldedOutput
            stream.write(0);    // joinsplits

            byte[] serialized = stream.toByteArray();

            // cal txid
            String txid = NumericUtil.bytesToHex(BitUtils.reverseBytes(Sha256Hash.twiceOf(serialized).getBytes()));
            JSONObject packedTx = new JSONObject();
            packedTx.put("txid", txid);
            packedTx.put("raw", NumericUtil.bytesToHex(serialized));
            return packedTx.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}