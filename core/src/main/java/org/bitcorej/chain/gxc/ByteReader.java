package org.bitcorej.chain.gxc;

public class ByteReader implements Type.Reader{

    private byte[] _buf;
    private int _index;

    public ByteReader(byte[] buf) {
        _buf = buf;
        _index = 0;
    }

    public ByteReader(byte[] buf, int index) {
        _buf = buf;
        _index = index;
    }

    @Override
    public byte get() throws Type.InsufficientBytesException {
        checkAvailable(1);
        return _buf[_index++];
    }


    @Override
    public int getShortLE() throws Type.InsufficientBytesException {
        checkAvailable(2);
        return (((_buf[_index++] & 0xFF)) | ((_buf[_index++] & 0xFF) << 8)) & 0xFFFF;
    }

    @Override
    public int getIntLE() throws Type.InsufficientBytesException {
        checkAvailable(4);
        return ((_buf[_index++] & 0xFF)) | ((_buf[_index++] & 0xFF) << 8) | ((_buf[_index++] & 0xFF) << 16)
                | ((_buf[_index++] & 0xFF) << 24);
    }


    @Override
    public long getLongLE() throws Type.InsufficientBytesException {
        checkAvailable(8);
        return ((_buf[_index++] & 0xFFL)) | ((_buf[_index++] & 0xFFL) << 8) | ((_buf[_index++] & 0xFFL) << 16)
                | ((_buf[_index++] & 0xFFL) << 24) | ((_buf[_index++] & 0xFFL) << 32) | ((_buf[_index++] & 0xFFL) << 40)
                | ((_buf[_index++] & 0xFFL) << 48) | ((_buf[_index++] & 0xFFL) << 56);
    }

    @Override
    public byte[] getBytes(int size) throws Type.InsufficientBytesException {
        checkAvailable(size);
        byte[] bytes = new byte[size];
        System.arraycopy(_buf, _index, bytes, 0, size);
        _index += size;
        return bytes;
    }

    @Override
    public String getString() throws Type.InsufficientBytesException {
        int size = (int)(getVariableUint() & 0x7FFFFFFF); // put 에서 variable uint 로 넣음.
        byte[] bytes = getBytes(size);
        return new String(bytes);
    }

    @Override
    public long getVariableUint() throws Type.InsufficientBytesException {

        long v = 0;
        byte b, by = 0;
        do {
            b = get();
            v |= ( b & 0x7F) << by;
            by +=7;
        }
        while ( (b & 0x80) != 0 );

        return v;
    }


    private void checkAvailable(int num) throws Type.InsufficientBytesException {
        if (_buf.length - _index < num) {
            throw new Type.InsufficientBytesException();
        }
    }
}
