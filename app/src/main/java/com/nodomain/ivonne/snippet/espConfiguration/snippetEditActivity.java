package com.nodomain.ivonne.snippet.espConfiguration;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.nodomain.ivonne.snippet.objects.Device;
import com.nodomain.ivonne.snippet.R;
import com.nodomain.ivonne.snippet.tools.dataManager;
import com.nodomain.ivonne.snippet.adapters.horizontalNumberPicker;
import com.nodomain.ivonne.snippet.services.monitorWiFi;
import com.nodomain.ivonne.snippet.app.timerActivity;

import static com.nodomain.ivonne.snippet.app.MainActivity.MONITOR_WIFI;
import static com.nodomain.ivonne.snippet.espConfiguration.espManager.ESP_FOO;
import static com.nodomain.ivonne.snippet.espConfiguration.espManager.ESP_MAC;
import static com.nodomain.ivonne.snippet.espConfiguration.espManager.ESP_TYPE;
import static com.nodomain.ivonne.snippet.espConfiguration.snippetSaveActivity.SELECT_IMAGE_CODE;

public class snippetEditActivity extends AppCompatActivity implements View.OnClickListener, Spinner.OnItemSelectedListener, TextWatcher{

    private EditText deviecName;
    private EditText devicePsw;
    private CheckBox editPsw;
    private CheckBox advancedConfiguration;
    private Spinner devHomeSpace;
    private ImageView deviceImage;
    private Button buttonSave;
    private Button buttonCancel;
    private Button buttonTimer;
    private LinearLayout layoutAdvanced;
    private Switch autoOff;
    private Switch autoOn;
    private horizontalNumberPicker timeSelector;

