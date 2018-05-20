package com.nodomain.ivonne.snippet.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;

import com.nodomain.ivonne.snippet.R;
import com.nodomain.ivonne.snippet.espConfiguration.espManager;
import com.nodomain.ivonne.snippet.espConfiguration.snippetEditActivity;
import com.nodomain.ivonne.snippet.espConfiguration.snippetNewActivity;
import com.nodomain.ivonne.snippet.espConfiguration.sendToEsp;
import com.nodomain.ivonne.snippet.services.backgroundActivity;
import com.nodomain.ivonne.snippet.tools.dataManager;
import com.nodomain.ivonne.snippet.tools.auxiliarTools;
import com.nodomain.ivonne.snippet.objects.Device;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nodomain.ivonne.snippet.espConfiguration.espManager.ESP_CLOSER;
import static com.nodomain.ivonne.snippet.espConfiguration.espManager.ESP_CMD_STATUS;
import static com.nodomain.ivonne.snippet.espConfiguration.espManager.ESP_CMD_TOOGLE;
import static com.nodomain.ivonne.snippet.espConfiguration.espManager.ESP_FOO;
import static com.nodomain.ivonne.snippet.espConfiguration.espManager.ESP_IMAGE;
import static com.nodomain.ivonne.snippet.espConfiguration.espManager.ESP_MAC;
import static com.nodomain.ivonne.snippet.espConfiguration.espManager.ESP_NAME;
import static com.nodomain.ivonne.snippet.espConfiguration.espManager.ESP_PSW;
import static com.nodomain.ivonne.snippet.espConfiguration.espManager.ESP_STATUS;
import static com.nodomain.ivonne.snippet.espConfiguration.espManager.ESP_TYPE;
import static com.nodomain.ivonne.snippet.espConfiguration.espManager.LIGHT_BULB;
import static com.nodomain.ivonne.snippet.espConfiguration.espManager.LIGHT_DIMMER;
import static com.nodomain.ivonne.snippet.espConfiguration.snippetNewActivity.ACTION;
import static com.nodomain.ivonne.snippet.espConfiguration.snippetNewActivity.PARAM1;
import static com.nodomain.ivonne.snippet.espConfiguration.snippetNewActivity.PARAM2;

//TODO: Iniciar actividad de fondo que monitorea constantemente el estado de los focos y reportará
//a actividad "desplegar" si es que esta abierta

