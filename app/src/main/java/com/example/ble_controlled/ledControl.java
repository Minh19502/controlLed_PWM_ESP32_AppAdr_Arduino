package com.example.ble_controlled;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.UUID;

public class ledControl extends AppCompatActivity {

    Button btnOn, btnOff, btnDis;
    SeekBar brightness;
    TextView lumn;
    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent newint = getIntent();
        address = newint.getStringExtra(DeviceList.EXTRA_ADDRESS); // receive the address of the bluetooth device

        setContentView(R.layout.activity_led_control);

        btnOn = findViewById(R.id.button2);
        btnOff = findViewById(R.id.button3);
        btnDis = findViewById(R.id.button4);
        brightness = findViewById(R.id.seekBar);
        lumn = findViewById(R.id.lumn);

        new ConnectBT().execute(); // Call the class to connect

        btnOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnOnLed(); // Method to turn on
            }
        });

        btnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnOffLed(); // Method to turn off
            }
        });

        btnDis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Disconnect(); // Close connection
            }
        });

        brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                lumn.setText(String.valueOf(progress));
                if (btSocket != null) {
                    try {
                        String command = String.format("%d\n", progress);
                        btSocket.getOutputStream().write(command.getBytes());
                    } catch (IOException e) {
                        msg("Error sending brightness value");
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Not used
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Not used
            }
        });
    }

    private void Disconnect() {
        if (btSocket != null) { // If the btSocket is busy
            try {
                btSocket.close(); // Close connection
            } catch (IOException e) {
                msg("Error");
            }
        }
        finish(); // Return to the first layout
    }

    private void turnOffLed() {
        if (btSocket != null) {
            try {
                btSocket.getOutputStream().write("led_off\n".getBytes());
            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    private void turnOnLed() {
        if (btSocket != null) {
            try {
                btSocket.getOutputStream().write("led_on\n".getBytes());
            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_led_control, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean ConnectSuccess = true; // If it's here, it's almost connected

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(ledControl.this, "Connecting...", "Please wait!!!");
        }

        @Override
        protected Void doInBackground(Void... devices) {
            try {
                if (btSocket == null || !isBtConnected) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter(); // Get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address); // Connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID); // Create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect(); // Start connection
                }
            } catch (IOException e) {
                ConnectSuccess = false; // If the try failed, you can check the exception here
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (!ConnectSuccess) {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            } else {
                msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }
}
