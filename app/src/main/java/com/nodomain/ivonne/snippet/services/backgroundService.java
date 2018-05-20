package com.nodomain.ivonne.snippet.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Message;

import com.nodomain.ivonne.snippet.tools.dataManager;
import com.nodomain.ivonne.snippet.objects.Network;

import java.util.List;

import static com.nodomain.ivonne.snippet.espConfiguration.snippetNewActivity.PARAM1;
import static com.nodomain.ivonne.snippet.espConfiguration.snippetNewActivity.PARAM2;
import static com.nodomain.ivonne.snippet.espConfiguration.snippetNewActivity.RECONNECT;
import static com.nodomain.ivonne.snippet.espConfiguration.snippetNewActivity.VALIDATE_PSW;
import static com.nodomain.ivonne.snippet.services.backgroundActivity.myHandler;


public class backgroundService extends Service {
    static final String EXTRA_MESSENGER = "com.nodomain.ivonne.snippet.extra.MESSENGER";
    static final String ACTION_BACKGROUND = "com.nodomain.ivonne.snippet.action.BACKGROUND";
    static final String TAG = "BACK_S";

    private CountDownTimer timer;

    public backgroundService() {
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
