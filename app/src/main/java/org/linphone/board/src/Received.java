package org.linphone.board.src;

public class Received {

    private byte[] mBuffe;
    private int size;

    public Received(byte[] mBuffe, int size) {
        this.mBuffe = mBuffe;
        this.size = size;
    }

    public byte[] getmBuffe() {
        return mBuffe;
    }

    public void setmBuffe(byte[] mBuffe) {
        this.mBuffe = mBuffe;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
