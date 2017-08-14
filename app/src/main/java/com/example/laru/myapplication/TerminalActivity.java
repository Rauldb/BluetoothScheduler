package com.example.laru.myapplication;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Button;
import android.view.View;
import android.widget.TimePicker;
import java.text.DecimalFormat;
import java.util.Calendar;
import android.app.TimePickerDialog;
import android.view.View.OnClickListener;
import java.text.SimpleDateFormat;
import android.os.Handler;




import com.macroyau.blue2serial.BluetoothDeviceListDialog;
import com.macroyau.blue2serial.BluetoothSerial;
import com.macroyau.blue2serial.BluetoothSerialListener;


import java.util.ArrayList;
import java.util.Locale;

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
    private ArrayList<Object> allInputs = new ArrayList<Object>();
    private ArrayList<LinearLayout> linLayouts = new ArrayList<LinearLayout>();
    private ArrayList<TextView> timeInputs = new ArrayList<TextView>();
    private ArrayList<CheckBox> checkBoxes = new ArrayList<CheckBox>();

    private int datacount = 0;
    String trama ="";
    final ArrayList<String> datos = new ArrayList<>();


    final Handler handler = new Handler();
    public Runnable sincronizaHora = new Runnable()
    {

        @Override
        public void run()
        {
            // type code here which will loop with the given delay



            //if you want to end the runnable type this in the condition
            if(datacount==4) {

                handler.removeCallbacks(this);
                datacount=0;
                return;

            }
            //Log.i("cuenta: ", datos.get(datacount));
            bluetoothSerial.write(datos.get(datacount) , false);
            datacount++;
            //delay for the runnable
            handler.postDelayed(sincronizaHora, 2000);
        }
    };

    public Runnable enviaHorario = new Runnable()
    {

        @Override
        public void run()
        {
            switch (datacount){
                case 0:
                    //Log.i("Hola", "esto es un salto de linea\n");
                    bluetoothSerial.write("*#P" , false);
                    datacount++;
                    break;
                case 1:
                    //Log.i("trama: ", trama);
                    bluetoothSerial.write(trama, false);
                    datacount++;
                    break;
                case 2:
                    handler.removeCallbacks(this);
                    datacount = 0;
                    return;
            }
            handler.postDelayed(enviaHorario, 2000);
        }
    };

     private void saveTimeState() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        for(int i = 0 ; i<timeInputs.size() ; i++) {
            editor.putString(""+timeInputs.get(i).getId() , timeInputs.get(i).getText().toString());
        }
        for (int i = 0 ; i<checkBoxes.size() ; i++){
            String id = ""+checkBoxes.get(i).getId();
            editor.putBoolean( id , checkBoxes.get(i).isChecked() );
            //Log.i("guardacheck"+i+":" , Boolean.toString(checkBoxes.get(i).isChecked()));

        }

        editor.apply();
    }

    private void loadState () {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("prefs", MODE_PRIVATE);
        for(int i = 0 ; i<timeInputs.size() ; i++) {
            String id = ""+timeInputs.get(i).getId();
            timeInputs.get(i).setText(pref.getString( id , "___"));
        }


        for (int i = 0 ; i<checkBoxes.size() ; i++){
            String id = ""+checkBoxes.get(i).getId();
            checkBoxes.get(i).setChecked(pref.getBoolean( id , true));
            //Log.i("checkbox"+i+": " , Boolean.toString(pref.getBoolean(""+checkBoxes.get(i).getId() , false)));


        }
    }



    private void getInputs() {

        timeInputs.clear();
        checkBoxes.clear();
        linLayouts.clear();

        for( int i = 0; i < mainLayout.getChildCount()-1; i++ ) {
            if(mainLayout.getChildAt(i) instanceof LinearLayout) {
                linLayouts.add((LinearLayout)mainLayout.getChildAt(i));
            }

        }

        // Iterate through all elements inside linearlayout and set listeners only on timepickers

        // Get timepickers

        for(int i=0 ; i<linLayouts.size();i++) {

            for (int j = 0; j < linLayouts.get(i).getChildCount(); j++) {


                TextView picker = null;
                if (linLayouts.get(i).getChildAt(j) instanceof TextView) picker = (TextView) linLayouts.get(i).getChildAt(j);


                if (picker != null) {

                    if(!(picker instanceof CheckBox) && picker.getText().length()<6){
                        picker.setOnClickListener(timePickListener);
                        timeInputs.add(picker);
                    }

                    if(picker instanceof CheckBox) {
                        picker.setOnClickListener(checkBoxListener);
                    }


                }

            }
        }




        // Get checkboxes



        // Iterate through all elements inside each linear layout and check for checkboxes and edittexts

        for(int i=0 ; i<linLayouts.size();i++) {

            for( int j = 0; j < linLayouts.get(i).getChildCount(); j++ ) {

                if( linLayouts.get(i).getChildAt(j) instanceof CheckBox ) checkBoxes.add((CheckBox) linLayouts.get(i).getChildAt(j));


            }

        }




    }







    private MenuItem actionConnect, actionDisconnect;

    private OnClickListener checkBoxListener = new OnClickListener() {
        @Override
        public void onClick(View view) {

            SharedPreferences pref = getApplicationContext().getSharedPreferences("prefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();

            CheckBox check = (CheckBox) view;
            String id = ""+check.getId();

            editor.putBoolean( id , check.isChecked() );
            //Log.i(id+":" , Boolean.toString(check.isChecked()));

            editor.apply();

        }
    };



    private OnClickListener timePickListener =   new OnClickListener() {
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
                    saveTimeState();
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
        SharedPreferences pref = getApplicationContext().getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();


        // Find UI views and set listeners

        sendButton = (Button) findViewById(R.id.botonEnviar);
        mainLayout = (RelativeLayout) findViewById(R.id.mainLayout);
        getInputs();


        //ArrayList<Object> allInputs = new ArrayList<Object>();
        //ArrayList<LinearLayout> linLayouts = new ArrayList<LinearLayout>();

        // Get all linearLayouts inside our main relative layout






        loadState();








        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button

                //ArrayList<Object> allInputs = new ArrayList<Object>();
                trama="";
                String tramaFormateada="*#P\n";
                CheckBox currentCheck = null;

                getInputs();



                for(int i=0 ; i<timeInputs.size(); i++) {


                    if (i==0 || i%2 == 0) {
                        if (i == 0) {
                            currentCheck = checkBoxes.get(0);
                        } else {
                            currentCheck = checkBoxes.get(i/2);
                        }
                        tramaFormateada+="-";
                        if(currentCheck.isChecked() == false) {
                            trama+="000000000";
                            tramaFormateada+="000000000";
                            i+=1;
                        } else {
                            trama+="1";
                            tramaFormateada+="1";
                        }
                    }

                        TextView tiempo = (TextView) timeInputs.get(i);
                        String subtrama = "";
                        subtrama+=tiempo.getText().toString().replaceAll("[^0-9]","");

                        if(currentCheck.isChecked() && subtrama!="") {
                            trama+= subtrama;
                            tramaFormateada+= subtrama;
                        }
                        if(currentCheck.isChecked() && subtrama=="") {
                            trama+="0000";
                            tramaFormateada+="0000";
                        }


                    }


                trama+="\r";
                tramaFormateada+="\r";




                if (trama.length() > 0) {
                    handler.post(enviaHorario);
                }

                // ALERT CON LA TRAMA Y TRAMA FORMATEADA PARA COMPROBAR


                //Log.i("Trama enviada :", ""+trama +" \n Trama formateada para revisar : "+tramaFormateada);





            }
        });






        // Create a new instance of BluetoothSerial
        bluetoothSerial = new BluetoothSerial(this, this);


    }

    private String guardar ( String hora , String minuto) {
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
        }else if (id == R.id.action_setTime){

            Calendar mcurrentTime = Calendar.getInstance(Locale.FRANCE);
            int weekDay = mcurrentTime.get(Calendar.DAY_OF_WEEK);
            int fixedDay;
            if(weekDay == 1) {
                fixedDay = 7;
            } else {
                fixedDay = weekDay -1;
            }
            SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyHHmm");
            SimpleDateFormat formatoFecha = new SimpleDateFormat("ddMMyy");
            SimpleDateFormat formatoHora = new SimpleDateFormat("HHmmss");
            String fecha = "0"+fixedDay+formatoFecha.format(mcurrentTime.getTime());
            String hora = formatoHora.format(mcurrentTime.getTime());
            datos.add("*#D");
            datos.add(fecha);
            datos.add("*#T");
            datos.add(hora);

            handler.post(sincronizaHora);


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
                .setCancelable(false)
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
