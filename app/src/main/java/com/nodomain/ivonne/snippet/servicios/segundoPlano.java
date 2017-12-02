package com.nodomain.ivonne.snippet.servicios;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.nodomain.ivonne.snippet.R;

import static com.nodomain.ivonne.snippet.configuracionESP.snippetNuevo.ACCION;
import static com.nodomain.ivonne.snippet.configuracionESP.snippetNuevo.CONFIGURAR_SNIPPET_ACCION;
import static com.nodomain.ivonne.snippet.configuracionESP.snippetNuevo.MOSTRAR_MAC_ACCION;
import static com.nodomain.ivonne.snippet.configuracionESP.snippetNuevo.PARAMETRO1;
import static com.nodomain.ivonne.snippet.configuracionESP.snippetNuevo.PARAMETRO2;
import static com.nodomain.ivonne.snippet.configuracionESP.snippetNuevo.PARAMETRO3;
import static com.nodomain.ivonne.snippet.configuracionESP.snippetNuevo.PARAMETRO4;
import static com.nodomain.ivonne.snippet.configuracionESP.snippetNuevo.RECONECTAR_ACCION;
import static com.nodomain.ivonne.snippet.configuracionESP.snippetNuevo.VALIDAR_CONTRASENA_ACCION;

public class segundoPlano extends AppCompatActivity implements Handler.Callback {
    private static final String TAG_INTENT_RETENIDO = "RetainedIntent";
    private retenerIntent intentRetenido;

    protected static Handler myHandler;
    private String accion;
    private Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_segundo_plano);
        this.setFinishOnTouchOutside(false);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        TextView mensaje = (TextView) findViewById(R.id.mensaje);
        Context context = this;
        myHandler = new Handler(this);
        accion = getIntent().getStringExtra(ACCION);

        FragmentManager fm = getFragmentManager();
        intentRetenido = (retenerIntent) fm.findFragmentByTag(TAG_INTENT_RETENIDO);

        if (intentRetenido == null) {
            // add the fragment
            intentRetenido = new retenerIntent();
            fm.beginTransaction().add(intentRetenido, TAG_INTENT_RETENIDO).commit();

            intent = new Intent(context, segundoPlanoService.class);
            intent.putExtra(segundoPlanoService.EXTRA_MESSENGER, new Messenger(myHandler));

            switch (accion){
                case "VALIDAR_CONTRASENA":{
                    intent.setAction(VALIDAR_CONTRASENA_ACCION);
                    String param1 = getIntent().getStringExtra(PARAMETRO1);
                    String param2 = getIntent().getStringExtra(PARAMETRO2);
                    intent.putExtra(PARAMETRO1, param1);
                    intent.putExtra(PARAMETRO2, param2);
                    context.startService(intent);
                    break;
                }
                case "RECONECTAR":{
                    intent.setAction(RECONECTAR_ACCION);
                    context.startService(intent);
                    break;
                }
                case "MOSTRAR_MAC":{
                    intent.setAction(MOSTRAR_MAC_ACCION);
                    context.startService(intent);
                    break;
                }
                case "CONFIGURAR_SNIPPET":{
                    intent.setAction(CONFIGURAR_SNIPPET_ACCION);
                    String param1 = getIntent().getStringExtra(PARAMETRO1);//nombre esp
                    String param2 = getIntent().getStringExtra(PARAMETRO2);//contraseña esp
                    String param3 = getIntent().getStringExtra(PARAMETRO3);//nombre red
                    String param4 = getIntent().getStringExtra(PARAMETRO4);//contrase{a red
                    intent.putExtra(PARAMETRO1,param1);
                    intent.putExtra(PARAMETRO2,param2);
                    intent.putExtra(PARAMETRO3,param3);
                    intent.putExtra(PARAMETRO4,param4);
                    context.startService(intent);
                    break;
                }
                case "ESCANEAR_NUEVOS":{
                    WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    DhcpInfo info = wifiManager.getDhcpInfo();
                    int intGate = info.gateway;//direccion del router en valor int
                    int intMask = info.netmask;//mascara de la red
                    int intRed = (intMask & intGate);//and de la mascara y la direccion de router para obtener la direccion de la red
                    escanearNuevos tareaDescubrirNuevos = new escanearNuevos(segundoPlano.this, new escanearNuevos.AsyncResponse() {
                        @Override
                        public void processFinish(boolean nuevo) {
                            if (nuevo)
                                setResult(RESULT_OK);
                            finish();
                        }
                    });
                    tareaDescubrirNuevos.setearRed(intMask, intRed, intGate);
                    tareaDescubrirNuevos.execute();
                    break;
                }
            }

            intentRetenido.setearIntent(intent);
        }

        switch (accion){
            case "VALIDAR_CONTRASENA":{
                mensaje.setText(getString(R.string.mensaje1));
                break;
            }
            case "RECONECTAR":{
                mensaje.setText(getString(R.string.mensaje2));
                break;
            }
            case "MOSTRAR_MAC":{
                mensaje.setText(getString(R.string.mensaje3));
                break;
            }
            case "CONFIGURAR_SNIPPET":{
                mensaje.setText(getString(R.string.mensaje4));
                break;
            }
            case "ESCANEAR_NUEVOS":{
                mensaje.setText(getString(R.string.mensaje6));
                break;
            }
        }
    }

    public static class retenerIntent extends Fragment {
        private Intent intent;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }

        public void setearIntent(Intent intent) {
            this.intent = intent;
        }

        public Intent recuperarIntent() {
            return intent;
        }

    }

    @Override
    public boolean handleMessage(Message msg){
        switch (accion){
            case "VALIDAR_CONTRASENA":{
                if (msg.arg1 == 1)
                    setResult(RESULT_OK);
                else
                    setResult(RESULT_CANCELED);
                break;
            }
            case "RECONECTAR":{
                break;
            }
            case "MOSTRAR_MAC":{
                if (msg.arg1 == 1)
                    setResult(RESULT_OK, getIntent().putExtra(PARAMETRO1,msg.obj.toString()));
                else
                    setResult(RESULT_CANCELED);
                break;
            }
            case "CONFIGURAR_SNIPPET":{
                if (msg.arg1 == 1)
                    setResult(RESULT_OK, getIntent().putExtra(PARAMETRO1,msg.obj.toString()));
                else
                    setResult(RESULT_CANCELED);
                break;
            }
        }
        finish();
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        if(isFinishing()) {
            FragmentManager fm = getFragmentManager();
            // we will not need this fragment anymore, this may also be a good place to signal
            // to the retained fragment object to perform its own cleanup.
            fm.beginTransaction().remove(intentRetenido).commit();
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        if (intent != null)
            stopService(intent);
        if (intentRetenido == null) {
            FragmentManager fm = getFragmentManager();
            fm.beginTransaction().remove(intentRetenido).commit();
        }
        super.onBackPressed();
    }
}
