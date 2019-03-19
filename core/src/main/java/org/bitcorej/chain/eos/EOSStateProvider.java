package org.bitcorej.chain.eos;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.KeyPair;
import org.bitcorej.chain.eos.types.TypeChainId;
import org.bitcorej.core.Network;
import org.bitcorej.utils.ByteUtil;
import org.bitcorej.utils.NumericUtil;
import org.spongycastle.crypto.digests.RIPEMD160Digest;

import java.util.Arrays;
import java.util.List;

public class EOSStateProvider implements ChainState {

    private Network network;
    private TypeChainId chainId;

    public EOSStateProvider(Network network) {
        switch (network) {
            case MAIN:
                chainId = new TypeChainId("");
            case TEST:
                chainId = new TypeChainId("e70aaab8997e1dfce58fbfac80cbbb8fecec7b99cf982a9444273cbc64c41473");
        }

        this.network = network;
    }

    @Override
    public KeyPair generateKeyPair(String secret) {
        ECKey ecKey = ECKey.fromPrivate(NumericUtil.hexToBytes(secret));
        byte[] pubKeyData = ecKey.getPubKey();
        RIPEMD160Digest digest = new RIPEMD160Digest();
        digest.update(pubKeyData, 0, pubKeyData.length);
        byte[] out = new byte[20];
        digest.doFinal(out, 0);
        byte[] checksumBytes = Arrays.copyOfRange(out, 0, 4);

        pubKeyData = ByteUtil.concat(pubKeyData, checksumBytes);
        return new KeyPair(ecKey.getPrivateKeyAsHex(), "EOS" + Base58.encode(pubKeyData));
    }

    @Override
    public KeyPair generateKeyPair() {
        return this.generateKeyPair(new ECKey().getPrivateKeyAsHex());
    }

    @Override
    public String signRawTransaction(String rawTx, List<String> keys) {
        Gson mGson = new GsonBuilder()
                .registerTypeAdapterFactory(new GsonEosTypeAdapterFactory())
                .excludeFieldsWithoutExposeAnnotation().create();

        SignedTransaction tx = mGson.fromJson(rawTx, SignedTransaction.class);
        tx.sign(keys.get(0), this.chainId);
        return mGson.toJson(tx);
    }
}
