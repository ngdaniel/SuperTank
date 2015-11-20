package com.ee472.daniel.supertank;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


public class SuperTank extends Activity implements Runnable, Handler.Callback, AdapterView.OnItemSelectedListener {

    private BlueSmirfSPP mSPP;
    private boolean mIsThreadRunning;
    private String mBluetoothAddress;
    private ArrayList<String> mArrayListBluetoothAddress;
    private String command;
    private Timer controlTimer;

    private TextView mTextViewStatus;
    private ArrayAdapter mArrayAdapterDevices;
    private Handler mHandler;

    private SeekBar a;
    private SeekBar b;
    private String signA = "0";
    private String signB = "0";
    private String valA = "00";
    private String valB = "00";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_tank);


        mIsThreadRunning = false;
        mBluetoothAddress = null;
        mSPP = new BlueSmirfSPP();
        mArrayListBluetoothAddress = new ArrayList<>();

        final JoystickView joystickView = (JoystickView)findViewById(R.id.joystick);
        joystickView.setOnJoystickMovedListener(new JoystickMovedListener() {
            @Override
            public void OnMoved(int a, int b) {
                signA = (a < 0) ? "0" : "1";
                signB = (b < 0) ? "0" : "1";
                valA = String.valueOf(Math.abs(a));

                valB = String.valueOf(Math.abs(b));
                valA = (valA.length() < 2) ? "0" + valA : valA;
                valB = (valB.length() < 2) ? "0" + valB : valB;
                valA = (valA.length() < 2) ? "0" + valA : valA;
                valB = (valB.length() < 2) ? "0" + valB : valB;

                Log.d("a", a + " ");
                Log.d("b", b + " ");
                command = "!" + signA + signB + valA + valB;
                sendMessage(command);
            }
            @Override
            public void OnHold() {
                //TimerTask task = new ControlTimerTask();
                //controlTimer = new Timer();
                //controlTimer.schedule(task, 0, 50);
            }

            @Override
            public void OnReleased() {
                sendMessage("#");
                //controlTimer.cancel();
                //controlTimer.purge();
            }


        });


        a = (VerticalSeekBar) findViewById(R.id.a);
        a.setMax(20);
        a.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                signA = (progressValue - 10 < 0) ? "0" : "1";
                valA = String.valueOf(Math.abs(progressValue - 10));

                valA = (valA.length() < 2) ? "0" + valA : valA;
                valA = (valA.length() < 2) ? "0" + valA : valA;

                command = "!" + signA + signB + valA + valB;
                sendMessage(command);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(10);
            }
        });

        b = (VerticalSeekBar) findViewById(R.id.b);
        b.setMax(20);
        b.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                signB = (progressValue - 10 < 0) ? "0" : "1";
                valB = String.valueOf(Math.abs(progressValue - 10));

                valB = (valB.length() < 2) ? "0" + valB : valB;
                valB = (valB.length() < 2) ? "0" + valB : valB;

                command = "!" + signA + signB + valA + valB;
                sendMessage(command);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(10);
            }
        });

        Button honk = (Button) findViewById(R.id.honk);
        honk.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                    //while (motionEvent.getAction() != android.view.MotionEvent.ACTION_UP){
                    sendMessage("?");
                    //}
                }
                return false;
            }
        });

        Button brake = (Button) findViewById(R.id.brake);
        brake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("#");
            }
        });

        Button manual = (Button) findViewById(R.id.manual);
        manual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("m");
            }
        });

        Button semiAutonomous = (Button) findViewById(R.id.semiautonomous);
        semiAutonomous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("s");
            }
        });

        Button autonomous = (Button) findViewById(R.id.autonomous);
        autonomous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("a");
            }
        });
        Switch control = (Switch) findViewById(R.id.control);
        control.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                if (isChecked) {
                    joystickView.setVisibility(View.GONE);
                    a.setVisibility(View.VISIBLE);
                    b.setVisibility(View.VISIBLE);

                } else {
                    joystickView.setVisibility(View.VISIBLE);
                    a.setVisibility(View.GONE);
                    b.setVisibility(View.GONE);
                }

            }
        });

        mTextViewStatus = (TextView) findViewById(R.id.status);
        ArrayList<String> items = new ArrayList<>();
        Spinner mSpinnerDevices = (Spinner) findViewById(R.id.devices);
        mArrayAdapterDevices = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        mHandler = new Handler(this);
        mSpinnerDevices.setOnItemSelectedListener(this);
        mArrayAdapterDevices.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerDevices.setAdapter(mArrayAdapterDevices);


    }


    private void sendMessage(String message) {
        if (message != null) {
            Log.d("message", message);
            byte[] send = message.getBytes();
            mSPP.write(send);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_super_tank, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.secure_connect_scan: {

                return true;
            }
        }
        return false;
    }


    @Override
    protected void onResume()
    {
        super.onResume();

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> devices = adapter.getBondedDevices();
        mArrayAdapterDevices.clear();
        mArrayListBluetoothAddress.clear();
        if(devices.size() > 0)
        {
            for(BluetoothDevice device : devices)
            {
                mArrayAdapterDevices.add(device.getName());
                mArrayListBluetoothAddress.add(device.getAddress());
            }
        }
        else
        {
            mBluetoothAddress = null;
        }

        UpdateUI();
    }

    @Override
    protected void onPause()
    {
        mSPP.disconnect();
        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }


    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
        mBluetoothAddress = mArrayListBluetoothAddress.get(pos);
    }

    public void onNothingSelected(AdapterView<?> parent)
    {
        mBluetoothAddress = null;
    }


    public void onBluetoothSettings(View view)
    {
        Intent i = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
        startActivity(i);
    }

    public void onConnectLink(View view)
    {
        if(!mIsThreadRunning)
        {
            mIsThreadRunning = true;
            UpdateUI();
            Thread t = new Thread(this);
            t.start();
        }
    }

    public void onDisconnectLink(View view)
    {
        mSPP.disconnect();
    }

    public void run()
    {
        Looper.prepare();
        mSPP.connect(mBluetoothAddress);
        while(mSPP.isConnected())
        {

            mSPP.flush();

            if(mSPP.isError())
            {
                mSPP.disconnect();
            }

            mHandler.sendEmptyMessage(0);

            try { Thread.sleep((long) (1000.0F/30.0F)); }
            catch(InterruptedException e) { Log.e("interrupted", e.getMessage());}
        }

        mIsThreadRunning = false;
        mHandler.sendEmptyMessage(0);
    }

    public boolean handleMessage (Message msg)
    {
        UpdateUI();
        return true;
    }

    private void UpdateUI()
    {
        if(mSPP.isConnected())
        {
            mTextViewStatus.setText("connected to " + mSPP.getBluetoothAddress());
        }
        else if(mIsThreadRunning)
        {
            mTextViewStatus.setText("connecting to " + mBluetoothAddress);
        }
        else
        {
            mTextViewStatus.setText("disconnected");
        }
    }

    private class ControlTimerTask extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                        sendMessage(command);
                }
            });
        }
    }
}
