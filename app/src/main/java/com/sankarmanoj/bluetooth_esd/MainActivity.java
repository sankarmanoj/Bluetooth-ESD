package com.sankarmanoj.bluetooth_esd;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
public class MainActivity extends AppCompatActivity {

    SeekBar fanSeekBar, lightSeekBar;
    TextView fanCurrentValueTextView, lightCurrentValueTextView, temperatureTextView;
    TextView bluetoothStatusTextView;
    Boolean connected = false;
    Boolean loggedin = false;
    BluetoothSocket bluetoothSocket;
    BufferedReader bluetoothInput;
    PrintWriter bluetoothOutput;
    BluetoothAdapter bluetoothAdapter;
    Timer fanTimer, lightTimer;
    Button requestButton, loginbutton;
    DecimalFormat formatter;
    static MainActivity mainActivity;
    // SPP UUID service
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivity = this;
        fanSeekBar = (SeekBar)findViewById(R.id.fanSeekBar);
        lightSeekBar = (SeekBar)findViewById(R.id.lightSeekBar);
        bluetoothStatusTextView = (TextView)findViewById(R.id.bluetoothStatusTextView);
        bluetoothStatusTextView.setTextColor(getResources().getColor(R.color.red));
        fanSeekBar.setProgress(0);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),0);
        fanCurrentValueTextView = (TextView) findViewById(R.id.fanSeekStatusTextView);
        fanCurrentValueTextView.setText("0");
        lightCurrentValueTextView = (TextView)findViewById(R.id.lightStatusTextView);
        lightCurrentValueTextView.setText("0");
        fanTimer = new Timer(true);
        lightTimer = new Timer(true);
        temperatureTextView = (TextView)findViewById(R.id.temperatureStatusTextView);
        requestButton = (Button)findViewById(R.id.tempRequestButton);
        loginbutton = (Button)findViewById(R.id.loginbutton);
        loginbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(connected)
                {
                    login();
                }
            }
        });
        formatter = new DecimalFormat("00");
        fanSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {
                fanCurrentValueTextView.setText(String.valueOf(progress));

                if(connected)
                {
                    fanTimer.cancel();
                    fanTimer = new Timer(true);
                    TimerTask task = new TimerTask() {
                        @Override
                        public void run() {
                            bluetoothOutput.write("~CF1"+formatter.format(progress)+"!");
                            bluetoothOutput.flush();
                        }
                    };
                    fanTimer.schedule(task,30);


                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        lightSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {
                lightCurrentValueTextView.setText(String.valueOf(progress));

                if(connected)
                {
                    lightTimer.cancel();
                    lightTimer = new Timer(true);
                    TimerTask task = new TimerTask() {
                        @Override
                        public void run() {
                            bluetoothOutput.write("~CL1"+formatter.format(progress)+"!");
                            bluetoothOutput.flush();
                        }
                    };
                    lightTimer.schedule(task,30);


                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(connected)
                {
                    bluetoothOutput.write("~RT100!");
                    bluetoothOutput.flush();

                }
            }
        });
        bluetoothStatusTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(connected == false)
                {
                     final Set<BluetoothDevice> pairedDevices;
                    pairedDevices = bluetoothAdapter.getBondedDevices();
                    ArrayList<String> list = new ArrayList<String>();
                    for(BluetoothDevice bt : pairedDevices)
                        list.add(bt.getName());

                    AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
                    builder.setTitle("Select A Device");
                    builder.setItems(list.toArray(new CharSequence[list.size()]), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            BluetoothDevice device = (BluetoothDevice) pairedDevices.toArray()[which];
                            try {
                                bluetoothSocket = createRfcommSocket(device);
                                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                                bluetoothSocket.connect();
                            }
                            catch (IOException e)
                            {
                                connected = false;
                                e.printStackTrace();

                                Toast.makeText(getApplicationContext(),"Unable to Connect",Toast.LENGTH_LONG).show();
                            }
                            if(bluetoothSocket.isConnected())
                            {
                                connected = true;
                                bluetoothStatusTextView.setText("Connected");
                                bluetoothStatusTextView.setTextColor(getResources().getColor(R.color.green));
                                try {
                                    bluetoothOutput = new PrintWriter(new OutputStreamWriter(bluetoothSocket.getOutputStream()));
                                    bluetoothInput = new BufferedReader(new InputStreamReader(bluetoothSocket.getInputStream()));
                                }
                                catch (IOException e)
                                {
                                }
                                new BluetoothReceiver(bluetoothSocket).start();
                            }
                            else
                            {
                                Toast.makeText(getApplicationContext(),"Unable to Connect",Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    builder.show();

                }
                else
                {
                    if(bluetoothSocket.isConnected())
                    {
                        connected = false;
                        try {
                            bluetoothSocket.close();
                        }catch (IOException e)
                        {                        }
                    }
                    bluetoothStatusTextView.setText("Not Connected");
                    bluetoothStatusTextView.setTextColor(getResources().getColor(R.color.red));
                }
            }
        });

    }
    public void login()
    {
        if(!loggedin)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
            builder.setTitle("Enter Password");

            final EditText input = new EditText(this);

            input.setInputType(InputType.TYPE_CLASS_TEXT );
            builder.setView(input);


            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String m_Text = input.getText().toString();
                    bluetoothOutput.write(m_Text);
                    bluetoothOutput.flush();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        }
    }
    public static BluetoothSocket createRfcommSocket(BluetoothDevice device) {
         final boolean D = true;
         final String TAG = "RFSOCK";
        BluetoothSocket tmp = null;
        try {

            Class class1 = device.getClass();
            Class aclass[] = new Class[1];
            aclass[0] = Integer.TYPE;
            Method method = class1.getMethod("createRfcommSocket", aclass);
            Object aobj[] = new Object[1];
            aobj[0] = Integer.valueOf(1);

            tmp = (BluetoothSocket) method.invoke(device, aobj);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            if (D) Log.e(TAG, "createRfcommSocket() failed", e);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            if (D) Log.e(TAG, "createRfcommSocket() failed", e);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            if (D) Log.e(TAG, "createRfcommSocket() failed", e);
        }
        return tmp;
    }
}