public class showDevicesActivity extends AppCompatActivity implements
        AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener,
        Spinner.OnItemSelectedListener {
    static final String TAG = "SHOW_DEV";

    public static final String CORRECT_IP = "CORRECT_IP";
    static final int CORRECT_IP_CODE = 51;

    static final String RECOVER_LIST = "RECOVER";
    static final String INIT_LIST = "INIT";
    static final String UPDATE_LIST = "UPDATE";
    static final String UPDATE_CONNECTION = "CONNECTION";
    static final String MAIN_VIEW = "MAIN";
    static final String SECOND_VIEW = "SECOND";
    static final String NEW_DEVICE = "NEW";

    static final int EDIT_DEV_CODE = 21;
    static final int DIMMER_ACTIVITY_CODE = 22;

    private SharedPreferences settings;
    private ListView listView;
    private GridView squareView;
    protected Bundle storedImages;

    private int mainViewSelected;
    private int secondViewSelected;
    private String device;
    private String network;
    private List<Map<String, String>> deviceList = null;
    private boolean longClick = false;
    private auxiliarTools myAuxFunction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_devices);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listView = (ListView)findViewById(R.id.view_list);
        squareView = (GridView)findViewById(R.id.view_square);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(showDevicesActivity.this, snippetNewActivity.class), MainActivity.NEW_DEVICE_CODE);
            }
        });

        settings = getSharedPreferences(MainActivity.SHARED_PREF, 0);
        secondViewSelected = settings.getInt(SECOND_VIEW, 0);
        mainViewSelected = settings.getInt(MAIN_VIEW, 0);

        Spinner spinner = (Spinner)findViewById(R.id.spinner_secondary);
        spinner.setSelection(secondViewSelected);
        spinner.setOnItemSelectedListener(this);

        device = getIntent().getStringExtra(MainActivity.DEVICE);
        network = getIntent().getStringExtra(MainActivity.NETWORK);

        setTitle(device);

        myAuxFunction = new auxiliarTools();

        if (savedInstanceState != null) {
            storedImages = savedInstanceState;
            feedList(RECOVER_LIST);
            feedList(UPDATE_CONNECTION);
        }
        else
            feedList(INIT_LIST);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        if (!longClick) {
            final ImageView imagen = (ImageView) view.findViewById(R.id.image_device);
            String tipo = deviceList.get(position).get(ESP_TYPE);
            sendToEsp conectarESP = new sendToEsp(getApplicationContext(), new sendToEsp.AsyncResponse() {
                @Override
                public void processFinish(String output) {
                    changeImage(imagen, position, output);
                }
            });
            switch (tipo) {
                case LIGHT_BULB: {
                    conectarESP.execute(ESP_CMD_TOOGLE+deviceList.get(position).get(ESP_PSW)+
                            ESP_CLOSER, deviceList.get(position).get(ESP_MAC),
                            deviceList.get(position).get(ESP_FOO));
                    break;
                }
                case LIGHT_DIMMER: {
                    Intent intent = new Intent(showDevicesActivity.this, dimmerActivity.class);
                    intent.putExtra(ESP_MAC, deviceList.get(position).get(ESP_MAC));
                    intent.putExtra(ESP_FOO, deviceList.get(position).get(ESP_FOO));
                    intent.putExtra(ESP_NAME, deviceList.get(position).get(ESP_NAME));
                    intent.putExtra(ESP_PSW, deviceList.get(position).get(ESP_PSW));
                    startActivityForResult(intent, DIMMER_ACTIVITY_CODE);
                    break;
                }
            }
        }
        longClick = false;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        if (Build.VERSION.SDK_INT >= 26) {
            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect
                    .createOneShot(50,VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(50);
        }
        longClick = true;
        new AlertDialog.Builder(showDevicesActivity.this, R.style.myDialog)
                .setMessage(getResources().getString(R.string.pregunta_editar)+" "+deviceList.get(position)
                        .get(ESP_NAME)+"?").setNegativeButton(getResources().getString(R.string.borrar), new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int id) {
                        new dataManager(showDevicesActivity.this).delete(deviceList.get(position)
                                .get(ESP_MAC));
                        deviceList.remove(position);
                        feedList(UPDATE_LIST);
                        setResult(RESULT_OK);
                    }})
                .setPositiveButton(getResources().getString(R.string.configurar), new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(showDevicesActivity.this, snippetEditActivity.class);
                        intent.putExtra(ESP_MAC,deviceList.get(position).get(ESP_MAC));
                        startActivityForResult(intent, EDIT_DEV_CODE);
                    }})
                .show();
        return false;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position != secondViewSelected){
            secondViewSelected = position;
            settings.edit().putInt(SECOND_VIEW, secondViewSelected).apply();
            feedList(UPDATE_LIST);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

    private void feedList(final String action){
        new AsyncTask<Void, Void, Void>(){
            dataManager datamanager = new dataManager(showDevicesActivity.this);
            SimpleAdapter showDevAdapter;

            @Override
            protected void onPreExecute(){
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
                //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
            }

            @Override
            protected Void doInBackground(Void ...params) {
                String[] values = action.split(",");
                switch (values[0]){
                    case NEW_DEVICE: {
                        addNew(values[1]);
                        break;
                    }
                    case UPDATE_LIST: {//solo manda a actualizar el adaptador por si se cambio la vista o si se borro un elemento
                        break;
                    }
                    case UPDATE_CONNECTION: {
                        updateStatus();
                        break;
                    }
                    case RECOVER_LIST: {
                        initList(true);//hay valores almacenados en imagenes_almacenadas
                        break;
                    }
                    default: {
                        initList(false);//NO hay valores almacenados en imagenes_almacenadas
                        break;
                    }
                }
                return null;
            }

            private void addNew(String devMac){
                Device newDevice = datamanager.getDeviceByMac(devMac);
                if (newDevice.getDevType().equals(device) || newDevice.getDevHouseSpace().equals(device)) {
                    int position = deviceList.indexOf(devMac);
                    if (position == -1)//dispositivo no existe en la lista, lo que quiere decir que es nuevo
                        position = deviceList.size();
                    Map<String, String> datum = new HashMap<>(2);
                    datum.put(ESP_NAME, newDevice.getDevName());
                    datum.put(ESP_MAC, newDevice.getDevMac());
                    datum.put(ESP_FOO, newDevice.getDevFoo());
                    datum.put(ESP_PSW, newDevice.getDevPsw());
                    datum.put(ESP_TYPE, newDevice.getDevType());
                    datum.put(ESP_IMAGE, newDevice.getDevImage());
                    datum.put(ESP_STATUS, getDefaultImage(newDevice.getDevImage()));
                    deviceList.add(position, datum);
                    requestStatus(datum, position);//ubicacion del elemento que se esta reescribiendo
                }
            }

            private void initList(boolean stored){
                List<Device> devices;
                switch (mainViewSelected){
                    case 0:{//view by spaces
                        devices = datamanager.getDevicesBySpaces(network, device);
                        break;
                    }
                    default:{
                        devices = datamanager.getDevicesByType(network, device);
                        break;
                    }
                }
                deviceList = new ArrayList<>();
                if (devices.size() > 0) {
                    for (int i = 0; i < devices.size(); i++) {
                        String validFoo = new espManager(getApplicationContext())
                                    .getValidFoo(devices.get(i).getDevMac(),
                                    devices.get(i).getDevFoo());
                        //if (validFoo.equals("ERROR") || validFoo.equals("") || validFoo.equals("0p0p0p0"))
                        //    scanForFoo(devices.get(i).getDevMac());
                        //else
                        if (!validFoo.equals(devices.get(i).getDevFoo()))
                            datamanager.updateIP(devices.get(i).getDevMac(),validFoo);
                        Map<String, String> datum = new HashMap<>(2);
                        datum.put(ESP_NAME, devices.get(i).getDevName());
                        datum.put(ESP_MAC, devices.get(i).getDevMac());
                        datum.put(ESP_FOO, validFoo);
                        datum.put(ESP_PSW, devices.get(i).getDevPsw());
                        datum.put(ESP_TYPE, devices.get(i).getDevType());
                        datum.put(ESP_IMAGE, devices.get(i).getDevImage());
                        datum.put(ESP_STATUS, getDefaultImage(devices.get(i).getDevImage()));
                        deviceList.add(i,datum);
                        if (stored)
                            datum.put(ESP_STATUS, recoverStored(devices.get(i).getDevMac(),
                                    devices.get(i).getDevImage()));
                        requestStatus(datum, i);
                    }
                }
            }

            private void updateStatus(){
                for (int i = 0; i < deviceList.size(); i++) {
                    Map<String, String> datum = new HashMap<>(2);
                    datum.put(ESP_MAC, deviceList.get(i).get(ESP_MAC));
                    datum.put(ESP_FOO, deviceList.get(i).get(ESP_FOO));
                    datum.put(ESP_IMAGE, deviceList.get(i).get(ESP_IMAGE));
                    datum.put(ESP_STATUS, getDefaultImage(deviceList.get(i).get(ESP_IMAGE)));
                    requestStatus(datum, i);
                }
            }

            private String getDefaultImage(String imagePrefix){
                return String.valueOf(getApplicationContext().getResources()
                        .getIdentifier(imagePrefix+"_desconectado", "drawable",//TODO:disconnected/desconectado
                                getApplicationContext().getPackageName()));
            }

            private void requestStatus(Map<String, String> myDevice, final int position){
                Log.w(TAG,"request status");
                String validFoo = new espManager(getApplicationContext())
                        .getValidFoo(myDevice.get(ESP_MAC),myDevice.get(ESP_FOO));
                //if (validFoo.equals("ERROR"))
                //    scanForFoo(myDevice.get(ESP_MAC));
                //else
                if (!validFoo.equals(myDevice.get(ESP_FOO)))
                    datamanager.updateIP(myDevice.get(ESP_MAC),validFoo);
                final String imagePrefix = myDevice.get(ESP_IMAGE);
                sendToEsp mySendToEsp = new sendToEsp(getApplicationContext(), new sendToEsp.AsyncResponse() {
                    @Override
                    public void processFinish(String output) {
                        String value[]= output.split(",");
                        switch (value[0]){
                            case "ON":{
                                int thisImage = getApplicationContext().getResources()
                                        .getIdentifier(imagePrefix+"_on", "drawable",
                                                getApplicationContext().getPackageName());
                                deviceList.get(position).put(ESP_STATUS,String.valueOf(thisImage));
                                if (showDevAdapter != null)
                                    showDevAdapter.notifyDataSetChanged();
                                break;
                            }
                            case "OFF":{
                                int thisImage = getApplicationContext().getResources()
                                        .getIdentifier(imagePrefix+"_off", "drawable",
                                                getApplicationContext().getPackageName());
                                deviceList.get(position).put(ESP_STATUS,String.valueOf(thisImage));
                                if (showDevAdapter != null)
                                    showDevAdapter.notifyDataSetChanged();
                                break;
                            }
                        }
                    }
                });
                mySendToEsp.execute(ESP_CMD_STATUS+myDevice.get(ESP_PSW)+ESP_CLOSER, myDevice.get(ESP_MAC),
                        myDevice.get(ESP_FOO));
            }

            private String recoverStored(String myMac, String imagePrefix){
                String image = storedImages.getString(myMac + "_image");
                if (image == null) {
                    image = getDefaultImage(imagePrefix);
                }
                else if (image.equals("")) {
                    image = getDefaultImage(imagePrefix);
                }
                return image;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                if (secondViewSelected == 0) {//Listview
                    showDevAdapter = new SimpleAdapter(showDevicesActivity.this, deviceList,
                            R.layout.list_view, new String[]{ESP_NAME, ESP_STATUS},
                            new int[]{R.id.text_name, R.id.image_device});
                    listView.setAdapter(showDevAdapter);
                    listView.setOnItemClickListener(showDevicesActivity.this);
                    listView.setOnItemLongClickListener(showDevicesActivity.this);
                    squareView.setVisibility(View.INVISIBLE);
                    listView.setVisibility(View.VISIBLE);
                }
                else{
                    showDevAdapter = new SimpleAdapter(showDevicesActivity.this, deviceList,
                            R.layout.square_view, new String[]{ESP_NAME, ESP_STATUS},
                            new int[]{R.id.text_name, R.id.image_device});
                    squareView.setAdapter(showDevAdapter);
                    squareView.setOnItemClickListener(showDevicesActivity.this);
                    squareView.setOnItemLongClickListener(showDevicesActivity.this);
                    listView.setVisibility(View.INVISIBLE);
                    squareView.setVisibility(View.VISIBLE);
                }
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }
        }.execute();
    }

    private void changeImage(ImageView image, int position, String state){
        String imagePrefix = deviceList.get(position).get(ESP_IMAGE);
        int idImage = getApplicationContext().getResources()
                .getIdentifier(imagePrefix+"_desconectado", "drawable", getApplicationContext()
                        .getPackageName());
        switch (state) {
            case "ON": {
                idImage = getApplicationContext().getResources()
                        .getIdentifier(imagePrefix+"_on", "drawable", getApplicationContext()
                                .getPackageName());
                break;
            }
            case "OFF": {
                idImage = getApplicationContext().getResources()
                        .getIdentifier(imagePrefix+"_off", "drawable", getApplicationContext()
                                .getPackageName());
                break;
            }
        }
        image.setImageResource(idImage);
        deviceList.get(position).put(ESP_STATUS, String.valueOf(idImage));
    }

    private void scanForFoo(String devMac) {
        Intent intent = new Intent(showDevicesActivity.this, backgroundActivity.class);
        intent.putExtra(ACTION, CORRECT_IP);
        intent.putExtra(PARAM1, devMac);
        startActivityForResult(intent, CORRECT_IP_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode){
                case MainActivity.NEW_DEVICE_CODE: {
                    feedList(NEW_DEVICE + "," + data.getStringExtra(ESP_MAC));
                    setResult(RESULT_OK);//notifica a la actividad anterior que algo se modifico
                    break;
                }
                case EDIT_DEV_CODE: {
                    feedList(INIT_LIST);
                    setResult(RESULT_OK);
                    break;
                }
                case DIMMER_ACTIVITY_CODE: {
                    feedList(INIT_LIST);
                    setResult(RESULT_OK);//notifica a la actividad anterior que algo se modifico
                    break;
                }
                case CORRECT_IP_CODE: {//CORREGIR_IP TODO: o se atrasa su inicio, o se hace 2 veces
                    String snippetFoo = data.getStringExtra(PARAM1);
                    String snippetMac = data.getStringExtra(PARAM2);
                    new dataManager(getApplicationContext()).updateIP(snippetMac,snippetFoo);
                    //feedList(UPDATE_CONNECTION);
                    break;
                }
                default:
                    break;
            }
        }
        // siempre actualizar state de todos los ESP al regresar a esta vista
        feedList(UPDATE_CONNECTION);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        if (deviceList != null)
            for (int i = 0; i < deviceList.size(); i++){
                outState.putString(deviceList.get(i).get(ESP_MAC)+"_Imagen",deviceList.get(i).get(ESP_STATUS));
            }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
        }
    }

}

/*
Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
 */