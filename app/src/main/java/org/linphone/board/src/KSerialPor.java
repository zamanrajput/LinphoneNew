package org.linphone.board.src;


import android.serialport.SerialPort;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.util.concurrent.Semaphore;

public class KSerialPor {
    private static final String TAG = "UPUS";
    private static final int SLEEPTIME = 50;

    //serial port
    private SerialPort mSerialPort = null;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    private ReadThread mReadThread;

    public boolean isInvalidParameterException = false;//Failed to obtain serial port address and baud rate
    /**
     * amount of signal
     */
    private Semaphore semaphore;
    private KCallBack.CallBackInterface cbi;
    public KSerialPor(KCallBack.CallBackInterface aCBI) {
        cbi= aCBI;
        semaphore = new Semaphore(1);
    }

    /**
     * Send serial information
     *
     * @param buffer
     */
    private synchronized boolean sendData(byte[] buffer) {
        if (mOutputStream != null) {
            try {
                mOutputStream.write(buffer);
                mOutputStream.flush();

                return true;
            } catch (IOException e) {
                e.printStackTrace();

            }
        } else {
            System.out.println("mOutputStream is null!");
        }
        return false;
    }

    public static String toHexString(byte[] arg, int length) {
        String result = new String();
        if (arg != null) {
            for (int i = 0; i < length; i++) {
                result = result
                        + (Integer.toHexString(
                        arg[i] < 0 ? arg[i] + 256 : arg[i]).length() == 1 ? "0"
                        + Integer.toHexString(arg[i] < 0 ? arg[i] + 256
                        : arg[i])
                        : Integer.toHexString(arg[i] < 0 ? arg[i] + 256
                        : arg[i])) + " ";
            }
            return result;
        }
        return "";
    }

    private boolean mReceived = false;
    private byte[] mBuffer = null;
    private int mSize = -1;

    /**
     * Receive serial port information
     */
    private class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                int size;
                try {
                    byte[] buffer = new byte[1024];
                    if (mInputStream == null) {
                        return;
                    }
                    size = mInputStream.read(buffer);
                    if (size > 0) {
                        mBuffer = buffer;
                        mSize = size;
                        mReceived = true;
                        cbi.sendToData(toHexString(buffer, size));
                    }
                    try {
                        Thread.sleep(SLEEPTIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    /**
     * Get a serial port instance
     *
     * @return
     * @throws SecurityException
     * @throws IOException
     * @throws InvalidParameterException
     */
    private SerialPort getSerialPort(String path, int baudrate) throws SecurityException, IOException, InvalidParameterException {
        if (mSerialPort == null) {
            if ((path.length() == 0) || (baudrate == -1)) {
                throw new InvalidParameterException();
            }

            mSerialPort = new SerialPort(new File(path), baudrate, 0);

        }
        return mSerialPort;
    }

    /**
     * Get serial port information
     *
     * @param buffer
     * @return
     */
    public Received getReceived(byte[] buffer) {
        try {
            semaphore.acquire();
            mReceived = false;
            mBuffer = null;
            mSize = -1;
            if (sendData(buffer)) {
                int n= 3000 ;
                while (!mReceived && n > 0) {
                    try {
                        Thread.sleep(SLEEPTIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    n-= SLEEPTIME;
                }
                if (mReceived && null != mBuffer && mSize > 0) {
                    byte[] rtBuffer= new byte[mSize];
                    int rtSize= mSize;
                    System.arraycopy(mBuffer, 0, rtBuffer, 0, rtSize);
                    Received rt= new Received(rtBuffer, rtSize);
                    semaphore.release();
                    return rt;
                } else {

                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();

        }
        semaphore.release();
        return null;
    }

    /**
     * Initialize the serial port
     */
    public boolean initSerialPort(String path, int baudrate) {
        try {
            isInvalidParameterException = false;
            if (mSerialPort!=null)
            {
                closeSerialPort();
            }
            mSerialPort = getSerialPort(path, baudrate);
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();
            /* Create a receiving thread */
            mReadThread = new ReadThread();
            mReadThread.start();
            isInvalidParameterException = true;
        } catch (SecurityException e) {
            isInvalidParameterException = false;
        } catch (IOException e) {
            isInvalidParameterException = false;
        } catch (InvalidParameterException e) {
            isInvalidParameterException = false;
        }
        return isInvalidParameterException;
    }

    /**
     * close the serial port
     */
    public void closeSerialPort() {
        if (mReadThread != null) {
            mReadThread.interrupt();
            mReadThread = null;
        }
        if (mSerialPort != null) {
            mSerialPort.close();
            mSerialPort = null;
        }
    }
}
