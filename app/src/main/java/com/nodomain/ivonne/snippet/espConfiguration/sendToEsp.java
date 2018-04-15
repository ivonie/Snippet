package com.nodomain.ivonne.snippet.espConfiguration;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.nodomain.ivonne.snippet.tools.auxiliarTools;
import com.nodomain.ivonne.snippet.tools.dataManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Ivonne on 21/09/2017.
 */

public class sendToEsp extends AsyncTask<String,Void,String> {
    static final String ESP_HEADER = "ESP8266,";

    /* COMMANDS */
    public static final String ESP_CMD_TOOGLE = ESP_HEADER+"1\r";
    public static final String ESP_CMD_ESTADO = ESP_HEADER+"3\r";
    public static final String ESP_CMD_NAME = ESP_HEADER+"4\r";
    public static final String ESP_CMD_PSW = ESP_HEADER+"5\r";
    public static final String ESP_CMD_STATUS = ESP_HEADER+"7\r";
    public static final String ESP_CMD_SCAN = ESP_HEADER+"9\r";
    public static final String ESP_CMD_DIMMER = ESP_HEADER+":\r";
    public static final String ESP_CMD_OFF = ESP_HEADER+"<\r";

    /* SEPARATORS */

    /* RESULTS*/
    public static final String ESP_RES_OK = "CORRECTO";
    public static final String ESP_RES_CONNECTED = "CONECTADO";
    public static final String ESP_RES_RETRY = "RETRY";
    public static final String ESP_RES_ERROR = "ERROR";
    public static final String ESP_RES_FAILED = "FAILEDCONNECTTOAP";

    private AsyncResponse delegate = null;
    private auxiliarTools misFunciones;
    private String TAG = "sendToEsp";
    private Context mContext;
    private int TIMEOUT_CONNECTION = 10000;//cuanto tiempo espera para que el ESP conteste
    public sendToEsp() {
    }
    public sendToEsp(Context context, AsyncResponse delegate) {
        this.mContext = context;
        this.delegate = delegate;
    }

    @Override
    protected void onPreExecute() {
                misFunciones = new auxiliarTools();
        }
    @Override
    protected String doInBackground(String... values) {
        String myMAC = values[1];
        String myFoo = values[2];
        if (values[2] == null) {
            dataManager myDM = new dataManager(mContext);
            String storedFoo = myDM.getStoredFoo(myMAC);
            myFoo = misFunciones.getValidFoo(myMAC, storedFoo);
        }
        Log.w(TAG, "Connection data " + myFoo + " " + myMAC);
        String resultado = "";
        if (!myFoo.equals("")) {
            try {
                String myRealFoo = myFoo.replaceAll("p", ".");
                Log.w(TAG, "Attempting to connect to " + myRealFoo + " " + myMAC);
                InetAddress netadd = InetAddress.getByName(myRealFoo);
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(netadd, 5000), TIMEOUT_CONNECTION);
                if (socket.isConnected()){
                    Log.w(TAG, "CONNECTED");
                    Log.w(TAG, "SENDING: "+ values[0]);
                    PrintStream output = new PrintStream(socket.getOutputStream());
                    output.println(values[0] + "\r");
                    InputStream stream = socket.getInputStream();
                    byte[] lenBytes = new byte[128];//128
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

    @Override
    protected void onPostExecute(String value) {
        if (value != null) {
            if (!value.equals(""))
                if (delegate != null)
                    delegate.processFinish(value);
        }
    }

    public interface AsyncResponse {
        void processFinish(String output);
    }
}