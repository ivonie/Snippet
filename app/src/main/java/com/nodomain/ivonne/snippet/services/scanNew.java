package com.nodomain.ivonne.snippet.services;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import com.nodomain.ivonne.snippet.objects.Device;
import com.nodomain.ivonne.snippet.tools.dataManager;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.nodomain.ivonne.snippet.espConfiguration.espManager.ESP_CMD_SCAN;
import static com.nodomain.ivonne.snippet.espConfiguration.espManager.ESP_RES_CONNECTED;
import static com.nodomain.ivonne.snippet.espConfiguration.espManager.ESP_RES_SCAN;
import static com.nodomain.ivonne.snippet.espConfiguration.espManager.SOCKET_RESPONSE;
import static com.nodomain.ivonne.snippet.espConfiguration.espManager.SOCKET_TIMEOUT;

/**
 * Created by Ivonne on 28/09/2017.
 */

public class scanNew extends AsyncTask<Void, Void, Void> {
    static final String TAG = "SCAN";
    private AsyncResponse delegate = null;
    private final static int PING_TIMEOUT = 500; //milisegundos
    private final static int TIMEOUT_SCAN = 30; // seconds
    private final static int TIMEOUT_SHUTDOWN = 30; // seconds
    private final static int MULTIPLIER = 2; // seconds
    private final static int THREADS = 10;
    private ExecutorService mPool,sPool; //pool de actividades asicronas
    private Context context;
    private boolean newDevices = false;
    private dataManager myDM;
    private String networkSSID;

    private int mascara = 0;
    private int red = 0;
    private int gate = 0;
    private int size = 0;
    private int inicio = 0;

    private ArrayList<String> addresses;

    public scanNew(Context context, AsyncResponse delegate) {
        this.context = context;
        myDM = new dataManager(context);
        this.delegate = delegate;
        addresses=new ArrayList<String>();
    }

    public void setearRed(int mascara, int red, int gate) {
        this.mascara = mascara;
        this.red = red;
        this.gate = gate;
    }

