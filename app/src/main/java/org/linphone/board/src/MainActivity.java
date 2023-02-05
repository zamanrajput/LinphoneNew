package org.linphone.board.src;


import static org.linphone.board.src.LockApi.SUCCESS;
import static org.linphone.board.src.LockApi.closePower;
import static org.linphone.board.src.LockApi.connectWithBoard;
import static org.linphone.board.src.LockApi.getAllState;
import static org.linphone.board.src.LockApi.getState;
import static org.linphone.board.src.LockApi.openAllLock;
import static org.linphone.board.src.LockApi.openLock;
import static org.linphone.board.src.LockApi.openPower;
import static org.linphone.board.src.LockApi.sendData;
import static org.linphone.board.src.LockApi.setCommLock;
import static org.linphone.board.src.LockApi.toByteArray;
import static org.linphone.board.src.MApp.mApp;

import android.os.Bundle;
import android.serialport.SerialPort;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import org.linphone.R;
import org.linphone.board.src.models.AdminConfig;
import org.linphone.board.src.models.MAppKotlin;

import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    EditText boardNumEt, lockNumEt, sendCommandEt;
    TextView tvLog;

    private void showMessage(String text) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show());
    }

    public void writeLog(String text) {
        runOnUiThread(() -> tvLog.append(text));
    }

    private void initView() {
        boardNumEt = findViewById(R.id.boardNumberEt);
        lockNumEt = findViewById(R.id.lockNumberEt);
        sendCommandEt = findViewById(R.id.sendCommandEt);
        tvLog = findViewById(R.id.tvLog);
        tvLog.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    private boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        SharedPreUtil sharedPreUtil = new SharedPreUtil(MainActivity.this);
        AdminConfig adminConfig = sharedPreUtil.getConfig();
        adminConfig = new AdminConfig();
        adminConfig.baudRate = 9600;
        adminConfig.deviceID = "331A";
        adminConfig.devicePortName = "/dev/ttyS4";
//        SerialPort.setSuPath("/system/xbin/su");
        try {
            connectWithBoard(adminConfig, s -> runOnUiThread(() -> {
                if (s.equals(SUCCESS)) {
                    Toast.makeText(getApplicationContext(), "Successfully Connected With Board", Toast.LENGTH_LONG).show();
                    return;
                }
                Toast.makeText(getApplicationContext(), "Connection Failed:" + s, Toast.LENGTH_LONG).show();
            }));

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Connection Failed:" + e.getLocalizedMessage(), Toast.LENGTH_LONG).show());
        }


        //Initialize the serial port, incoming processing serial port reception
        MAppKotlin.Companion.initCOM(new KCallBack.CallBackInterface() {
            @Override
            public void sendToData(String str) {
                //do something
                writeLog("Serial port receiving：" + str + "\n");
            }
        });

        //Set serial port
        findViewById(R.id.btnSet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        //Connect to the serial port
        AdminConfig finalAdminConfig = adminConfig;
        findViewById(R.id.btnConn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            connectWithBoard(finalAdminConfig, s -> runOnUiThread(() -> {
                                if (s.equals(SUCCESS)) {
                                    Toast.makeText(getApplicationContext(), "Successfully Connected With Board", Toast.LENGTH_LONG).show();
                                    return;
                                }
                                Toast.makeText(getApplicationContext(), "Connection Failed:" + s, Toast.LENGTH_LONG).show();
                            }));

                        } catch (Exception e) {
                            e.printStackTrace();
                            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Connection Failed:" + e.getLocalizedMessage(), Toast.LENGTH_LONG).show());
                        }
                    }
                }).start();
            }
        });


        //unlock
        findViewById(R.id.btnUnlock).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(() -> {
                    if (!isInteger(boardNumEt.getText().toString()) || !isInteger(lockNumEt.getText().toString())) {
                        showMessage("Please enter a valid board number and lock number");
                        return;
                    }
                    String[] rsMsg = {""};
                    openLock(Byte.parseByte(boardNumEt.getText().toString()), Byte.parseByte(lockNumEt.getText().toString()), rsMsg);
                    showMessage("unlock," + rsMsg[0]);
                }).start();


            }
        });

        //all locks
        findViewById(R.id.btnUnlockAll).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (isInteger(boardNumEt.getText().toString()) == false) {
                            showMessage("Please enter a valid board number");
                            return;
                        }
                        String[] rsMsg = {""};
                        openAllLock(Byte.valueOf(boardNumEt.getText().toString()), rsMsg);
                        showMessage("all unlocked, complete");
                    }
                }).start();


            }
        });

        //state
        findViewById(R.id.btnState).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (isInteger(boardNumEt.getText().toString()) == false || isInteger(lockNumEt.getText().toString()) == false) {
                            showMessage("Please enter a valid board number and lock number");
                            return;
                        }
                        String[] rsMsg = {""};
                        getState(Byte.valueOf(boardNumEt.getText().toString()), Byte.valueOf(lockNumEt.getText().toString()), rsMsg);
                        showMessage("mode," + rsMsg[0]);
                    }
                }).start();

            }
        });

        //所有状态
        findViewById(R.id.btnAllStates).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (isInteger(boardNumEt.getText().toString()) == false) {
                            showMessage("Please enter a valid board number");
                            return;
                        }
                        String[] rsMsg = {""};
                        getAllState(Byte.valueOf(boardNumEt.getText().toString()), rsMsg);
                        showMessage("All States," + rsMsg[0]);
                    }
                }).start();

            }
        });

        //通电
        findViewById(R.id.btnPowerUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (isInteger(boardNumEt.getText().toString()) == false || isInteger(lockNumEt.getText().toString()) == false) {
                            showMessage("Please enter a valid board number and lock number");
                            return;
                        }
                        String[] rsMsg = {""};
                        openPower(Byte.valueOf(boardNumEt.getText().toString()), Byte.valueOf(lockNumEt.getText().toString()), rsMsg);
                        showMessage("Energize," + rsMsg[0]);
                    }
                }).start();


            }
        });

        //断电
        findViewById(R.id.btnPowerOutage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (isInteger(boardNumEt.getText().toString()) == false || isInteger(lockNumEt.getText().toString()) == false) {
                            showMessage("Please enter a valid board number and lock number");
                            return;
                        }
                        String[] rsMsg = {""};
                        closePower(Byte.valueOf(boardNumEt.getText().toString()), Byte.valueOf(lockNumEt.getText().toString()), rsMsg);
                        showMessage("power outage," + rsMsg[0]);
                    }
                }).start();
            }
        });

        //发送
        findViewById(R.id.btnSent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (sendCommandEt.getText().toString().trim().equals("")) {
                            showMessage("Please enter the sending hexadecimal content");
                            return;
                        }
                        String[] rsMsg = {""};
                        byte[] to_send = toByteArray(sendCommandEt.getText().toString());
                        if (sendData(to_send, rsMsg)) {
                            writeLog("take over:" + rsMsg[0] + "\n");
                        } else {
                            writeLog("Sending failed, or the serial port is not connected!" + "\n");
                        }
                    }
                }).start();
            }
        });

        //清空
        findViewById(R.id.btnCls).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvLog.setText("");
            }
        });


    }

    private void showToast(String[] rsMsg) {
        Toast.makeText(getApplicationContext(), rsMsg[0], Toast.LENGTH_SHORT).show();
    }


}
