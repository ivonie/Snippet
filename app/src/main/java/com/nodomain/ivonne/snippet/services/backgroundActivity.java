package com.nodomain.ivonne.snippet.services;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.nodomain.ivonne.snippet.R;
import com.nodomain.ivonne.snippet.espConfiguration.espManager;
import com.nodomain.ivonne.snippet.espConfiguration.sendToEsp;
import com.nodomain.ivonne.snippet.tools.auxiliarTools;

import java.util.List;

import static com.nodomain.ivonne.snippet.app.MainActivity.SCAN_NEW;
import static com.nodomain.ivonne.snippet.app.showDevicesActivity.CORRECT_IP;
import static com.nodomain.ivonne.snippet.espConfiguration.espManager.ESP_AP_SSID;
import static com.nodomain.ivonne.snippet.espConfiguration.espManager.ESP_CLOSER;
import static com.nodomain.ivonne.snippet.espConfiguration.espManager.ESP_CMD_ESTADO;
import static com.nodomain.ivonne.snippet.espConfiguration.espManager.ESP_CMD_MAC;
import static com.nodomain.ivonne.snippet.espConfiguration.espManager.ESP_PSW;
import static com.nodomain.ivonne.snippet.espConfiguration.espManager.ESP_RES_CONNECTED;
import static com.nodomain.ivonne.snippet.espConfiguration.espManager.ESP_RES_FAILED;
import static com.nodomain.ivonne.snippet.espConfiguration.espManager.ESP_RES_RECEIVED;
import static com.nodomain.ivonne.snippet.espConfiguration.espManager.SOCKET_CONNECT;
import static com.nodomain.ivonne.snippet.espConfiguration.snippetNewActivity.ACTION;
import static com.nodomain.ivonne.snippet.espConfiguration.snippetNewActivity.CONFIGURE_SNIPPET;
import static com.nodomain.ivonne.snippet.espConfiguration.snippetNewActivity.PARAM1;
import static com.nodomain.ivonne.snippet.espConfiguration.snippetNewActivity.PARAM2;
import static com.nodomain.ivonne.snippet.espConfiguration.snippetNewActivity.PARAM3;
import static com.nodomain.ivonne.snippet.espConfiguration.snippetNewActivity.PARAM4;
import static com.nodomain.ivonne.snippet.espConfiguration.snippetNewActivity.RECONNECT;
import static com.nodomain.ivonne.snippet.espConfiguration.snippetNewActivity.SHOW_MAC;
import static com.nodomain.ivonne.snippet.espConfiguration.snippetNewActivity.VALIDATE_PSW;

public class backgroundActivity extends AppCompatActivity implements Handler.Callback {
    static final String TAG = "BACKGROUND";
    static final String HIDDEN = "HIDDEN";
    private static final String TAG_RETAINED_INTENT = "RetainedIntent";
    private retainIntent intentRetenided;

    protected static Handler myHandler;
    private String action;
    private Intent intent;

