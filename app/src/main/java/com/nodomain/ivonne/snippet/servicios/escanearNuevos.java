package com.nodomain.ivonne.snippet.servicios;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;

import com.nodomain.ivonne.snippet.objetos.Dispositivo;
import com.nodomain.ivonne.snippet.herramientas.dataManager;

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

/**
 * Created by Ivonne on 28/09/2017.
 */

public class escanearNuevos extends AsyncTask<Void, Void, Void> {
    private AsyncResponse delegate = null;
    private final static int TIMEOUT_CONNECTION = 1500; //milisegundos
    private final static int TIMEOUT_SCAN = 3600; // seconds
    private final static int TIMEOUT_SHUTDOWN = 10; // seconds
    private final static int THREADS = 10;
    private ExecutorService mPool; //pool de actividades asicronas
    private Context context;
    private int contador = 0;
    private dataManager myDM;
    private String networkSSID;

    private int mascara = 0;
    private int red = 0;
    private int puerta = 0;
    private int tamaño = 0;
    private int inicio = 0;

    public escanearNuevos(Context context, AsyncResponse delegate) {
        this.context = context;
        myDM = new dataManager(context);
        this.delegate = delegate;
    }

    public void setearRed(int mascara, int red, int puerta) {
        this.mascara = mascara;
        this.red = red;
        this.puerta = puerta;
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
            if (contador > 0)
                delegate.processFinish(true);//si se encontraron dispositivos nuevos
            else
                delegate.processFinish(false);
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
            mPool.execute(new revisarConexion(getStringFromReversedIP(i)));
        }
    }

    private class revisarConexion implements Runnable {
        private String addr;

        revisarConexion(String addr) {
            this.addr = addr;
        }

        public void run() {
            if(isCancelled()) {
                alcanzable(null);
            }
            // Create host object
            if(!addr.equals(getStringFromReversedIP(Integer.reverse(puerta))) & !addr.equals("0.0.0.0")) {
                final Dispositivo myDispo = new Dispositivo();
                myDispo.setFoo(addr);
                try {
                    InetAddress h = InetAddress.getByName(addr);
                    // Arp Check #1
                    myDispo.setMac(getHardwareAddress(addr));
                    if (!myDispo.getMac().equals("00:00:00:00:00:00")) {
                        alcanzable(myDispo);
                        return;
                    }
                    // Native InetAddress check
                    if (h.isReachable(TIMEOUT_CONNECTION)) {
                        alcanzable(myDispo);
                        return;
                    }
                    // Arp Check #2
                    myDispo.setMac(getHardwareAddress(addr));
                    if (!myDispo.getMac().equals("00:00:00:00:00:00")) {
                        alcanzable(myDispo);
                        return;
                    }
                } catch (IOException e) {
                }
            }
        }
    }

    private void alcanzable(final Dispositivo host) {
        if (myDM.existeDispositivo(host.getMac().toUpperCase()))//si el dispositivo ya esta en la base de datos
            myDM.actualizarIP(host.getMac(), host.getFoo().replaceAll("\\.", "p"));
        else{
            try {
                InetAddress netadd = InetAddress.getByName(host.getFoo());
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(netadd, 5000), TIMEOUT_CONNECTION);
                PrintStream output = new PrintStream(socket.getOutputStream());
                output.println("ESP8266,9ESTADO\r");//FIXME: 9 escaneo del dimmer
                InputStream stream = socket.getInputStream();
                byte[] lenBytes = new byte[128];//128
                stream.read(lenBytes, 0, 128);
                String resultado = new String(lenBytes, "UTF-8").trim();
                String[] valores = resultado.split(",");
                if (valores[0].equals("CONECTADO")) {
                    String aux = valores[1].replaceAll("\\.", "p");
                    Dispositivo dispositivo = new Dispositivo();
                    dispositivo.setFoo(aux);
                    dispositivo.setMac(valores[2]);
                    dispositivo.setTipo(valores[3]);
                    dispositivo.setNombre(valores[4]);
                    dispositivo.setRed(networkSSID);
                    dispositivo.setContrasena("0000");
                    dispositivo.setAmbiente("Nuevo");
                    dispositivo.setImagen(valores[3].toLowerCase() + "0");
                    contador++;
                    myDM.agregarActualizarDispositivo(dispositivo);
                }
            } catch (UnknownHostException ex) {
            } catch (IOException ex) {
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