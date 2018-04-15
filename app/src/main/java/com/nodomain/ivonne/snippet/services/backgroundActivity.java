package com.nodomain.ivonne.snippet.services;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.nodomain.ivonne.snippet.R;
import com.nodomain.ivonne.snippet.espConfiguration.espManager;

import static com.nodomain.ivonne.snippet.app.MainActivity.SCAN_NEW;
import static com.nodomain.ivonne.snippet.app.showDevicesActivity.CORRECT_IP;
import static com.nodomain.ivonne.snippet.espConfiguration.snippetNewActivity.ACTION;
import static com.nodomain.ivonne.snippet.espConfiguration.snippetNewActivity.CONFIGURE_SNIPPET;
import static com.nodomain.ivonne.snippet.espConfiguration.snippetNewActivity.PARAM1;
import static com.nodomain.ivonne.snippet.espConfiguration.snippetNewActivity.PARAM2;
import static com.nodomain.ivonne.snippet.espConfiguration.snippetNewActivity.PARAM3;
import static com.nodomain.ivonne.snippet.espConfiguration.snippetNewActivity.PARAM4;
import static com.nodomain.ivonne.snippet.espConfiguration.snippetNewActivity.RECONNECT;
import static com.nodomain.ivonne.snippet.espConfiguration.snippetNewActivity.SHOW_MAC;
import static com.nodomain.ivonne.snippet.espConfiguration.snippetNewActivity.VALIDATE_PSW;

public class backgroundActivity extends AppCompatActivity implements Handler.Callback {
    static final String TAG = "BACKGROUND";
    static final String HIDDEN = "HIDDEN";
    private static final String TAG_RETAINED_INTENT = "RetainedIntent";
    private retainIntent intentRetenided;

