package com.nodomain.ivonne.snippet.servicios;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Message;

import com.nodomain.ivonne.snippet.objetos.Network;
import com.nodomain.ivonne.snippet.herramientas.sendToEsp;
import com.nodomain.ivonne.snippet.herramientas.dataManager;

import static com.nodomain.ivonne.snippet.servicios.segundoPlano.myHandler;
import static com.nodomain.ivonne.snippet.configuracionESP.snippetNuevo.PARAMETRO1;
import static com.nodomain.ivonne.snippet.configuracionESP.snippetNuevo.PARAMETRO2;
import static com.nodomain.ivonne.snippet.configuracionESP.snippetNuevo.PARAMETRO3;
import static com.nodomain.ivonne.snippet.configuracionESP.snippetNuevo.PARAMETRO4;

public class segundoPlanoService extends Service {
    static final String EXTRA_MESSENGER = "com.nodomain.ivonne.snippet.extra.MESSENGER";
    static final String ACTION_SEGUNDO_PLANO = "com.nodomain.ivonne.snippet.action.SEGUNDO_PLANO";

    private CountDownTimer timer;

    //baderas
    boolean enviado = false;
    boolean recibido = false;
    boolean primerTick = true;

    public segundoPlanoService() {
    }

    @Override
    public IBinder onBind(Intent intent) {return null;}

