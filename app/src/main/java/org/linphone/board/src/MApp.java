package org.linphone.board.src;

import android.app.Application;


/**
 * Application
 */
public class MApp extends Application {

    public static MApp mApp;
    static KSerialPor COM1;

    @Override
    public void onCreate() {
        super.onCreate();
        mApp = this;

    }

    @Override
    public void onTerminate() {
        COM1.closeSerialPort();
        super.onTerminate();
    }

    public static void initCOM(KCallBack.CallBackInterface aCBI)
    {
        COM1 = new KSerialPor(aCBI);
    }
}

