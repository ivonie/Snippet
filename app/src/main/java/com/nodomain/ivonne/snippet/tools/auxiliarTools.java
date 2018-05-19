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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                String myNewFoo = readARP(myMac.toLowerCase());
                if (isIPreachable(myNewFoo)) {
                    foo = myNewFoo;
                }
            }
        }
        else{
            if (myMac != null) {
                myFoo = readARP(myMac);
                if (isIPreachable(myFoo))
                    foo = myFoo;
            }
        }
        return foo;
    }

    private static String readARP(String devMAC) {//ip from MAC
        String devIP = "";
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4) {
                    String ip = splitted[0];
                    String mac = splitted[3];
                    if (mac.equals(devMAC.toLowerCase()))
                        devIP = ip.replaceAll("\\.","p");
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
        return devIP;
    }

    private static String getHardwareAddress(String ip) {//MAC from IP
        String hw = "00:00:00:00:00:00";
        BufferedReader bufferedReader = null;
        try {
            if (ip != null) {
                String ptrn = String.format("^%s\\s+0x1\\s+0x2\\s+([:0-9a-fA-F]+)\\s+\\*\\s+\\w+$", ip.replace(".", "\\."));
                Pattern pattern = Pattern.compile(ptrn);
                bufferedReader = new BufferedReader(new FileReader("/proc/net/arp"), 8 * 1024);
                String line;
                Matcher matcher;
                while ((line = bufferedReader.readLine()) != null) {
                    matcher = pattern.matcher(line);
                    if (matcher.matches()) {
                        hw = matcher.group(1);
                        break;
                    }
                }
            }
        } catch (IOException e) {
            return hw;
        } finally {
            try {
                if(bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
            }
        }
        return hw;
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
