package com.nodomain.ivonne.snippet.services;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;

import com.nodomain.ivonne.snippet.objects.Device;
import com.nodomain.ivonne.snippet.tools.dataManager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.nodomain.ivonne.snippet.espConfiguration.sendToEsp.ESP_CMD_SCAN;

/**
 * Created by Ivonne on 28/09/2017.
 */

public class scanNew extends AsyncTask<Void, Void, Void> {
    static final String TAG = "SCAN";
    private AsyncResponse delegate = null;
    private final static int TIMEOUT_CONNECTION = 1500; //milisegundos
    private final static int TIMEOUT_SCAN = 3600; // seconds
    private final static int TIMEOUT_SHUTDOWN = 10; // seconds
    private final static int THREADS = 10;
    private ExecutorService mPool; //pool de actividades asicronas
    private Context context;
    private boolean newDevices = false;
    private dataManager myDM;
    private String networkSSID;

    private int mascara = 0;
    private int red = 0;
    private int gate = 0;
    private int tamaño = 0;
    private int inicio = 0;

    public scanNew(Context context, AsyncResponse delegate) {
        this.context = context;
        myDM = new dataManager(context);
        this.delegate = delegate;
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
        tamaño = (int) Math.pow(2,(32-diagonal))-3;
        inicio = Integer.reverse(red);
    }

    @Override
    protected Void doInBackground(Void... params) {
        mPool = Executors.newFixedThreadPool(THREADS);
        for (int i = inicio; i <= inicio+tamaño-1; i++) {
            iniciar(i);
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
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        if (delegate != null) {
            delegate.processFinish(newDevices);//si se encontraron dispositivos newDevices
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

    private void iniciar(int i) {
        if(!mPool.isShutdown()) {
            mPool.execute(new buscarEnLaRed(getStringFromReversedIP(i)));
        }
    }

    private class buscarEnLaRed implements Runnable {
        private String addr;

        buscarEnLaRed(String addr) {
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
                    // Arp Check #1
                    if (!getHardwareAddress(addr).equals("00:00:00:00:00:00")) {
                        Device myDevice = new Device();
                        myDevice.setDevFoo(addr.replaceAll("\\.", "p"));
                        myDevice.setDevMac(getHardwareAddress(addr));
                        reachable(myDevice);
                        return;
                    }
                    // Native InetAddress check
                    if (h.isReachable(TIMEOUT_CONNECTION)) {
                        Device myDevice = new Device();
                        myDevice.setDevFoo(addr.replaceAll("\\.", "p"));
                        myDevice.setDevMac(getHardwareAddress(addr));
                        reachable(myDevice);
                        return;
                    }
                    // Arp Check #2
                    if (!getHardwareAddress(addr).equals("00:00:00:00:00:00")) {
                        Device myDevice = new Device();
                        myDevice.setDevFoo(addr.replaceAll("\\.", "p"));
                        myDevice.setDevMac(getHardwareAddress(addr));
                        reachable(myDevice);
                        return;
                    }
                } catch (IOException e) {
                }
            }
        }
    }

    private void reachable(Device host) {
        if (myDM.deviceExists(host.getDevMac().toUpperCase()))//si el dispositivo ya esta en la base de datos
            myDM.updateIP(host.getDevMac(), host.getDevFoo());
        else{
            connectWithNew(host);
        }
    }

    private void connectWithNew(Device host){
        try {
            InetAddress netadd = InetAddress.getByName(host.getDevFoo().replaceAll("p", "\\."));
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(netadd, 5000), TIMEOUT_CONNECTION);
            if (socket.isConnected()){
                PrintStream output = new PrintStream(socket.getOutputStream());
                output.println(ESP_CMD_SCAN);//FIXME: 9 escaneo del dimmer
                InputStream stream = socket.getInputStream();
                byte[] lenBytes = new byte[128];//128
                stream.read(lenBytes, 0, 128);
                String resultado = new String(lenBytes, "UTF-8").trim();
                String[] valores = resultado.split(",");
                if (valores[0].equals("CONECTADO")) {
                    Device myDevice = new Device();
                    myDevice.setDevFoo(host.getDevFoo());
                    myDevice.setDevMac(valores[2]);
                    myDevice.setDevType(valores[3]);
                    myDevice.setDevImage(valores[3].toLowerCase() + "0");
                    myDevice.setDevName(valores[4]);
                    myDevice.setDevNetwork(networkSSID);
                    myDevice.setDevPsw("0000");
                    myDevice.setDevHouseSpace("Nuevo");
                    newDevices = true;
                    myDM.addorUpdateDevice(myDevice);
                }

            }
        } catch (UnknownHostException ex) {
        } catch (IOException ex) {
        }
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