package com.example.multimediav2.Utils;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class LimitedInputStream extends FilterInputStream {
    private long maxBytesToRead;

    public LimitedInputStream(InputStream in, long maxBytesToRead) {
        super(in);
        this.maxBytesToRead = maxBytesToRead;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (maxBytesToRead <= 0) {
            return -1;
        }
        if (len > maxBytesToRead) {
            len = (int) maxBytesToRead;
        }
        int count = super.read(b, off, len);
        if (count > 0) {
            maxBytesToRead -= count;
        }
        return count;
    }

    @Override
    public int read() throws IOException {
        if (maxBytesToRead <= 0) {
            return -1;
        }
        maxBytesToRead--;
        return super.read();
    }
}
