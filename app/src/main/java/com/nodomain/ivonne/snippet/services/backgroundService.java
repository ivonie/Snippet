package com.nodomain.ivonne.snippet.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.nodomain.ivonne.snippet.espConfiguration.espManager;
import com.nodomain.ivonne.snippet.espConfiguration.sendToEsp;
import com.nodomain.ivonne.snippet.tools.dataManager;
import com.nodomain.ivonne.snippet.tools.auxiliarTools;
import com.nodomain.ivonne.snippet.objects.Network;

import java.util.List;

import static com.nodomain.ivonne.snippet.espConfiguration.espManager.ESP_CLOSER;
import static com.nodomain.ivonne.snippet.espConfiguration.espManager.ESP_CMD_ESTADO;
import static com.nodomain.ivonne.snippet.espConfiguration.espManager.ESP_CMD_MAC;
import static com.nodomain.ivonne.snippet.espConfiguration.espManager.ESP_PSW;
import static com.nodomain.ivonne.snippet.espConfiguration.espManager.ESP_RES_CONNECTED;
import static com.nodomain.ivonne.snippet.espConfiguration.espManager.ESP_RES_FAILED;
import static com.nodomain.ivonne.snippet.espConfiguration.espManager.ESP_RES_RECEIVED;
import static com.nodomain.ivonne.snippet.espConfiguration.espManager.SOCKET_CONNECT;
import static com.nodomain.ivonne.snippet.espConfiguration.snippetNewActivity.CONFIGURE_SNIPPET;
import static com.nodomain.ivonne.snippet.espConfiguration.snippetNewActivity.PARAM1;
import static com.nodomain.ivonne.snippet.espConfiguration.snippetNewActivity.PARAM2;
import static com.nodomain.ivonne.snippet.espConfiguration.snippetNewActivity.PARAM3;
import static com.nodomain.ivonne.snippet.espConfiguration.snippetNewActivity.PARAM4;
import static com.nodomain.ivonne.snippet.espConfiguration.snippetNewActivity.RECONNECT;
import static com.nodomain.ivonne.snippet.espConfiguration.snippetNewActivity.SHOW_MAC;
import static com.nodomain.ivonne.snippet.espConfiguration.snippetNewActivity.VALIDATE_PSW;
import static com.nodomain.ivonne.snippet.services.backgroundActivity.myHandler;


public class backgroundService extends Service {
    static final String EXTRA_MESSENGER = "com.nodomain.ivonne.snippet.extra.MESSENGER";
    static final String ACTION_BACKGROUND = "com.nodomain.ivonne.snippet.action.BACKGROUND";
    static final String TAG = "BACK_S";

    private String WIFI_ERROR = "WIFI_ERROR";

    private auxiliarTools myTools;
    private espManager myEspManager;

    private CountDownTimer timer;

    public backgroundService() {
        myEspManager = new espManager(this);
        myTools = new auxiliarTools();
    }

    @Override
    public IBinder onBind(Intent intent) {return null;}

    @Override
    public int onStartCommand(Intent intent,int flags, int startId) {
        if (intent != null) {
            String accion = intent.getAction();
            switch (accion) {
                case VALIDATE_PSW: {
                    String param1 = intent.getStringExtra(PARAM1);
                    String param2 = intent.getStringExtra(PARAM2);
                    handleAccionValidatePassword(param1, param2);
                    break;
                }
                case RECONNECT: {
                    String param1 = intent.getStringExtra(PARAM1);
                    handleAccionReconnect(param1);
                    break;
                }
                case SHOW_MAC: {
                    handleAccionRequestMac();
                    break;
                }
                case CONFIGURE_SNIPPET: {
                    String param1 = intent.getStringExtra(PARAM1);
                    String param2 = intent.getStringExtra(PARAM2);
                    String param3 = intent.getStringExtra(PARAM3);
                    String param4 = intent.getStringExtra(PARAM4);
                    handleAccionConfigureEsp(param1, param2, param3, param4);
                    break;
                }
                }

            }
        return super.onStartCommand(intent, flags, startId);
    }

