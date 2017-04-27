package com.sankarmanoj.bluetooth_esd;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * Created by sankarmanoj on 27/4/17.
 */

public class BluetoothReceiver extends Thread {
    private BufferedReader input;
    private String input_buffer ;
    @Override
    public void run() {
        super.run();
        try {
            while (MainActivity.mainActivity.connected) {
                char input_value = (char) input.read();
                if(input_value=='~')
                {
                    input_buffer="";
                }
                else if(input_value=='!')
                {
                    Log.d("BRECV",input_buffer);
                    if(input_buffer.toLowerCase().charAt(0)=='d' &&input_buffer.toLowerCase().charAt(1)=='t')
                    {
                        final String temp = input_buffer.substring(3);
                        MainActivity.mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity.mainActivity.temperatureTextView.setText(temp+" C");
                            }
                        });
                    }
                }
                else
                {
                    input_buffer+=input_value;
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            MainActivity.mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MainActivity.mainActivity.connected = false;
                    MainActivity.mainActivity.bluetoothStatusTextView.setTextColor(MainActivity.mainActivity.getResources().getColor(R.color.red));
                    MainActivity.mainActivity.bluetoothStatusTextView.setText("Not Connected");
                }
            });
        }
    }

    public BluetoothReceiver(BluetoothSocket socket)
    {
        input_buffer = "";
        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }
        catch (IOException e)
        {
            e.printStackTrace();
            MainActivity.mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MainActivity.mainActivity.connected = false;
                    MainActivity.mainActivity.bluetoothStatusTextView.setTextColor(MainActivity.mainActivity.getResources().getColor(R.color.red));
                    MainActivity.mainActivity.bluetoothStatusTextView.setText("Not Connected");
                }
            });
        }
    }
}