    private Device myDevice;
    private int position;
    private String newImge;
    private String[] spacesList;
    private String oldPsw;
    private dataManager myDM;
    private Boolean setOff = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_snippet);
        deviecName = (EditText) findViewById(R.id.text_name);
        devicePsw = (EditText) findViewById(R.id.text_psw);
        editPsw = (CheckBox) findViewById(R.id.edit_psw);
        advancedConfiguration = (CheckBox) findViewById(R.id.checkbox_advanced);
        devHomeSpace = (Spinner) findViewById(R.id.text_space);
        deviceImage = (ImageView) findViewById(R.id.image_device);
        buttonSave = (Button)findViewById(R.id.button_save);
        buttonCancel = (Button)findViewById(R.id.button_cancel);
        buttonTimer = (Button) findViewById(R.id.botonTimer);
        layoutAdvanced = (LinearLayout) findViewById( (R.id.layout_advanced));
        autoOff = (Switch) findViewById(R.id.switch_auto_off);
        autoOn = (Switch) findViewById(R.id.switch_auto_on);
        timeSelector = (horizontalNumberPicker) findViewById(R.id.picker_time);

        String deviceMac = getIntent().getStringExtra(ESP_MAC);
        recoverData(deviceMac);

        setTitle(getResources().getString(R.string.configurando)+" "+ myDevice.getDevName());

        devHomeSpace.setOnItemSelectedListener(this);
        deviceImage.setOnClickListener(this);
        buttonSave.setOnClickListener(this);
        buttonCancel.setOnClickListener(this);
        editPsw.setOnClickListener(this);
        advancedConfiguration.setOnClickListener(this);
        autoOff.setOnClickListener(this);
        autoOn.setOnClickListener(this);
        buttonTimer.setOnClickListener(this);
        deviecName.addTextChangedListener(this);
        devicePsw.addTextChangedListener(this);
    }

    private void recoverData(String mac){
        myDM = new dataManager(snippetEditActivity.this);
        myDevice = myDM.getDeviceByMac(mac);
        deviecName.setText(myDevice.getDevName());
        deviecName.setSelection(myDevice.getDevName().length());
        devicePsw.setText(myDevice.getDevPsw());
        oldPsw = myDevice.getDevPsw();
        devicePsw.setEnabled(false);
        newImge = myDevice.getDevImage();

        spacesList = getResources().getStringArray(R.array.ambientes);
        for (int i = 0; i < spacesList.length; i++){
            if (spacesList[i].equals(myDevice.getDevHouseSpace()))
                position = i;
        }
        devHomeSpace.setSelection(position);

        int resID = getApplicationContext().getResources().getIdentifier(myDevice.getDevImage(), "drawable", getApplicationContext().getPackageName());
        deviceImage.setImageResource(resID);

        if (myDevice.getDevOn().equals("true"))
            autoOn.setChecked(true);

        /*if (myDevice.setOff.equals("true")) {
            autoOff.setActivated(true);
            timeSelector.setValue(Integer.parseInt(myDevice.tiempo));
        }*/

    }

    @Override
    public void onClick(View view) {
        if(view.getId() == editPsw.getId()){
            if (editPsw.isChecked()){
                devicePsw.setText("");
                devicePsw.setEnabled(true);
            }
            else{
                devicePsw.setText(myDevice.getDevPsw());
                devicePsw.setEnabled(false);
            }
        }
        else if(view.getId() == advancedConfiguration.getId()){
            if (advancedConfiguration.isChecked())
                layoutAdvanced.setVisibility(View.VISIBLE);
            else
                layoutAdvanced.setVisibility(View.GONE);
        }
        else if(view.getId() == autoOff.getId()){
            setOff = autoOff.isChecked();
            timeSelector.setEnabled(autoOff.isChecked());
            buttonSave.setEnabled(true);
        }
        else if(view.getId() == autoOn.getId()){
            buttonSave.setEnabled(true);
        }
        else if (view.getId() == deviceImage.getId()){
            Intent getImagen = new Intent(snippetEditActivity.this,selectImageActivity.class);
            getImagen.putExtra(ESP_TYPE, myDevice.getDevType());
            getImagen.putExtra("ACTUAL", newImge);
            startActivityForResult(getImagen,SELECT_IMAGE_CODE);
        }
        else if (view.getId() == buttonTimer.getId()){
            Intent intent = new Intent(snippetEditActivity.this, timerActivity.class);
            intent.putExtra(ESP_MAC, myDevice.getDevMac());
            intent.putExtra(ESP_FOO, myDevice.getDevFoo());
            startActivity(intent);
        }
        else if (view.getId() == buttonSave.getId()){
            aplicarcambios();
        }
        else if (view.getId() == buttonCancel.getId()){
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    public void aplicarcambios(){
        if (!devicePsw.getText().toString().equals(myDevice.getDevPsw()))
            enviarCambiosESP("password");
        else if (!deviecName.getText().toString().equals(myDevice.getDevName()))
            enviarCambiosESP("nombre");
        else if (setOff){
            setOff = false;
            sendToEsp conectarESP = new sendToEsp(getApplicationContext(), new sendToEsp.AsyncResponse() {
                @Override
                public void processFinish(String output) {
                    String value[]= output.split(",");
                    switch (value[0]){
                        case "ON":{
                            enviarCambiosESP("setOff");
                            break;
                        }
                        case "OFF":{
                            //MENSAJE de que deberia estar encendido el foco
                            break;
                        }
                    }
                }
            });
            conectarESP.execute("ESP8266,7STATUS", myDevice.getDevMac(), myDevice.getDevFoo());
        }
        else if (!myDevice.getDevOn().equals(String.valueOf(autoOn.isChecked()))){
            myDevice.setDevOn(String.valueOf(autoOn.isChecked()));
            aplicarcambios();
            //TODO: FUNCION QUE manda a encender el foco al detectar la red wifi
        }
        else if (!myDevice.getDevImage().equals(newImge)){
            myDevice.setDevImage(newImge);
            aplicarcambios();
        }
        else if (!myDevice.getDevHouseSpace().equals(spacesList[position])){
            myDevice.setDevHouseSpace(spacesList[position]);
            aplicarcambios();
        }
        else if(!myDevice.getDevOn().equals(Boolean.toString(autoOn.isChecked()))){
            myDevice.setDevOn(Boolean.toString(autoOn.isChecked()));
            if (autoOn.isChecked()){
                if (!MONITOR_WIFI){
                    Intent WiFiService = new Intent(snippetEditActivity.this, monitorWiFi.class);
                    startService(WiFiService);
                    MONITOR_WIFI = true;
                }
            }
            Log.w("EDITAR_S", "encendido automatico: "+Boolean.toString(autoOn.isChecked()));
            aplicarcambios();
        }
        else{
            myDM.addorUpdateDevice(myDevice);
            buttonSave.setEnabled(false);
            setResult(RESULT_OK);//algo se modifico
            finish();
        }
    }

    private void enviarCambiosESP(String cambio){
        final sendToEsp enviarCambios = new sendToEsp(snippetEditActivity.this, new sendToEsp.AsyncResponse() {
            @Override
            public void processFinish(String output) {
                String[] valores = output.split(",");
                switch (valores[0]){
                    case "ERROR":{
                        if (!devicePsw.getText().toString().equals(oldPsw)) {
                            oldPsw = devicePsw.getText().toString();
                            enviarCambiosESP("password");
                        } else {
                            Toast.makeText(snippetEditActivity.this, getResources().getString(R.string.error7), Toast.LENGTH_SHORT).show();
                            buttonSave.setEnabled(false);
                        }
                        break;
                    }
                    case "CORRECTO":{
                        if (valores[1].equals("PASSWORD")) {
                            myDevice.setDevPsw(devicePsw.getText().toString());
                            oldPsw = myDevice.getDevPsw();
                        }
                        else if (valores[1].equals("NOMBRE"))
                            myDevice.setDevName(deviecName.getText().toString());
                        aplicarcambios();
                        break;
                    }
                }
            }
        });
        switch (cambio){
            case "nombre":{
                enviarCambios.execute("ESP8266,49\n10"+ deviecName.getText().toString()+"10\n1115\n16"+ oldPsw +"16\n17", myDevice.getDevMac(), myDevice.getDevFoo());//FIXME: 4 cambiar nombre
                break;
            }
            case "password":{
                enviarCambios.execute("ESP8266,59\n10"+ devicePsw.getText()
                        .toString()+"10\n1115\n16"+ oldPsw +"16\n17",
                        myDevice.getDevMac(), myDevice.getDevFoo());//FIXME: 5 cambiar password
                break;
            }
            case "setOff":{
                WifiManager wifiManager = (WifiManager)getApplicationContext()
                        .getSystemService(Context.WIFI_SERVICE);
                int esteAndroid = wifiManager.getDhcpInfo().ipAddress;
                new sendToEsp().execute("ESP8266,<13\n14"+getStringFromIP(esteAndroid)+
                        "14\n1515\n16"+ oldPsw +"16\n17", myDevice.getDevMac(),
                        myDevice.getDevFoo());//FIXME: < (12) configurar setOff automatico
                //TODO: si el usuario selecciono que sea permanente, guardarlo en la base de datos
                aplicarcambios();
                break;
            }
        }
    }

    @Override public void beforeTextChanged(CharSequence var1, int var2, int var3, int var4){}
    @Override public void onTextChanged(CharSequence var1, int var2, int var3, int var4){}

    @Override
    public void afterTextChanged(Editable var1){
        if (deviecName.getText().toString().equals(""))
            buttonSave.setEnabled(false);
        else
            buttonSave.setEnabled(true);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position != this.position) {
            buttonSave.setEnabled(true);
            this.position = position;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_IMAGE_CODE){
            if (resultCode == RESULT_OK) {
                newImge = data.getStringExtra("IMAGEN");
                if (!myDevice.getDevImage().equals(newImge)) {
                    deviceImage.setImageResource(getApplicationContext().getResources().getIdentifier(newImge, "drawable", getApplicationContext().getPackageName()));
                    buttonSave.setEnabled(true);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private static String getStringFromIP(int i) {
        //int i = Integer.reverse(ip_reverse);
        return  ((i & 0xFF) + "." +
                ((i >>> 8) & 0xFF) + "." +
                ((i >>> 16) & 0xFF) + "." +
                ((i >>> 24) & 0xFF));
    }
}
