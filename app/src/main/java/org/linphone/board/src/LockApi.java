package org.linphone.board.src;


import org.linphone.board.src.models.AdminConfig;
import org.linphone.board.src.models.PortListener;
import org.linphone.board.src.models.MAppKotlin;


public class LockApi {
    public static final String TAG = "UPUS";
    public static final int SLEEPTIME = 50;
    public static final String SUCCESS = "success";
    public static final String FAIL = "fail";
    public static final String OVERTIME = "time out";
    public static final String EXCEPTION = "Exception";
    public static final String VALID = "Great";
    public static final String INVALID = "invalid";
    public static final String DOOR_OPEN_START = "8a";
    public static final String DOOR_CLOSE_START = "82";


    //Serial port settings---------------------------------------------
    public static void connectWithBoard(AdminConfig adminConfig, PortListener listener) {
        setCommLock(adminConfig.devicePortName, adminConfig.baudRate, listener);
    }

    public static boolean setCommLock(String path, int baudrate, PortListener listener) {
        try {
            if (MAppKotlin.Companion.getCOM1().initSerialPort(path, baudrate)) {
                listener.onResponse(SUCCESS);
                return true;
            } else {
                listener.onResponse(FAIL);
            }
        } catch (Exception e) {
            e.printStackTrace();
            listener.onResponse(e.getLocalizedMessage());
        }
        return false;
    }


    public static boolean setCommLock(String path, int baudrate, String[] rsMsg) {
        try {
            if (MAppKotlin.Companion.getCOM1().initSerialPort(path, baudrate)) {
                rsMsg[0] = SUCCESS;
                return true;
            } else {
                rsMsg[0] = FAIL;
            }
        } catch (Exception e) {
            rsMsg[0] = EXCEPTION;
        }
        return false;
    }

    public static String toHexString(byte[] arg, int length) {
        String result = "";
        if (arg != null) {
            for (int i = 0; i < length; i++) {
                result = result + (Integer.toHexString(arg[i] < 0 ? arg[i] + 256 : arg[i]).length() == 1 ? "0" + Integer.toHexString(arg[i] < 0 ? arg[i] + 256 : arg[i]) : Integer.toHexString(arg[i] < 0 ? arg[i] + 256 : arg[i])) + " ";
            }
            return result;
        }
        return "";
    }

    public static byte[] toByteArray(String arg) {
        if (arg != null) {
            /* 1. First remove the ' ' in the String, and then convert the String to a char array*/
            char[] NewArray = new char[1000];
            char[] array = arg.toCharArray();
            int length = 0;
            for (int i = 0; i < array.length; i++) {
                if (array[i] != ' ') {
                    NewArray[length] = array[i];
                    length++;
                }
            }
            /* Convert the values in the char array to an actual decimal array */
            int EvenLength = (length % 2 == 0) ? length : length + 1;
            if (EvenLength != 0) {
                int[] data = new int[EvenLength];
                data[EvenLength - 1] = 0;
                for (int i = 0; i < length; i++) {
                    if (NewArray[i] >= '0' && NewArray[i] <= '9') {
                        data[i] = NewArray[i] - '0';
                    } else if (NewArray[i] >= 'a' && NewArray[i] <= 'f') {
                        data[i] = NewArray[i] - 'a' + 10;
                    } else if (NewArray[i] >= 'A' && NewArray[i] <= 'F') {
                        data[i] = NewArray[i] - 'A' + 10;
                    }
                }
                /* The value of each char is composed of two hexadecimal data */
                byte[] byteArray = new byte[EvenLength / 2];
                for (int i = 0; i < EvenLength / 2; i++) {
                    byteArray[i] = (byte) (data[i * 2] * 16 + data[i * 2 + 1]);
                }
                return byteArray;
            }
        }
        return new byte[]{};
    }

    //send data
    public static boolean sendData(byte[] data, String[] rsMsg) {
        try {
            Received rs = MAppKotlin.Companion.getCOM1().getReceived(data);

            if (rs != null) {
                rsMsg[0] = toHexString(rs.getmBuffe(), rs.getSize());
                return true;
            } else {
                rsMsg[0] = OVERTIME;

            }
        } catch (Exception e) {
            e.printStackTrace();
            rsMsg[0] = EXCEPTION;
        }

        return false;
    }

    //Lock Control Board Command
    public static boolean lockCmd(byte cmdNo, byte boardNo, byte lockNo, String[] rsMsg) {
        try {
            byte[] sendData = new byte[5];
            sendData[0] = cmdNo;
            sendData[1] = boardNo;
            sendData[2] = lockNo;
            sendData[3] = (byte) 0x11;
            sendData[4] = (byte) (sendData[0] ^ sendData[1] ^ sendData[2] ^ sendData[3]);

            Received rs =  MAppKotlin.Companion.getCOM1().getReceived(sendData);

            if (rs != null) {
                if (rs.getSize() == 5 && rs.getmBuffe()[0] == cmdNo) {
                    if (cmdNo == (byte) 0x80) //get status
                    {
                        if (rs.getmBuffe()[3] == (byte) 0x11) {
                            rsMsg[0] = VALID;
                            return true;
                        } else {
                            rsMsg[0] = INVALID;
                        }
                    } else {
                        rsMsg[0] = SUCCESS;
                        return true;
                    }
                } else if (rs.getSize() == 7 && rs.getmBuffe()[0] == cmdNo) {
                    rsMsg[0] = Integer.toBinaryString((rs.getmBuffe()[2] & 0xFF) + 0x100).substring(1) + Integer.toBinaryString((rs.getmBuffe()[3] & 0xFF) + 0x100).substring(1) + Integer.toBinaryString((rs.getmBuffe()[4] & 0xFF) + 0x100).substring(1);
                    return true;
                } else {
                    rsMsg[0] = FAIL;
                }
            } else {
                rsMsg[0] = OVERTIME;

            }
        } catch (Exception e) {
            e.printStackTrace();
            rsMsg[0] = EXCEPTION;
        }

        return false;
    }

    public static boolean openLock(byte boardNo, byte lockNo, String[] rsMsg) {
        return lockCmd((byte) 0x8A, boardNo, lockNo, rsMsg);
    }

    public static boolean openAllLock(byte boardNo, String[] rsMsg) {
        return lockCmd((byte) 0x9D, boardNo, (byte) 0x02, rsMsg);
    }

    public static boolean openPower(byte boardNo, byte lockNo, String[] rsMsg) {
        return lockCmd((byte) 0x9A, boardNo, lockNo, rsMsg);

    }

    public static boolean closePower(byte boardNo, byte lockNo, String[] rsMsg) {
        return lockCmd((byte) 0x9B, boardNo, lockNo, rsMsg);

    }

    public static boolean getState(byte boardNo, byte lockNo, String[] rsMsg) {
        return lockCmd((byte) 0x80, boardNo, lockNo, rsMsg);

    }

    public static boolean getAllState(byte boardNo, String[] rsMsg) {
        return lockCmd((byte) 0x80, boardNo, (byte) 0x0, rsMsg);

    }

}
