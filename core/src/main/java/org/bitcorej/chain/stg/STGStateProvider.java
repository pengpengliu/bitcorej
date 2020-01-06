package org.bitcorej.chain.stg;

import cafe.cryptography.curve25519.CompressedRistretto;
import cafe.cryptography.curve25519.InvalidEncodingException;
import cafe.cryptography.curve25519.RistrettoElement;
import cafe.cryptography.curve25519.Scalar;
import iost.crypto.Ed25519;
import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.KeyPair;
import org.bitcorej.chain.Transaction;
import org.bitcorej.crypto.Bech32;
import org.bitcorej.utils.NumericUtil;
import org.whispersystems.curve25519.Curve25519;
import org.whispersystems.curve25519.Curve25519KeyPair;

import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

public class STGStateProvider implements ChainState {
    @Override
    public KeyPair generateKeyPair(String secret) {
        byte[] bytes = NumericUtil.hexToBytes(secret);
        // 2. Convert our secret key to Scalar type
        Scalar scalar_from_bytes = Scalar.fromBits(bytes);
        Scalar scalar = scalar_from_bytes.reduce(); // make canonical
        // Use rust generator constant instead of java.
        byte[] rust_generator = { -116, -110, 64, -76, 86, -87, -26, -36, 101, -61, 119, -95, 4, -115, 116, 95, -108, -96,
                -116, -37, 127, 68, -53, -51, 123, 70, -13, 64, 72, -121, 17, 52 };
        CompressedRistretto compresed = new CompressedRistretto(rust_generator);
        try {
            // 3. multiply using Generator, by result you would have RistrettoElement -
            // which is our pk
            RistrettoElement pk = compresed.decompress().multiply(scalar);
            // 4. compress RistrettoElement (this would byte representation of pk)
            CompressedRistretto pk_bytes = pk.compress();
            // System.out.println(Arrays.toString(pk_bytes.toByteArray()));
            // 5. encode using bech32, with needed prefix, and you will have our address.
            byte[] data_u8 = convert_to_u5(pk_bytes.toByteArray());
            // System.out.println(Arrays.toString(data_u8));
            String address = Bech32.encode("stg", data_u8);
            return new KeyPair(secret, address);
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public KeyPair generateKeyPair() {
        // 1. generate random hash, lower than 2^252 (this would be our secret key)
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        bytes[31] = (byte) (bytes[0] & 0x0f);
        return generateKeyPair(NumericUtil.bytesToHex(bytes));
    }

    private byte[] convert_to_u5(byte[] source) {
        // Create buffer of 5-bit groups
        ByteArrayOutputStream data = new ByteArrayOutputStream(); // Every element is uint5
        // Variables/constants for bit processing
        final int IN_BITS = 8;
        final int OUT_BITS = 5;
        int inputIndex = 0;
        int bitBuffer = 0; // Topmost bitBufferLen bits are valid; remaining lower bits are zero
        int bitBufferLen = 0; // Always in the range [0, 12]
        // Repack all 8-bit bytes into 5-bit groups, adding padding
        while (inputIndex < source.length || bitBufferLen > 0) {
            assert 0 <= bitBufferLen && bitBufferLen <= IN_BITS + OUT_BITS - 1;
            assert (bitBuffer << bitBufferLen) == 0;
            if (bitBufferLen < OUT_BITS) {
                if (inputIndex < source.length) { // Read a byte
                    bitBuffer |= (source[inputIndex] & 0xFF) << (32 - IN_BITS - bitBufferLen);
                    inputIndex++;
                    bitBufferLen += IN_BITS;
                } else // Create final padding
                    bitBufferLen = OUT_BITS;
            }
            assert bitBufferLen >= 5;
            // Write a 5-bit group
            data.write(bitBuffer >>> (32 - OUT_BITS)); // uint5
            bitBuffer <<= OUT_BITS;
            bitBufferLen -= OUT_BITS;
        }
        return data.toByteArray();
    }

    @Override
    public Boolean validateTx(String rawTx, String requestTx) {
        return null;
    }

    @Override
    public Transaction decodeRawTransaction(String rawTx) {
        return null;
    }

    @Override
    public String signRawTransaction(String rawTx, List<String> keys) {
        return null;
    }
}
