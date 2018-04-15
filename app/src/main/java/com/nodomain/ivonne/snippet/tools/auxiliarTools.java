package com.nodomain.ivonne.snippet.tools;

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

public class auxiliarTools {
    private int TIMEOUT_CONNECTION = 5000;
    private static final String TAG = "funcAux";

    public auxiliarTools(){}

    public String getValidFoo(String myMac, String myFoo){
        String foo = "";
        if (myFoo != null) {
            if (!myFoo.equals("0p0p0p0") && isIPreachable(myFoo))
                foo = myFoo;
            else{
                String myNewFoo = leerARP(myMac.toLowerCase());
                if (isIPreachable(myNewFoo)) {
                    foo = myNewFoo;
                }
            }
        }
        else{
            if (myMac != null) {
                myFoo = leerARP(myMac);
                if (isIPreachable(myFoo))
                    foo = myFoo;
            }
        }
        return foo;
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

    public String intToIp(int i) {
        return  ((i & 0xFF) + "." +
                ((i >>> 8) & 0xFF) + "." +
                ((i >>> 16) & 0xFF) + "." +
                ((i >>> 24) & 0xFF));
    }


}
