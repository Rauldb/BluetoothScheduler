package com.example.laru.myapplication;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Button;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TimePicker;

import java.sql.Time;
import java.text.DecimalFormat;
import java.util.Calendar;
import android.app.TimePickerDialog;
import android.view.View.OnClickListener;


import com.macroyau.blue2serial.BluetoothDeviceListDialog;
import com.macroyau.blue2serial.BluetoothSerial;
import com.macroyau.blue2serial.BluetoothSerialListener;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 *
 *  This Bluetooth app uses the Blue2Serial library by Macro Yau
 *
 */



// POLISHED BRANCH


public class TerminalActivity extends AppCompatActivity
        implements BluetoothSerialListener, BluetoothDeviceListDialog.OnDeviceSelectedListener {

    private static final int REQUEST_ENABLE_BLUETOOTH = 1;

    private BluetoothSerial bluetoothSerial;


    private Button sendButton;
    private RelativeLayout mainLayout;
    private ArrayList<TimePicker> timepickers = new ArrayList<TimePicker>();


    private MenuItem actionConnect, actionDisconnect;

    private boolean crlf = false;

    private OnClickListener listener    =   new OnClickListener() {
        @Override
        public void onClick(View v) {
            Calendar mcurrentTime = Calendar.getInstance();
            int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
            int minute = mcurrentTime.get(Calendar.MINUTE);
            final TextView picker = (TextView) v;

            TimePickerDialog mTimePicker;
            mTimePicker = new TimePickerDialog(TerminalActivity.this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute ) {

                    String hora = new DecimalFormat("00").format(selectedHour);
                    String minuto = new DecimalFormat("00").format(selectedMinute);
                    hora = guardar(hora, minuto);
                    picker.setText(hora);
                }
            }, hour, minute, true);
            mTimePicker.setTitle("Select Time");
            mTimePicker.show();

        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminal);

        // Find UI views and set listeners

        sendButton = (Button) findViewById(R.id.botonEnviar);
        mainLayout = (RelativeLayout) findViewById(R.id.mainLayout);

        ArrayList<Object> allInputs = new ArrayList<Object>();
        ArrayList<LinearLayout> linLayouts = new ArrayList<LinearLayout>();

        // Get all linearLayouts inside our main relative layout

        for( int i = 0; i < mainLayout.getChildCount()-1; i++ ) {
            if(mainLayout.getChildAt(i) instanceof LinearLayout) {
                linLayouts.add((LinearLayout)mainLayout.getChildAt(i));
            }

        }

        // Iterate through all elements inside each linear layout and check for checkboxes and edittexts


        for(int i=0 ; i<linLayouts.size();i++) {

            for (int j = 0; j < linLayouts.get(i).getChildCount(); j++) {


                TextView picker = null;
                String hora = "00:00";
                if (linLayouts.get(i).getChildAt(j) instanceof TextView) picker = (TextView) linLayouts.get(i).getChildAt(j);

                if (picker != null) {

                    picker.setOnClickListener(listener);


                }

            }
        }





        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button

                ArrayList<Object> allInputs = new ArrayList<Object>();
                String trama ="##V#";
                String tramaFormateada="##V#";
                ArrayList<LinearLayout> linLayouts = new ArrayList<LinearLayout>();

                // Get all linearLayouts inside our main relative layout

                for( int i = 0; i < mainLayout.getChildCount()-1; i++ ) {
                    if(mainLayout.getChildAt(i) instanceof LinearLayout) {
                        linLayouts.add((LinearLayout)mainLayout.getChildAt(i));
                    }

                }

                // Iterate through all elements inside each linear layout and check for checkboxes and edittexts

                for(int i=0 ; i<linLayouts.size();i++) {

                    for( int j = 0; j < linLayouts.get(i).getChildCount(); j++ ) {

                        if( linLayouts.get(i).getChildAt(j) instanceof CheckBox ) allInputs.add((CheckBox) linLayouts.get(i).getChildAt(j));

                        if (linLayouts.get(i).getChildAt(j) instanceof TextView)
                            allInputs.add((TextView) linLayouts.get(i).getChildAt(j));
                    }

                }



                Boolean checked = false;
                int checksCounter = 0;


                for(int i=0 ; i<allInputs.size(); i++) {

                    if (i==21) {
                        trama += "I#";
                        tramaFormateada += "I#";
                    }

                    if (allInputs.get(i) instanceof CheckBox) {
                        tramaFormateada+="-";
                        CheckBox check = (CheckBox) allInputs.get(i);
                        if (check.isChecked() == false) {
                            trama+="000000000";
                            tramaFormateada+="000000000";
                            i+=2;
                        } else {
                                checked=true;
                                /*trama += "1";
                                tramaFormateada += "1";*/
                        }
                    } else {

                        TextView tiempo = (TextView) allInputs.get(i);

                        if(tiempo.getId()+""=="veranoSeparador" || tiempo.getId()+"" == "inviernoSeparador") {
                            i++;
                            continue;
                        }

                        String subtrama = "";
                        subtrama+=tiempo.getText().toString().replaceAll("[^0-9]","");

                        if(checked && subtrama!="___") {
                            trama+="1";
                            tramaFormateada+="1";
                            checked=false;
                        }
                        trama+= subtrama;
                        tramaFormateada+= subtrama;

                    }
                }

                trama+="*##";
                tramaFormateada+="*##";


                if (trama.length() > 0) {
                    bluetoothSerial.write(trama, crlf);
                }

                // ALERT CON LA TRAMA Y TRAMA FORMATEADA PARA COMPROBAR


                AlertDialog alertDialog = new AlertDialog.Builder(TerminalActivity.this).create();
                alertDialog.setTitle("Alert");
                //alertDialog.setMessage("Trama enviada : "+trama +" \n Trama formateada para revisar : "+tramaFormateada);
                alertDialog.setMessage("Inputs capturados : " +allInputs); // todo Se repiten los checkboxes en el array con los TextView, investigar
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();




            }
        });



        // Create a new instance of BluetoothSerial
        bluetoothSerial = new BluetoothSerial(this, this);


    }

    public String guardar ( String hora , String minuto) {
        String resultado = hora+":"+minuto;
        String resultadoLimpio = resultado.replaceAll("[^0-9]","");
        return resultadoLimpio;
    }



    @Override
    protected void onStart() {
        super.onStart();

        // Check Bluetooth availability on the device and set up the Bluetooth adapter
        bluetoothSerial.setup();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Open a Bluetooth serial port and get ready to establish a connection
        if (bluetoothSerial.checkBluetooth() && bluetoothSerial.isBluetoothEnabled()) {
            if (!bluetoothSerial.isConnected()) {
                bluetoothSerial.start();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Disconnect from the remote device and close the serial port
        bluetoothSerial.stop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_terminal, menu);

        actionConnect = menu.findItem(R.id.action_connect);
        actionDisconnect = menu.findItem(R.id.action_disconnect);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_connect) {
            showDeviceListDialog();
            return true;
        } else if (id == R.id.action_disconnect) {
            bluetoothSerial.stop();
            return true;
        } else if (id == R.id.action_crlf) {
            crlf = !item.isChecked();
            item.setChecked(crlf);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void invalidateOptionsMenu() {
        if (bluetoothSerial == null)
            return;

        // Show or hide the "Connect" and "Disconnect" buttons on the app bar
        if (bluetoothSerial.isConnected()) {
            if (actionConnect != null)
                actionConnect.setVisible(false);
            if (actionDisconnect != null)
                actionDisconnect.setVisible(true);
        } else {
            if (actionConnect != null)
                actionConnect.setVisible(true);
            if (actionDisconnect != null)
                actionDisconnect.setVisible(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_ENABLE_BLUETOOTH:
                // Set up Bluetooth serial port when Bluetooth adapter is turned on
                if (resultCode == Activity.RESULT_OK) {
                    bluetoothSerial.setup();
                }
                break;
        }
    }

    private void updateBluetoothState() {
        // Get the current Bluetooth state
        final int state;
        if (bluetoothSerial != null)
            state = bluetoothSerial.getState();
        else
            state = BluetoothSerial.STATE_DISCONNECTED;

        // Display the current state on the app bar as the subtitle
        String subtitle;
        switch (state) {
            case BluetoothSerial.STATE_CONNECTING:
                subtitle = getString(R.string.status_connecting);
                break;
            case BluetoothSerial.STATE_CONNECTED:
                subtitle = getString(R.string.status_connected, bluetoothSerial.getConnectedDeviceName());
                break;
            default:
                subtitle = getString(R.string.status_disconnected);
                break;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(subtitle);
        }
    }

    private void showDeviceListDialog() {
        // Display dialog for selecting a remote Bluetooth device
        BluetoothDeviceListDialog dialog = new BluetoothDeviceListDialog(this);
        dialog.setOnDeviceSelectedListener(this);
        dialog.setTitle(R.string.paired_devices);
        dialog.setDevices(bluetoothSerial.getPairedDevices());
        dialog.showAddress(true);
        dialog.show();
    }

    /* Implementation of BluetoothSerialListener */

    @Override
    public void onBluetoothNotSupported() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.no_bluetooth)
                .setPositiveButton(R.string.action_quit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setCancelable(true)
                .show();
    }

    @Override
    public void onBluetoothDisabled() {
        Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBluetooth, REQUEST_ENABLE_BLUETOOTH);
    }

    @Override
    public void onBluetoothDeviceDisconnected() {
        invalidateOptionsMenu();
        updateBluetoothState();
    }

    @Override
    public void onConnectingBluetoothDevice() {
        updateBluetoothState();
    }

    @Override
    public void onBluetoothDeviceConnected(String name, String address) {
        invalidateOptionsMenu();
        updateBluetoothState();
    }

    @Override
    public void onBluetoothSerialRead(String message) {
        // Print the incoming message on the terminal screen

    }

    @Override
    public void onBluetoothSerialWrite(String message) {
        // Print the outgoing message on the terminal screen

    }

    /* Implementation of BluetoothDeviceListDialog.OnDeviceSelectedListener */

    @Override
    public void onBluetoothDeviceSelected(BluetoothDevice device) {
        // Connect to the selected remote Bluetooth device
        bluetoothSerial.connect(device);
    }

    /* End of the implementation of listeners */



}