    @Override
    public int onStartCommand(Intent intent,int flags, int startId) {
        if (intent != null) {
            String accion = intent.getAction();
            switch (accion){
                case "VALIDAR_CONTRASENA":{
                    String param1 = intent.getStringExtra(PARAMETRO1);
                    String param2 = intent.getStringExtra(PARAMETRO2);
                    handleAccionValidarContraseña(param1, param2);
                    break;
                }
                case "RECONECTAR":{
                    handleAccionReconectar();
                    break;
                }
                case "MOSTRAR_MAC":{
                    handleAccionSolicitarMac();
                    break;
                }
                case "CONFIGURAR_SNIPPET":{
                    String param1 = intent.getStringExtra(PARAMETRO1);
                    String param2 = intent.getStringExtra(PARAMETRO2);
                    String param3 = intent.getStringExtra(PARAMETRO3);
                    String param4 = intent.getStringExtra(PARAMETRO4);
                    handleAccionConfigurarSnippet(param1, param2, param3, param4);
                    break;
                }
            }

        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void handleAccionValidarContraseña(final String SSID, final String constrasena) {
        final WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo currentWifi = wifiManager.getConnectionInfo();
        int netId = currentWifi.getNetworkId();
        wifiManager.disconnect();
        wifiManager.disableNetwork(netId);
        wifiManager.removeNetwork(netId);
        wifiManager.saveConfiguration();
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = SSID;
        conf.preSharedKey = "\""+ constrasena +"\"";
        netId = wifiManager.addNetwork(conf);
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();

        timer = new CountDownTimer(20000, 3000) {
            @Override
            public void onTick(long l) {
                if (wifiManager.getConnectionInfo().getIpAddress() != 0) {
                    if (wifiManager.getConnectionInfo().getSSID().equals(SSID)) {
                        Network nuevaRed = new Network();
                        nuevaRed.setNombre_ssid(SSID);
                        nuevaRed.setContrasena(constrasena);
                        dataManager mydataManager = new dataManager(getApplicationContext());
                        mydataManager.agregarActualizarRed(nuevaRed);
                        handleResultado(null, true);
                    }
                    else
                        handleResultado(null, false);
                    this.cancel();
                }
            }

            @Override
            public void onFinish() {
                handleResultado(null, false);
            }
        }.start();
    }

    private void handleAccionReconectar(){
        final WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.reconnect();
        timer = new CountDownTimer(10000, 3000) {
            @Override
            public void onTick(long l) {
                if (wifiManager.getConnectionInfo().getIpAddress() != 0) {
                    handleResultado(null, true);
                    this.cancel();
                }
            }

            @Override
            public void onFinish() {
                handleResultado(null, false);
            }
        }.start();
    }

    private void handleAccionSolicitarMac() {
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + "ESP8266" + "\"";
        conf.preSharedKey = "\""+ "12345678" +"\"";
        final WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int netId = wifiManager.addNetwork(conf);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();
        timer =  new CountDownTimer(20000, 3000) {
            public void onTick(long millisUntilFinished) {
                if (wifiManager.getConnectionInfo().getIpAddress() != 0) {
                    if (wifiManager.getConnectionInfo().getSSID().contains("ESP8266")) {
                        DhcpInfo info = wifiManager.getDhcpInfo();
                        int gatewayIP = info.gateway;
                        sendToEsp enviaraEsp = new sendToEsp(getApplicationContext(), new sendToEsp.AsyncResponse() {
                            @Override
                            public void processFinish(String output) {
                                String valores[] = output.split(",");
                                switch (valores[0]) {
                                    case "RECIBIDO": {
                                        desconectarWifi();
                                        handleResultado(valores[1], true);
                                        break;
                                    }
                                    default:{
                                        desconectarWifi();
                                        handleResultado(null, false);
                                    }
                                }
                            }
                        });
                        enviaraEsp.execute("ESP8266,=", null, intToIp(gatewayIP));// FIXME: = (13) pedir MAC
                    }
                    else{
                        handleResultado(null, false);
                    }
                }
            }
            public void onFinish() {desconectarWifi();handleResultado(null, false);}
        }.start();
    }

    private void handleAccionConfigurarSnippet(final String nombreESP, final String contrasenaESP, final String networkSSID, final String contrasenaSSID) {
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + "ESP8266" + "\"";
        conf.preSharedKey = "\""+ "12345678" +"\"";
        final WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        final int netId = wifiManager.addNetwork(conf);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();
        timer =  new CountDownTimer(60000, 7000) {
            public void onTick(long millisUntilFinished) {
                if (!recibido) {
                    if (!enviado) {
                        if (wifiManager.getConnectionInfo().getIpAddress() != 0) {
                            if (wifiManager.getConnectionInfo().getSSID().contains("ESP8266")) {
                                int gatewayIP = wifiManager.getDhcpInfo().gateway;
                                sendToEsp enviaraEsp = new sendToEsp(getApplicationContext(), new sendToEsp.AsyncResponse() {
                                    @Override
                                    public void processFinish(String output) {
                                        String resultado = output.substring(0,output.indexOf(","));
                                        switch (resultado) {
                                            case "RECIBIDO": {
                                                recibido = true;
                                                break;
                                            }
                                            default: {
                                                desconectarWifi();
                                                handleResultado(null, false);
                                            }
                                        }
                                    }
                                });
                                enviaraEsp.execute("ESP8266,26\n7" + networkSSID.replaceAll("\"","") + "7\n8" + contrasenaSSID + "8\n9" + nombreESP + "9\n10" + contrasenaESP, null, intToIp(gatewayIP));//FIXME: 2 configurar el wifi de casa en el esp
                                enviado = true;
                            } else
                                handleResultado(null, false);
                        }
                    }
                }
                else{
                    if (primerTick)
                        primerTick = false;
                    else{
                        if (wifiManager.getConnectionInfo().getSSID().contains("ESP8266")){
                            int gatewayIP = wifiManager.getDhcpInfo().gateway;
                            sendToEsp enviaraESP = new sendToEsp(getApplicationContext(), new sendToEsp.AsyncResponse() {
                                @Override
                                public void processFinish(String output) {
                                    String[] resultado = output.split(",");
                                    switch (resultado[0]) {
                                        case "CONECTADO": {
                                            recibido = true;
                                            desconectarWifi();
                                            String aux = output.replaceAll("\\.", "p");
                                            handleResultado(aux.substring(aux.indexOf(",")+1), true);//pasar toda la cadena
                                            break;
                                        }
                                        default: {
                                            desconectarWifi();
                                            handleResultado(null, false);
                                        }
                                    }
                                }
                            });
                            enviaraESP.execute("ESP8266,3ESTADO", null, intToIp(gatewayIP));//FIXME: 3 Estado si se conecto a la red
                        }
                        else{
                            wifiManager.disconnect();
                            wifiManager.enableNetwork(netId, true);
                            wifiManager.reconnect();
                            primerTick = true;
                        }
                    }
                }
            }
            public void onFinish() {desconectarWifi();}
        }.start();
    }

    private String intToIp(int i) {
        return  ((i & 0xFF) + "." +
                ((i >>> 8) & 0xFF) + "." +
                ((i >>> 16) & 0xFF) + "." +
                ((i >>> 24) & 0xFF));
    }

    private void desconectarWifi(){
        int netId = 0;
        WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.getConnectionInfo().getSSID().contains("ESP8266")) {
            netId = wifiManager.getConnectionInfo().getNetworkId();
            wifiManager.disconnect();
        }
        if (netId != 0) {
            wifiManager.disableNetwork(netId);
            wifiManager.removeNetwork(netId);
            wifiManager.saveConfiguration();//nuevo API no requiere de este comando
        }
        if (wifiManager.getConnectionInfo().getIpAddress() == 0)
            wifiManager.reconnect();
    }

    protected void handleResultado(String resultadoTexto, boolean resultadoLogico) {
        if (timer != null)
            timer.cancel();
        try {
            Message msg = new Message();
            if (resultadoTexto != null)
                msg.obj = resultadoTexto;
            if (resultadoLogico)
                msg.arg1 = 1;
            else
                msg.arg1 = 0;
            myHandler.sendMessage(msg);
            this.stopSelf();
        }catch (Exception e) {
            e.printStackTrace();
        }
        this.stopSelf();
    }

}