    @Override
    protected void onPreExecute() {
        WifiManager wifiManager = (WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        networkSSID = wifiManager.getConnectionInfo().getSSID().replaceAll("\"","");

        int diagonal = Integer.bitCount(mascara);//diagonal de la red
        size = (int) Math.pow(2,(32-diagonal))-3;
        inicio = Integer.reverse(red);
    }

    @Override
    protected Void doInBackground(Void... params) {
        mPool = Executors.newFixedThreadPool(2*THREADS);
        for (int i = inicio; i <= inicio+size-1; i++) {
            start(i);
        }
        mPool.shutdown();
        try {
            if(!mPool.awaitTermination(TIMEOUT_SCAN, TimeUnit.SECONDS)){
                mPool.shutdownNow();
                if(!mPool.awaitTermination(TIMEOUT_SHUTDOWN, TimeUnit.SECONDS)){
                }
            }
        } catch (InterruptedException e){
            mPool.shutdownNow();
            Thread.currentThread().interrupt();
        } finally {
            sPool = Executors.newFixedThreadPool(THREADS/10);
            processARP();
            sPool.shutdown();
            try {
                if(!sPool.awaitTermination(TIMEOUT_SCAN*MULTIPLIER, TimeUnit.SECONDS)){
                    sPool.shutdownNow();
                    if(!sPool.awaitTermination(TIMEOUT_SHUTDOWN*MULTIPLIER, TimeUnit.SECONDS)){
                    }
                }
            } catch (InterruptedException e){
                sPool.shutdownNow();
                Thread.currentThread().interrupt();
            } finally {
                //return null;
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        if (delegate != null) {
            Log.w(TAG,"SCAN COMPLETED");
            delegate.processFinish(newDevices);
        }
    }

    interface AsyncResponse {
        void processFinish(boolean nuevo);
    }

    @Override
    protected void onCancelled() {
        if (mPool != null) {
            synchronized (mPool) {
                mPool.shutdownNow();
            }
        }
        onPostExecute(null);
        super.onCancelled();
    }

    private void start(int i) {
        if(!mPool.isShutdown()) {
            mPool.execute(new networkSearch(getStringFromReversedIP(i)));
        }
    }

    private class networkSearch implements Runnable {
        private String addr;

        networkSearch(String addr) {
            this.addr = addr;
        }

        public void run() {
            if(isCancelled()) {
                reachable(null);
            }
            // Create host object
            if(!addr.equals(getStringFromReversedIP(Integer.reverse(gate))) & !addr.equals("0.0.0.0")) {
                try {
                    InetAddress h = InetAddress.getByName(addr);
                    // Native InetAddress check
                    if (h.isReachable(PING_TIMEOUT)) {
                        Log.w(TAG,"Ping to "+addr);
                        return;
                    }
                    else
                        h.isReachable(PING_TIMEOUT);
                } catch (IOException e) {
                }
            }
        }
    }

    private void processARP() {
        Log.w(TAG,"Processing ARP table");
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4) {
                    if(!(splitted[3].equals("00:00:00:00:00:00")) && !(splitted[0].equals("IP"))) {
                        Log.w(TAG,"IP: "+splitted[0]+" MAC: "+splitted[3]);
                        if(!sPool.isShutdown()) {
                            sPool.execute(new networkProcess(splitted[0]));
                        }
                    }
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
    }

    private class networkProcess implements Runnable {
        private String addr;

        networkProcess(String addr) {
            this.addr = addr;
        }

        public void run() {
            if(isCancelled()) {
                reachable(null);
            }
            // Create host object
            Device myDevice = new Device();
            myDevice.setDevFoo(addr.replaceAll("\\.", "p"));
            myDevice.setDevMac(getHardwareAddress(addr));
            reachable(myDevice);
        }
    }

    private void reachable(Device host) {
        if (myDM.deviceExists(host.getDevMac().toUpperCase())) {
            myDM.updateIP(host.getDevMac(), host.getDevFoo());
        }
        else{
            if (!(addresses.contains(host.getDevFoo().replaceAll("p", "\\."))))
                connectWithNew(host);
        }
    }

    private void connectWithNew(Device host){
        Socket socket = new Socket();
        try {
            InetAddress netadd = InetAddress.getByName(host.getDevFoo().replaceAll("p", "\\."));
            Log.w(TAG,"attempting socket connection to "+host.getDevFoo());
            addresses.add(host.getDevFoo().replaceAll("p", "\\."));
            socket.connect(new InetSocketAddress(netadd, 5000),SOCKET_TIMEOUT);//timeout for connection
            socket.setSoTimeout(SOCKET_RESPONSE);
            if (socket.isConnected()){
                Log.w(TAG,"Connected to "+host.getDevFoo());
                PrintStream output = new PrintStream(socket.getOutputStream());
                output.println(ESP_CMD_SCAN);
                InputStream stream = socket.getInputStream();
                byte[] lenBytes = new byte[128];//128
                stream.read(lenBytes, 0, 128);
                String resultado = new String(lenBytes, "UTF-8").trim();
                Log.w(TAG,"Received: "+resultado);
                String[] data = resultado.split(",");
                if (data[0].equals(ESP_RES_SCAN)) {
                    Device myDevice = new Device();
                    myDevice.setDevFoo(host.getDevFoo());
                    myDevice.setDevMac(data[2]);
                    myDevice.setDevType(data[3]);
                    myDevice.setDevImage(data[3].toLowerCase() + "0");
                    myDevice.setDevName(data[4]);
                    myDevice.setDevNetwork(networkSSID);
                    myDevice.setDevPsw("----");
                    myDevice.setDevHouseSpace("Nuevo");
                    newDevices = true;
                    myDM.addorUpdateDevice(myDevice);
                    Log.w(TAG,"New ESP added to the database "+host.getDevFoo());
                }
            }
        } catch (SocketTimeoutException ex) {
            if(socket.isConnected()){
                Log.w(TAG,host.getDevFoo()+" timeout");
                try{
                    socket.close();
                    Log.w(TAG,"socket close "+host.getDevFoo());
                }catch (IOException e) {}
            }
        } catch (UnknownHostException ex) {
        }catch (IOException ex) {}
    }

    private static String getStringFromReversedIP(int ip_reverse) {
        int i = Integer.reverse(ip_reverse);
        return  ((i & 0xFF) + "." +
                ((i >>> 8) & 0xFF) + "." +
                ((i >>> 16) & 0xFF) + "." +
                ((i >>> 24) & 0xFF));
    }

    private static String getHardwareAddress(String ip) {
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
}