package com.nodomain.ivonne.snippet.espConfiguration;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

/**
 * Created by Ivonne on 24/03/2018.
 */

public class espManager {
    static final String TAG = "ESP_MANAGER";

    public static final String ESP_NAME = "NAME";
    public static final String ESP_MAC = "MAC";
    public static final String ESP_FOO = "FOO";
    public static final String ESP_TYPE = "TYPE";
    public static final String ESP_IMAGE = "IMAGE";
    public static final String ESP_STATUS = "STATUS";
    public static final String ESP_PSW = "PSW";

    /*Snippet AP network*/
    public static final String ESP_AP_SSID = "ESP8266";
    public static final String ESP_AP_PSW = "12345678";

    static final String ESP_HEADER = "ESP8266,";
    public static final String ESP_CLOSER = ",\r";

    /* COMMANDS */
    public static final String ESP_CMD_TOOGLE = ESP_HEADER+"1,";
    public static final String SOCKET_CONNECT = ESP_HEADER+"2,";
    public static final String ESP_CMD_ESTADO = ESP_HEADER+"3"+ESP_CLOSER;
    public static final String ESP_CMD_NAME = ESP_HEADER+"4,";
    public static final String ESP_CMD_PSW = ESP_HEADER+"5,";
    public static final String ESP_CMD_STATUS = ESP_HEADER+"7,";
    public static final String ESP_CMD_DIMMER_SEND = ESP_HEADER+"8,";
    public static final String ESP_CMD_DIMMER_CLOSE = "e"+ESP_CLOSER;
    public static final String ESP_CMD_SCAN = ESP_HEADER+"9"+ESP_CLOSER;
    public static final String ESP_CMD_STATUS_DIMMER = ESP_HEADER+":,";//10
    public static final String ESP_CMD_TIMER = ESP_HEADER+";,";//11
    public static final String ESP_CMD_SETOFF = ESP_HEADER+"<,";//12
    public static final String ESP_CMD_MAC = ESP_HEADER+"=";//13

    /* RESULTS*/
    public static final String ESP_RES_CONNECTED = "CONECTADO";
    public static final String ESP_RES_FAILED = "FAILCONNECTTOAP";
    public static final String ESP_RES_RECEIVED = "RECIBIDO";
    public static final String ESP_RES_DIMMER = "VALUEOFDIMMER";
    public static final String ESP_RES_SCAN = "SCANOK";
    public static final String ESP_RES_OK = "CORRECTO";
    public static final String ESP_NAME_CHANGED ="NAMEDEVICECHANGED";
    public static final String ESP_PSW_CHANGED ="PASSDEVICECHANGED";
    public static final String ESP_RES_ERROR = "ERROR";

    /*Snippet types*/
    public static final String LIGHT_BULB = "FOCO";
    public static final String LIGHT_DIMMER = "DIMMER";

    /*Snippet status*/
    public static final String STATUS_ON = "ON";
    public static final String STATUS_OFF = "OFF";
    public static final String STATUS_DISCONNECTED = "DESCONECTADO";

    public static final int SOCKET_TIMEOUT = 200; //milisecodns
    public static final int SOCKET_RESPONSE = 10000; //miliseconds
    private boolean reconnect_flag = false;

    Context context;

    public espManager(Context context){
        this.context = context;
    }