    protected static Handler myHandler;
    private String action;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_background);
        this.setFinishOnTouchOutside(false);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        TextView text = (TextView) findViewById(R.id.text);
        Context context = this;
        myHandler = new Handler(this);
        action = getIntent().getStringExtra(ACTION);
        boolean hidden = getIntent().getBooleanExtra(HIDDEN,false);
        if (hidden)
            findViewById(R.id.wait_layout).setVisibility(View.GONE);

        FragmentManager fm = getFragmentManager();
        intentRetenided = (retainIntent) fm.findFragmentByTag(TAG_RETAINED_INTENT);

        if (intentRetenided == null) {
            // add the fragment
            intentRetenided = new retainIntent();
            fm.beginTransaction().add(intentRetenided, TAG_RETAINED_INTENT).commit();

            intent = new Intent(context, backgroundService.class);
            intent.putExtra(backgroundService.EXTRA_MESSENGER, new Messenger(myHandler));
            Log.w(TAG,action);
            switch (action){
                case VALIDATE_PSW:{
                    intent.setAction(VALIDATE_PSW);
                    String param1 = getIntent().getStringExtra(PARAM1);
                    String param2 = getIntent().getStringExtra(PARAM2);
                    intent.putExtra(PARAM1, param1);
                    intent.putExtra(PARAM2, param2);
                    context.startService(intent);
                    break;
                }
                case RECONNECT:{
                    intent.setAction(RECONNECT);
                    String param1 = getIntent().getStringExtra(PARAM1);
                    intent.putExtra(PARAM1, param1);
                    context.startService(intent);
                    break;
                }
                case SHOW_MAC:{
                    intent.setAction(SHOW_MAC);
                    context.startService(intent);
                    break;
                }
                case CONFIGURE_SNIPPET:{
                    intent.setAction(CONFIGURE_SNIPPET);
                    String param1 = getIntent().getStringExtra(PARAM1);//nombre esp
                    String param2 = getIntent().getStringExtra(PARAM2);//contraseña esp
                    String param3 = getIntent().getStringExtra(PARAM3);//nombre red
                    String param4 = getIntent().getStringExtra(PARAM4);//contrase{a red
                    intent.putExtra(PARAM1,param1);
                    intent.putExtra(PARAM2,param2);
                    intent.putExtra(PARAM3,param3);
                    intent.putExtra(PARAM4,param4);
                    context.startService(intent);
                    break;
                }
                case SCAN_NEW:{
                    WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    DhcpInfo info = wifiManager.getDhcpInfo();
                    int intGate = info.gateway;//direccion del router en valor int
                    int intMask = info.netmask;//mascara de la red
                    int intRed = (intMask & intGate);//and de la mascara y la direccion de router para obtener la direccion de la red
                    scanNew scanNewtask = new scanNew(backgroundActivity.this, new scanNew.AsyncResponse() {
                        @Override
                        public void processFinish(boolean nuevo) {
                            if (nuevo)
                                setResult(RESULT_OK);
                            finish();
                        }
                    });
                    scanNewtask.setearRed(intMask, intRed, intGate);
                    scanNewtask.execute();
                    break;
                }
                case CORRECT_IP:{
                    final String macABuscar = getIntent().getStringExtra(PARAM1);//mac a buscar
                    WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    DhcpInfo info = wifiManager.getDhcpInfo();
                    int intGate = info.gateway;//direccion del router en valor int
                    int intMask = info.netmask;//mascara de la red
                    int intRed = (intMask & intGate);//and de la mascara y la direccion de router para obtener la direccion de la red
                    scanOne scanOneTask = new scanOne(new scanOne.AsyncResponse() {
                        @Override
                        public void processFinish(String IpCorregida) {
                            if (!IpCorregida.isEmpty())
                                setResult(RESULT_OK, getIntent().putExtra(PARAM1, IpCorregida)
                                        .putExtra(PARAM2, macABuscar));
                            finish();
                        }
                    });
                    scanOneTask.setearValores(intMask, intRed, intGate, macABuscar);
                    scanOneTask.execute();
                    break;
                }
            }

            intentRetenided.setIntent(intent);
        }

        switch (action){
            case VALIDATE_PSW:{
                text.setText(getString(R.string.mensaje1));
                break;
            }
            case RECONNECT:{
                text.setText(getString(R.string.mensaje2));
                break;
            }
            case SHOW_MAC:{
                text.setText(getString(R.string.mensaje3));
                break;
            }
            case CONFIGURE_SNIPPET:{
                text.setText(getString(R.string.mensaje4));
                break;
            }
            case SCAN_NEW:{
                text.setText(getString(R.string.mensaje6));
                break;
            }
            case CORRECT_IP:{
                text.setText(getString(R.string.mensaje8));
                break;
            }
        }
    }

    public static class retainIntent extends Fragment {
        private Intent intent;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }

        public void setIntent(Intent intent) {
            this.intent = intent;
        }

        public Intent recoverIntent() {
            return intent;
        }

    }

    @Override
    public boolean handleMessage(Message msg){
        if (msg.arg1 == 0) {
            setResult(RESULT_CANCELED);
        }
        else {
            switch (action) {
                case VALIDATE_PSW: {
                    setResult(RESULT_OK);
                    break;
                }
                case RECONNECT: {
                    break;
                }
                case SHOW_MAC: {
                    setResult(RESULT_OK, getIntent().putExtra(PARAM1, msg.obj.toString()));
                    break;
                }
                case CONFIGURE_SNIPPET: {
                    setResult(RESULT_OK, getIntent().putExtra(PARAM1, msg.obj.toString()));
                    break;
                }
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
            fm.beginTransaction().remove(intentRetenided).commit();
        }
    }

    @Override
    public void onBackPressed() {
        new espManager(this).borrarEspWifi();
        setResult(RESULT_CANCELED);
        if (intent != null)
            stopService(intent);
        if (intentRetenided == null) {
            FragmentManager fm = getFragmentManager();
            fm.beginTransaction().remove(intentRetenided).commit();
        }
        super.onBackPressed();
    }
}