    private void handleAccionValidatePassword(final String SSID, final String password) {
        final WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int netId = wifiManager.getConnectionInfo().getNetworkId();
        wifiManager.disconnect();
        wifiManager.disableNetwork(netId);
        wifiManager.removeNetwork(netId);
        wifiManager.saveConfiguration();
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = SSID;
        conf.preSharedKey = "\""+ password +"\"";
        netId = wifiManager.addNetwork(conf);
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();

        timer = new CountDownTimer(20000, 1000) {/*20 seconds to connect*/
            @Override
            public void onTick(long l) {
                if (wifiManager.getConnectionInfo().getIpAddress() != 0) {
                    if (wifiManager.getConnectionInfo().getSSID().equals(SSID)) {/*connection successfull*/
                        Network newNetwork = new Network();
                        newNetwork.setNetworkSSID(SSID);
                        newNetwork.setNetworkPSW(password);
                        dataManager mydataManager = new dataManager(getApplicationContext());
                        mydataManager.addOrUpdateNetwork(newNetwork);/*save network to database*/
                        handleResult(null, true);
                    }
                    else
                        handleResult(null, false);
                    this.cancel();
                }
            }

            @Override
            public void onFinish() {
                handleResult(null, false);
            }
        }.start();
    }

    private void handleAccionReconnect(final String networkName){
        final WifiManager wifiManager = (WifiManager)getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        if (!networkName.isEmpty()){/*if a network is specified*/
            List<WifiConfiguration> networks = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration configuration:networks) {//get the ID of the network mathcing the name
                if (configuration.SSID.contains(networkName)){
                    final int netId = configuration.networkId;
                    wifiManager.enableNetwork(netId, true);
                    break;
                }
            }
        }
        wifiManager.reconnect();
        timer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long l) {
                if (wifiManager.getConnectionInfo().getIpAddress() != 0) {
                    if (networkName.isEmpty()) {
                        handleResult(null, true);
                        this.cancel();
                    }
                    else {
                        if (wifiManager.getConnectionInfo().getSSID().equals(networkName)) {
                            handleResult(null, true);
                            this.cancel();
                        }
                        else {
                            wifiManager.disconnect();
                            wifiManager.reconnect();
                        }
                    }
                }
            }
            @Override
            public void onFinish() {
                handleResult(null, false);
            }
        }.start();
    }

    private void handleAccionRequestMac() {
        myEspManager.setOnConnectedListener(new espManager.OnConnectedListener() {
            @Override
            public void onConnected(boolean connected) {
                Log.w(TAG, "listener");
                if (connected) {
                    Log.w(TAG, "connected");
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

    private void handleAccionConfigureEsp(final String nombreESP, final String contrasenaESP,
                                          final String networkSSID, final String contrasenaSSID) {
        myEspManager.setOnConnectedListener(new espManager.OnConnectedListener() {
            @Override
            public void onConnected(boolean connected) {
                if (connected) {
                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    int gatewayIP = wifiManager.getDhcpInfo().gateway;
                    String output = myEspManager.sendToESPfromService(myTools.intToIp(gatewayIP),
                            SOCKET_CONNECT + networkSSID.replaceAll("\"","") + "," + contrasenaSSID + "," + nombreESP + "," + ESP_PSW + ESP_CLOSER);
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
                } else{
                    myEspManager.borrarEspWifi();
                    handleResult(null, false);
                }
            }
        });
        myEspManager.connectToEsp();
    }

    protected void getEspData(){
        new CountDownTimer(30000, 1000) {/*wait 10 seconds before reading data*/
            int counter=0;
            @Override
            public void onTick(long l) {
                counter ++;
                if (counter == 10) {/*wait exactly 10 seconds*/
                    myEspManager.setOnConnectedListener(new espManager.OnConnectedListener() {
                        @Override
                        public void onConnected(boolean connected) {
                            if (connected) {
                                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                                int gatewayIP = wifiManager.getDhcpInfo().gateway;
                                String output = myEspManager.sendToESPfromService(myTools.intToIp(gatewayIP), ESP_CMD_ESTADO);
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

    protected void handleResult(String text, boolean result) {
        if (timer != null) {
            timer.cancel();
        }
        try {
            Message msg = new Message();
            if (text != null)
                msg.obj = text;
            if (result)
                msg.arg1 = 1;//result OK
            else
                msg.arg1 = 0;//result NOTOK
            myHandler.sendMessage(msg);
            this.stopSelf();
        }catch (Exception e) {
            e.printStackTrace();
        }
        this.stopSelf();
    }

}
