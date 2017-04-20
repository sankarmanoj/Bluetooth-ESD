package com.sankarmanoj.bluetooth_esd;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
public class MainActivity extends AppCompatActivity {

    SeekBar seekBar;
    TextView currentValueTextView;
    TextView bluetoothStatusTextView;
    Boolean connected = false;
    BluetoothSocket bluetoothSocket;
    BufferedReader bluetoothInput;
    PrintWriter bluetoothOutput;
    BluetoothAdapter bluetoothAdapter;
    static MainActivity mainActivity;
    // SPP UUID service
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivity = this;
        seekBar = (SeekBar)findViewById(R.id.seekBar);
        bluetoothStatusTextView = (TextView)findViewById(R.id.bluetoothStatusTextView);
        bluetoothStatusTextView.setTextColor(getResources().getColor(R.color.red));
        seekBar.setProgress(0);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),0);
        currentValueTextView = (TextView) findViewById(R.id.seekStatusTextView);
        currentValueTextView.setText("0");

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentValueTextView.setText(String.valueOf(progress));
                if(connected&&bluetoothSocket.isConnected())
                {
                    bluetoothOutput.write("~"+String.valueOf(progress)+"!");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

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
                                bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                                bluetoothSocket.connect();
                            }
                            catch (IOException e)
                            {
                                connected = false;
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
}
