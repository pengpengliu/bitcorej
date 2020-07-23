package org.bitcorej.chain.adk;

import org.apache.commons.lang3.ArrayUtils;
import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.KeyPair;
import org.bitcorej.chain.Transaction;
import org.bitcorej.chain.adk.hash.Curl;
import org.bitcorej.chain.adk.hash.ISS;
import org.bitcorej.utils.NumericUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.nervos.ckb.type.fixed.UInt32;

import java.security.SecureRandom;
import java.util.List;

public class ADKStateProvider implements ChainState {
    String characters = "9ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    @Override
    public KeyPair generateKeyPair(String secret) {
        int[] seed = Converter.trits(secret);
        int[] subseed = ISS.subseed(seed, 0);
        int[] key = ISS.key(subseed, 2);
        int[] digests = ISS.digests(key);
        int[] addressTrits = ISS.address(digests);
        String address = Converter.trytes(addressTrits);

        int[] l = Converter.trits(address);
        final int[] state = ArrayUtils.addAll(l, new int[729 - l.length]);
        Curl.transform(state);
        String checksum = Converter.trytes(state).substring(0, 9);
        return new KeyPair(secret, address + checksum);
    }

    @Override
    public KeyPair generateKeyPair() {
        StringBuilder seed = new StringBuilder();
        for (int i = 0; i < 81; i++) {
            SecureRandom random = new SecureRandom();
            byte[] bytes = new byte[4];
            random.nextBytes(bytes);
            UInt32 uInt32 = new UInt32(NumericUtil.bytesToHex(bytes));
            long index = uInt32.getValue() % 27;
            seed.append(characters, (int)index, (int)index + 1);
        }
        return generateKeyPair(seed.toString());
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
        String secret = keys.get(0);
        int[] seed = Converter.trits(secret);
        int[] subseed = ISS.subseed(seed, 0);
        int[] key = ISS.key(subseed, 2);

        JSONArray bundle = new JSONArray(rawTx);

        for (int i = 0; i < bundle.length(); i++) {
            if (bundle.getJSONObject(i).getLong("value") < 0) {
                String thisAddress = bundle.getJSONObject(i).getString("address");
                JSONArray normalizedBundleHashArray = bundle.getJSONObject(i).getJSONArray("normalizedBundleHash");
                int[] normalizedBundleHash = new int[normalizedBundleHashArray.length()];
                for (int j = 0; j < normalizedBundleHashArray.length(); j++) {
                    normalizedBundleHash[j] = normalizedBundleHashArray.getInt(j);
                }
                int[] firstBundleFragment = getSliceOfArray(normalizedBundleHash,0, 27);
                int[] firstFragment = getSliceOfArray(key,0, 6561);
                int[] firstSignedFragment = ISS.signatureFragment(firstBundleFragment, firstFragment);
                bundle.getJSONObject(i).put("signatureMessageFragment", Converter.trytes(firstSignedFragment));
                for (int j = 0; j < bundle.length(); j++) {
                    if (bundle.getJSONObject(j).getString("address").equals(thisAddress) && bundle.getJSONObject(j).getLong("value") == 0) {
                        int[] secondFragment = getSliceOfArray(key, 6561, 2 * 6561);
                        int[] secondBundleFragment = getSliceOfArray(normalizedBundleHash,27, 27 * 2);
                        int[] secondSignedFragment = ISS.signatureFragment(secondBundleFragment, secondFragment);
                        bundle.getJSONObject(j).put("signatureMessageFragment", Converter.trytes(secondSignedFragment));
                    }
                }
            }
            bundle.getJSONObject(i).remove("normalizedBundleHash");
        }
        return bundle.toString();
    }

    public static int[] getSliceOfArray(int[] arr,
                                        int start, int end)
    {

        // Get the slice of the Array
        int[] slice = new int[end - start];

        // Copy elements of arr to slice
        for (int i = 0; i < slice.length; i++) {
            slice[i] = arr[start + i];
        }

        // return the slice
        return slice;
    }
}
