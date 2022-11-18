package Modules;

public class ByteBuffer {

    public ByteBuffer(int size) {
        this.size = size;
        buffer = new byte[this.size];
    }

    public int getSize() {
        return size;
    }

    private int size;
    byte[] buffer;

    public int getDataLen() {
        return dataLen;
    }

    private int dataLen = 0;

    public void AddRange(byte[] datas) {
        System.arraycopy(datas, 0, buffer, dataLen, datas.length);
        dataLen += datas.length;
    }

    public void RemoveRange(int len) {
        byte[] temp = new byte[size];
        System.arraycopy(buffer, len, temp, 0, dataLen - len);
        buffer = temp;
        dataLen -= len;
    }

    public byte[] GetRange(int s, int len) {
        byte[] temp = new byte[len];
        System.arraycopy(buffer, s, temp, 0, len);
        return temp;
    }

    public void Clear() {
        dataLen=0;
    }
}