package com.nodomain.ivonne.snippet.herramientas;

import android.os.AsyncTask;

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

/**
 * Created by Ivonne on 23/09/2017.
 */

public class funcionesAuxiliares {
    private int TIMEOUT_CONNECTION = 5000;
    private static final String TAG = "funcAux";

    public funcionesAuxiliares (){}

    public String getIPvalida(String myMac, String myFoo){
        String result = "";
        if (myFoo != null) {
            if (isIPreachable(myFoo))
                result = myFoo;
            else{
                String myNewFoo = leerARP(myMac.toLowerCase());
                if (isIPreachable(myNewFoo)) {
                    result = myNewFoo;
                }
            }
        }
        else{
            if (myMac != null) {
                myFoo = leerARP(myMac.toLowerCase());
                if (isIPreachable(myFoo))
                    result = myFoo;
            }
        }
        return result;
    }

    private static String leerARP(String dis_mac) {
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
                    if (mac.equals(dis_mac))
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

    String resultado = "";
    String getEstado(final String myFoo){
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void ...params) {
                String estado = "";
                if(!myFoo.equals(""))
                {
                    try {
                        String myRealFoo = myFoo.replaceAll("p", ".");
                        InetAddress netadd = InetAddress.getByName(myRealFoo);
                        Socket socket = new Socket();
                        socket.connect(new InetSocketAddress(netadd, 5000), TIMEOUT_CONNECTION);
                        PrintStream output = new PrintStream(socket.getOutputStream());
                        output.println("ESP8266,7STATUS\r");
                        InputStream stream = socket.getInputStream();
                        byte[] lenBytes = new byte[128];
                        stream.read(lenBytes, 0, 128);
                        estado = new String(lenBytes, "UTF-8").trim();
//                socket.close();
                    } catch (UnknownHostException ex) {
                    } catch (IOException ex) {
                    }
                }
                return estado;
            }
            @Override
            protected void onPostExecute(String value) {
                resultado = value;
            }
        }.execute();
        return resultado;
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

}
