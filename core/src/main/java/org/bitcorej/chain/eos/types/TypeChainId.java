package org.bitcorej.chain.eos.types;

import org.bitcorej.crypto.Hash;

/**
 * Created by swapnibble on 2017-09-12.
 */
public class TypeChainId {
    private final byte[] raw;

    public TypeChainId() {
        raw = Hash.sha256(new byte[32]);
    }

    byte [] getSha256FromHexStr(String str){
        int len = str.length();
        byte [] bytes = new byte[32];
        for(int i=0;i<len;i+=2){
            String strIte = str.substring(i, i+2);
            Integer n =Integer.parseInt(strIte, 16) & 0xFF;
            bytes[i/2] = n.byteValue();
        }
        return bytes;
    }
    public TypeChainId(String str){
        raw = Hash.sha256(getSha256FromHexStr(str));
    }

    public byte[] getBytes() {
        return this.raw;
    }
}