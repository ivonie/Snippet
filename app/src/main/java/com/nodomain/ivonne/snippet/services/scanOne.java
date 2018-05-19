package com.nodomain.ivonne.snippet.services;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by Ivonne on 19/03/2018.
 */

public class scanOne extends AsyncTask<Void, Void, Void> {
    private AsyncResponse delegate = null;
    static final String TAG = "SCAN";
    private final static int TIMEOUT_CONNECTION = 300; //milisegundos
    private final static int TIMEOUT_SCAN = 3600; // seconds
    private final static int TIMEOUT_SHUTDOWN = 10; // seconds
    private final static int THREADS = 10;
    private ExecutorService mPool; //pool de actividades asicronas
    private String searchedMAC;
    private String IPFound = "";

    private int mascara = 0;
    private int red = 0;
    private int gate = 0;
    private int size = 0;
    private int inicio = 0;

    public scanOne(AsyncResponse delegate) {
        this.delegate = delegate;
    }

    public void setearValores(int mascara, int red, int gate, String MacABuscar) {
        this.mascara = mascara;
        this.red = red;
        this.gate = gate;
        this.searchedMAC = MacABuscar;
    }

    @Override
    protected void onPreExecute() {
        int diagonal = Integer.bitCount(mascara);//diagonal de la red
        size = (int) Math.pow(2,(32-diagonal))-3;
        inicio = Integer.reverse(red);
    }

    @Override
    protected Void doInBackground(Void... params) {
        mPool = Executors.newFixedThreadPool(THREADS);
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
        }finally{
            IPFound = getAddressfromHardware(searchedMAC).replaceAll("\\.", "p");;
            Log.w(TAG,"ip: "+IPFound);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        if (delegate != null) {
            delegate.processFinish(IPFound);//si se encontraron dispositivos nuevos
        }
    }

    interface AsyncResponse {
        void processFinish(String fooEncontrada);
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
                //TODO:
            }
            // Create host object
            if(!addr.equals(getStringFromReversedIP(Integer.reverse(gate))) & !addr.equals("0.0.0.0")) {
                try {
                    InetAddress h = InetAddress.getByName(addr);
                    h.isReachable(TIMEOUT_CONNECTION);
                } catch (IOException e) {
                }
            }
        }
    }

    private static String getStringFromReversedIP(int ip_reverse) {
        int i = Integer.reverse(ip_reverse);
        return  ((i & 0xFF) + "." +
                ((i >>> 8) & 0xFF) + "." +
                ((i >>> 16) & 0xFF) + "." +
                ((i >>> 24) & 0xFF));
    }

    private static String getAddressfromHardware(String hw) {//IP from MAC
        String devIP = "";
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4) {
                    if(!(splitted[3].equals("00:00:00:00:00:00"))) {
                        Log.w(TAG, "LINE " + line);
                        String ip = splitted[0];
                        String mac = splitted[3];
                        if (mac.equals(hw.toLowerCase()))
                            devIP = ip.replaceAll("\\.", "p");
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
        return devIP;
    }
}