    private auxiliarTools myTools;
    private espManager myEspManager;

    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_background);
        this.setFinishOnTouchOutside(false);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        TextView text = (TextView) findViewById(R.id.text);
        Context context = this;
        myHandler = new Handler(this);
        action = getIntent().getStringExtra(ACTION);
        boolean hidden = getIntent().getBooleanExtra(HIDDEN,false);
        if (hidden)
            findViewById(R.id.wait_layout).setVisibility(View.GONE);

        FragmentManager fm = getFragmentManager();
        intentRetenided = (retainIntent) fm.findFragmentByTag(TAG_RETAINED_INTENT);

        /*Parse dialog*/
        switch (action){
            case VALIDATE_PSW:{
                text.setText(getString(R.string.mensaje1));
                break;
            }
            case RECONNECT:{
                text.setText(getString(R.string.mensaje2));
                break;
            }
            case SHOW_MAC:{
                text.setText(getString(R.string.mensaje3));
                break;
            }
            case CONFIGURE_SNIPPET:{
                text.setText(getString(R.string.mensaje4));
                break;
            }
            case SCAN_NEW:{
                text.setText(getString(R.string.mensaje6));
                break;
            }
        }

        /*Parse data for action*/
        if (intentRetenided == null) {
            // add the fragment
            intentRetenided = new retainIntent();
            fm.beginTransaction().add(intentRetenided, TAG_RETAINED_INTENT).commit();

            myEspManager = new espManager(this);
            myTools = new auxiliarTools();

            Log.w(TAG,action);
            switch (action){
                case VALIDATE_PSW:{
                    intent = new Intent(context, backgroundService.class);
                    intent.putExtra(backgroundService.EXTRA_MESSENGER, new Messenger(myHandler));
                    intent.setAction(VALIDATE_PSW);
                    String param1 = getIntent().getStringExtra(PARAM1);
                    String param2 = getIntent().getStringExtra(PARAM2);
                    intent.putExtra(PARAM1, param1);
                    intent.putExtra(PARAM2, param2);
                    intentRetenided.setIntent(intent);
                    context.startService(intent);
                    break;
                }
                case RECONNECT:{
                    intent.setAction(RECONNECT);
                    String param1 = getIntent().getStringExtra(PARAM1);
                    intent.putExtra(PARAM1, param1);
                    intentRetenided.setIntent(intent);
                    context.startService(intent);
                    break;
                }
                case SHOW_MAC:{
                    handleAccionRequestMac();
                    break;
                }
                case CONFIGURE_SNIPPET:{
                    String param1 = getIntent().getStringExtra(PARAM1);//ESP NAME
                    String param2 = getIntent().getStringExtra(PARAM2);//ESP PSW
                    String param3 = getIntent().getStringExtra(PARAM3);//NETWORK NAME
                    String param4 = getIntent().getStringExtra(PARAM4);//NETWORK PSW
                    handleAccionConfigureEsp(param1, param2, param3, param4);
                    break;
                }
                case SCAN_NEW:{
                    scannAll();
                    break;
                }
                case CORRECT_IP:{
                    String param1 = getIntent().getStringExtra(PARAM1);
                    scannOne(param1);
                    break;
                }
            }
        }
    }

    /*RETAIN INTENT*/
    public static class retainIntent extends Fragment {
        private Intent intent;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }

        public void setIntent(Intent intent) {
            this.intent = intent;
        }

        public Intent recoverIntent() {
            return intent;
        }

    }

    /*FUNCTIONS*/
    /*Functions that required a socket to ESP -cannot be run from service-*/
    private void handleAccionRequestMac() {
        myEspManager.setOnConnectedListener(new espManager.OnConnectedListener() {
            @Override
            public void onConnected(boolean connected) {
                if (connected) {
                    WifiManager wifiManager = (WifiManager) getApplicationContext()
                            .getSystemService(Context.WIFI_SERVICE);
                    DhcpInfo info = wifiManager.getDhcpInfo();
                    int gatewayIP = info.gateway;
                    sendToEsp sendingToEsp = new sendToEsp(getApplicationContext(), new sendToEsp.AsyncResponse() {
                        @Override
                        public void processFinish(String output) {
                            String values[] = output.split(",");
                            switch (values[0]) {
                                case ESP_RES_RECEIVED: {
                                    myEspManager.borrarEspWifi();
                                    handleResult(values[1], true);
                                    break;
                                }
                                default: {
                                    myEspManager.borrarEspWifi();
                                    handleResult(null, false);
                                }
                            }
                        }
                    });
                    sendingToEsp.execute(ESP_CMD_MAC, null, myTools.intToIp(gatewayIP));
                } else {
                    myEspManager.borrarEspWifi();
                    handleResult(null, false);
                }
            }
        });
        myEspManager.connectToEsp();
    }

    private void handleAccionConfigureEsp(final String ESPname, final String ESPpsw,
                                          final String networkSSID, final String contrasenaSSID) {
        myEspManager.setOnConnectedListener(new espManager.OnConnectedListener() {
            @Override
            public void onConnected(boolean connected) {
                if (connected) {
                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    int gatewayIP = wifiManager.getDhcpInfo().gateway;
                    sendToEsp sendingToEsp = new sendToEsp(getApplicationContext(), new sendToEsp.AsyncResponse() {
                        @Override
                        public void processFinish(String output) {
                            String result = output.substring(0,output.indexOf(","));
                            switch (result) {
                                case ESP_RES_RECEIVED: {
                                    getEspData();
                                    break;
                                }
                                default: {
                                    myEspManager.borrarEspWifi();
                                    handleResult(null, false);
                                }
                            }

                        }
                    });
                    sendingToEsp.execute(SOCKET_CONNECT + networkSSID.replaceAll("\"","") + "," + contrasenaSSID + "," + ESPname + "," + ESPpsw + ESP_CLOSER, null, myTools.intToIp(gatewayIP));
                } else{
                    myEspManager.borrarEspWifi();
                    handleResult(null, false);
                }
            }
        });
        myEspManager.connectToEsp();
    }

    protected void getEspData(){
        myEspManager.borrarEspWifi();
        timer = new CountDownTimer(30000, 1000) {/*wait 10 seconds before reading data*/
            int counter=0;
            @Override
            public void onTick(long l) {
                counter ++;
                if (counter == 11) {/*wait exactly 10 seconds*/
                    myEspManager.setOnConnectedListener(new espManager.OnConnectedListener() {
                        @Override
                        public void onConnected(boolean connected) {
                            if (connected) {
                                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                                int gatewayIP = wifiManager.getDhcpInfo().gateway;
                                sendToEsp sendingToEsp = new sendToEsp(getApplicationContext(), new sendToEsp.AsyncResponse() {
                                    @Override
                                    public void processFinish(String output) {
                                        String[] resultado = output.split(",");
                                        switch (resultado[0]) {
                                            case ESP_RES_CONNECTED: {
                                                myEspManager.borrarEspWifi();
                                                String aux = output.replaceAll("\\.", "p");
                                                handleResult(aux.substring(aux.indexOf(",")+1), true);
                                                break;
                                            }
                                            case ESP_RES_FAILED: {
                                                handleResult(ESP_RES_FAILED, true);
                                                break;
                                            }
                                            default: {
                                                myEspManager.borrarEspWifi();
                                                handleResult(null, false);
                                                break;
                                            }
                                        }
                                    }
                                });
                                sendingToEsp.execute(ESP_CMD_ESTADO, null, myTools.intToIp(gatewayIP));
                            } else{
                                myEspManager.borrarEspWifi();
                                handleResult(null, false);
                            }
                        }
                    });
                    myEspManager.connectToEsp();
                }

            }
            @Override
            public void onFinish() {
                myEspManager.borrarEspWifi();
                handleResult(null, false);
                this.cancel();
            }
        }.start();
    }

    private void scannAll(){
        WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        DhcpInfo info = wifiManager.getDhcpInfo();
        int intGate = info.gateway;
        int intMask = info.netmask;
        int intRed = (intMask & intGate);
        scanNew scanNewtask = new scanNew(backgroundActivity.this, new scanNew.AsyncResponse() {
            @Override
            public void processFinish(boolean nuevo) {
                if (nuevo)
                    handleResult(null, true);
                else
                    handleResult(null, false);
            }
        });
        scanNewtask.setearRed(intMask, intRed, intGate);
        scanNewtask.execute();
    }

    private void scannOne(final String macABuscar){
        WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        DhcpInfo info = wifiManager.getDhcpInfo();
        int intGate = info.gateway;
        int intMask = info.netmask;
        int intRed = (intMask & intGate);
        scanOne scanOneTask = new scanOne(new scanOne.AsyncResponse() {
            @Override
            public void processFinish(String IpCorregida) {
                if (!IpCorregida.isEmpty())
                    handleResult(IpCorregida+","+macABuscar, true);
                else
                    handleResult(null, false);
            }
        });
        scanOneTask.setearValores(intMask, intRed, intGate, macABuscar);
        scanOneTask.execute();
    }

    /*Handle result from service*/
    @Override
    public boolean handleMessage(Message msg){
        if (msg.arg1 == 0) {
            setResult(RESULT_CANCELED);
        }
        else {
            switch (action) {
                case VALIDATE_PSW: {
                    setResult(RESULT_OK);
                    break;
                }
                case RECONNECT: {
                    break;
                }
            }
        }
        finish();
        return true;
    }

    /*Handle rest of the results*/
    protected void handleResult(String text, boolean result) {
        if (timer != null) {
            timer.cancel();
        }
        try {
            if (!result)
                setResult(RESULT_CANCELED);
            else{
                switch (action) {
                    case SHOW_MAC:{
                        setResult(RESULT_OK, getIntent().putExtra(PARAM1, text));
                        break;
                    }
                    case CONFIGURE_SNIPPET:{
                        setResult(RESULT_OK, getIntent().putExtra(PARAM1, text));
                        break;
                    }
                    case SCAN_NEW:{
                        setResult(RESULT_OK);
                        break;
                    }
                    case CORRECT_IP:{
                        setResult(RESULT_OK, getIntent().putExtra(PARAM1, text.substring(0,text.indexOf(",")))
                                .putExtra(PARAM2, text.substring(1,text.indexOf(","))));
                        break;
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        finish();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(isFinishing()) {
            FragmentManager fm = getFragmentManager();
            // we will not need this fragment anymore, this may also be a good place to signal
            // to the retained fragment object to perform its own cleanup.
            fm.beginTransaction().remove(intentRetenided).commit();
        }
    }

    @Override
    public void onBackPressed() {
        new espManager(this).borrarEspWifi();
        setResult(RESULT_CANCELED);
        if (intent != null)
            stopService(intent);
        if (intentRetenided == null) {
            FragmentManager fm = getFragmentManager();
            fm.beginTransaction().remove(intentRetenided).commit();
        }
        super.onBackPressed();
    }
}
