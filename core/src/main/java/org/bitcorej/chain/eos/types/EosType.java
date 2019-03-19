package org.bitcorej.chain.eos.types;

import java.util.Collection;

/**
 * Created by swapnibble on 2017-09-12.
 */

public interface EosType {
    class InsufficientBytesException extends Exception {

        private static final long serialVersionUID = 1L;
    }

    interface Packer {
        void pack(Writer writer);
    }

    interface Unpacker {
        void unpack(Reader reader) throws InsufficientBytesException;
    }

    interface Reader {
        byte get() throws InsufficientBytesException;
        int getShortLE() throws InsufficientBytesException;
        int getIntLE() throws InsufficientBytesException;
        long getLongLE() throws InsufficientBytesException;
        byte[] getBytes(int size) throws InsufficientBytesException;
        String getString() throws InsufficientBytesException;

        long getVariableUint() throws InsufficientBytesException;
    }

    interface Writer {
        void put(byte b);
        void putShortLE(short value);
        void putIntLE(int value);
        void putLongLE(long value);
        void putBytes(byte[] value);
        void putString(String value);
        byte[] toBytes();
        int length();

        void putCollection(Collection<? extends Packer> collection);

        void putVariableUInt(long val);
    }
}