    public void connectToEsp(){
        final WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + ESP_AP_SSID + "\"";
        conf.preSharedKey = "\""+ ESP_AP_PSW +"\"";
        final WifiManager wifiManager = (WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int netId = wifiManager.addNetwork(conf);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();

        new CountDownTimer(20000, 2000) {/*20 seconds to connect*/
            @Override
            public void onTick(long l) {
                if (wifiManager.getConnectionInfo().getSSID().contains(ESP_AP_SSID) &&
                        (wifiManager.getDhcpInfo().gateway != 0)) {
                    //we want to validate that is connected to the right network at least twice
                    if (reconnect_flag){
                        Log.w(TAG,"connected to ESP");
                        onConnectedListener.onConnected(true);
                        this.cancel();
                    }
                    else
                        reconnect_flag = true;
                }
                else
                    reconnect_flag = false;
            }

            @Override
            public void onFinish() {
                onConnectedListener.onConnected(false);
            }
        }.start();
    }

    public void setOnConnectedListener(OnConnectedListener onConnectedListener) {
        this.onConnectedListener = onConnectedListener;
    }

    private OnConnectedListener onConnectedListener;
    public interface OnConnectedListener {
        public void onConnected(boolean connected);
    }

    public void borrarEspWifi(){
        int netId = 0;
        WifiManager wifiManager = (WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.getConnectionInfo().getSSID().contains(ESP_AP_SSID)) {
            netId = wifiManager.getConnectionInfo().getNetworkId();
            wifiManager.disconnect();
            wifiManager.disableNetwork(netId);
            wifiManager.removeNetwork(netId);
            wifiManager.saveConfiguration();
        }
        List<WifiConfiguration> networks = wifiManager.getConfiguredNetworks();
        if (!networks.isEmpty()){
            for (WifiConfiguration configuration:networks) {
                if (configuration.SSID.contains(ESP_AP_SSID)){
                    netId = configuration.networkId;
                    wifiManager.disableNetwork(netId);
                    wifiManager.removeNetwork(netId);
                    wifiManager.saveConfiguration();//nuevo API no requiere de este comando
                }
            }
        }
        wifiManager.reconnect();
    }

    public void checkIfHomeNetwork(){
        WifiManager wifiManager = (WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo currentWifi = wifiManager.getConnectionInfo();
        String networkSSID = currentWifi.getSSID();
        if (networkSSID.contains(ESP_AP_SSID))
            borrarEspWifi();
    }

    public String getValidFoo(String myMac, String myFoo){
        if (myFoo != null) {
            if (!myFoo.equals("0p0p0p0") && isIPreachable(myFoo) && !myFoo.isEmpty())
                return myFoo;
        }
        if (myMac != null) {
            myFoo = readArp(myMac.toLowerCase());
            if (isIPreachable(myFoo))
                return myFoo;
        }
        else
            return "ERROR";
        return  "ERROR";
    }

    private static String readArp(String dis_mac) {
        String dis_ip = "";
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4) {
                    String ip = splitted[0];
                    String mac = splitted[3];
                    if (mac.equals(dis_mac.toLowerCase()))
                        dis_ip = ip.replaceAll("\\.","p");
                }
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } finally{
            try {
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return dis_ip;
    }

    private static boolean isIPreachable(String Foo){
        try {
            InetAddress netadd = InetAddress.getByName(Foo.replaceAll("p","."));
            if (netadd.isReachable(700))
                return true;
            else
                return false;
        } catch (Exception e) {
            return false;
        }
    }


    public String sendToESPfromService(final String myFoo, final String command){
                String resultado = "";
                if(!myFoo.equals(""))
                {
                    try {
                        String myRealFoo = myFoo.replaceAll("p", ".");
                        Log.w(TAG, "Attempting to connect to " + myRealFoo);
                        InetAddress netadd = InetAddress.getByName(myRealFoo);
                        Socket socket = new Socket();
                        socket.connect(new InetSocketAddress(netadd, 5000), SOCKET_TIMEOUT);
                        socket.setSoTimeout(SOCKET_RESPONSE);
                        if (socket.isConnected()){
                            PrintStream output = new PrintStream(socket.getOutputStream());
                            Log.w(TAG, command);
                            output.println(command);
                            InputStream stream = socket.getInputStream();
                            byte[] lenBytes = new byte[128];
                            stream.read(lenBytes, 0, 128);
                            resultado = new String(lenBytes, "UTF-8").trim();
                            Log.w(TAG, "RECEIVED: "+ resultado);
                        }
                        Log.w(TAG, "DISCONNECTED");
//                socket.close();
                    } catch (UnknownHostException ex) {
                    } catch (IOException ex) {
                    }
                }
                return resultado;
    }

    String status;
    public String getStatus(final String myFoo){
        Log.w(TAG,"getStatus");
        Log.w(TAG,myFoo);
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void ...params) {
                String estado = "";
                if(!myFoo.equals(""))
                {
                    try {
                        String myRealFoo = myFoo.replaceAll("p", ".");
                        Log.w(TAG,myRealFoo);
                        InetAddress netadd = InetAddress.getByName(myRealFoo);
                        Socket socket = new Socket();
                        socket.connect(new InetSocketAddress(netadd, 5000), SOCKET_TIMEOUT);
                        socket.setSoTimeout(SOCKET_RESPONSE);
                        PrintStream output = new PrintStream(socket.getOutputStream());
                        output.println("ESP8266,7STATUS\r");
                        InputStream stream = socket.getInputStream();
                        byte[] lenBytes = new byte[128];
                        stream.read(lenBytes, 0, 128);
                        estado = new String(lenBytes, "UTF-8").trim();
                        Log.w(TAG,estado);
//                socket.close();
                    } catch (UnknownHostException ex) {
                    } catch (IOException ex) {
                    }
                }
                return estado;
            }
            @Override
            protected void onPostExecute(String value) {
                status = value;
            }
        }.execute();
        Log.w(TAG,status);
        return status;
    }

}
