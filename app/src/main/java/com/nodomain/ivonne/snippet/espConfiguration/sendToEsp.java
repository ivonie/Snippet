package com.nodomain.ivonne.snippet.espConfiguration;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import com.nodomain.ivonne.snippet.tools.auxiliarTools;
import com.nodomain.ivonne.snippet.tools.dataManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import static com.nodomain.ivonne.snippet.espConfiguration.espManager.SOCKET_RESPONSE;
import static com.nodomain.ivonne.snippet.espConfiguration.espManager.SOCKET_TIMEOUT;

/**
 * Created by Ivonne on 21/09/2017.
 */

public class sendToEsp extends AsyncTask<String,Void,String> {

    private AsyncResponse delegate = null;
    private auxiliarTools misFunciones;
    private String TAG = "sendToEsp";
    private Context mContext;
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
                WifiManager wifiManager = (WifiManager)mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                int localIpAddress = wifiManager.getConnectionInfo().getIpAddress();
                socket.bind(new InetSocketAddress(localIpAddress));
                socket.connect(new InetSocketAddress(netadd, 5000), SOCKET_TIMEOUT);
                if (socket.isConnected()){
                    socket.setSoTimeout(SOCKET_RESPONSE);
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