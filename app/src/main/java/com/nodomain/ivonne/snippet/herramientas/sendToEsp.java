package com.nodomain.ivonne.snippet.herramientas;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.nodomain.ivonne.snippet.herramientas.funcionesAuxiliares;
import com.nodomain.ivonne.snippet.herramientas.dataManager;

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
        private AsyncResponse delegate = null;
        private funcionesAuxiliares misFunciones;
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
                misFunciones = new funcionesAuxiliares();
        }

        @Override
        protected String doInBackground(String... values) {
                String myMAC = values[1];
                Log.w(TAG, "MAC: "+ myMAC);
                String myFoo = values[2];
                Log.w(TAG, "IP: "+ myFoo);
                if (values[2] == null) {
                        dataManager myDM = new dataManager(mContext);
                        String storedFoo = myDM.getFooAlmacenada(myMAC);
                        myFoo = misFunciones.getIPvalida(myMAC, storedFoo);
                }
                String resultado = "";
                if (!myFoo.equals("")) {
                        try {
                                String myRealFoo = myFoo.replaceAll("p", ".");
                                InetAddress netadd = InetAddress.getByName(myRealFoo);
                                Socket socket = new Socket();
                                socket.connect(new InetSocketAddress(netadd, 5000), TIMEOUT_CONNECTION);
                                PrintStream output = new PrintStream(socket.getOutputStream());
                                output.println(values[0] + "\r");
                                Log.w("SEND", values[0] + " " + myRealFoo);
                                InputStream stream = socket.getInputStream();
                                byte[] lenBytes = new byte[128];//128
                                stream.read(lenBytes, 0, 128);
                                resultado = new String(lenBytes, "UTF-8").trim();
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