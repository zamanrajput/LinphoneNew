package org.linphone.board.src.models

import android.app.Application
import org.linphone.board.src.KCallBack
import org.linphone.board.src.KSerialPor

/**
 * Application
 */
class MAppKotlin : Application() {
    override fun onCreate() {
        super.onCreate()
        mApp = this
    }

    override fun onTerminate() {
        COM1!!.closeSerialPort()
        super.onTerminate()
    }

    companion object {
        var COM1: KSerialPor? = null

        var mApp: MAppKotlin? = null
        fun initCOM(aCBI: KCallBack.CallBackInterface?) {
            COM1 = KSerialPor(aCBI)
        }
    }
}
