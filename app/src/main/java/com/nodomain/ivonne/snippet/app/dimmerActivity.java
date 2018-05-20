package com.nodomain.ivonne.snippet.app;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.nodomain.ivonne.snippet.R;
import com.nodomain.ivonne.snippet.adapters.brightPicker;
import com.nodomain.ivonne.snippet.espConfiguration.espManager;
import com.nodomain.ivonne.snippet.espConfiguration.snippetEditActivity;
import com.nodomain.ivonne.snippet.espConfiguration.sendToEsp;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import static com.nodomain.ivonne.snippet.espConfiguration.espManager.ESP_CLOSER;
import static com.nodomain.ivonne.snippet.espConfiguration.espManager.ESP_CMD_DIMMER_CLOSE;
import static com.nodomain.ivonne.snippet.espConfiguration.espManager.ESP_CMD_DIMMER_SEND;
import static com.nodomain.ivonne.snippet.espConfiguration.espManager.ESP_CMD_STATUS;
import static com.nodomain.ivonne.snippet.espConfiguration.espManager.ESP_CMD_STATUS_DIMMER;
import static com.nodomain.ivonne.snippet.espConfiguration.espManager.ESP_FOO;
import static com.nodomain.ivonne.snippet.espConfiguration.espManager.ESP_MAC;
import static com.nodomain.ivonne.snippet.espConfiguration.espManager.ESP_NAME;
import static com.nodomain.ivonne.snippet.espConfiguration.espManager.ESP_PSW;
import static com.nodomain.ivonne.snippet.espConfiguration.espManager.ESP_RES_DIMMER;

public class dimmerActivity extends AppCompatActivity implements brightPicker
        .OnSeekBarChangeListener {
    static final String PROGRESS_CHANGED = "onProgressChanged";
    static final String START_TRACKING = "onStartTrackingTouch";
    static final String STOP_TRACKING = "onStopTrackingTouch";

    private brightPicker myBrightPicker;
    private Socket mySocket;
    private String myFoo;
    private String myMac;
    private String myPsw;
    private int scale = 64;
    private int progressOld;
    PrintStream output;

    private static final String TAG = "DIMMER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dimmer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        myBrightPicker = (brightPicker) findViewById(R.id.bright_Picker);
        myBrightPicker.setScale(scale);

        myFoo = getIntent().getStringExtra(ESP_FOO);
        myMac = getIntent().getStringExtra(ESP_MAC);
        myPsw = getIntent().getStringExtra(ESP_PSW);
        String myName = getIntent().getStringExtra(ESP_NAME);

        toolbar.setTitle(myName);
        setSupportActionBar(toolbar);
        //toolbar.inflateMenu(R.menu.secondary_menu);

        new sendToEsp(this, new sendToEsp.AsyncResponse() {
            @Override
            public void processFinish(String output) {
                functionSelection();
            }
        }).execute(ESP_CMD_STATUS+myPsw+ESP_CLOSER, myMac,myFoo);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.secondary_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_configure) {
            Intent intent = new Intent(dimmerActivity.this, snippetEditActivity.class);
            intent.putExtra(ESP_MAC,myMac);
            startActivityForResult(intent, showDevicesActivity.EDIT_DEV_CODE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void functionSelection(){
        myBrightPicker.setOnSeekBarChangeListener(this);

        sendToEsp conectarESP = new sendToEsp(getApplicationContext(), new sendToEsp.AsyncResponse() {
            @Override
            public void processFinish(String output) {
                String[] resultado = output.split(",");
                switch (resultado[0]) {
                    case ESP_RES_DIMMER:{
                        progressOld = Integer.parseInt(resultado[1]);
                        myBrightPicker.setEnabled(true);
                        myBrightPicker.setBright(progressOld, scale);
                        break;
                    }
                }
            }
        });
        conectarESP.execute(ESP_CMD_STATUS_DIMMER+myPsw+ESP_CLOSER, myMac, myFoo);//FIXME: : enviar psw
    }

    @Override
    public void onProgressChanged(brightPicker picker, int progressValue) {
        if (progressValue != progressOld) {
            progressOld = progressValue;
            asyncDimmer(PROGRESS_CHANGED);
        }
    }

    @Override
    public void onStartTrackingTouch(brightPicker picker) {
        asyncDimmer(START_TRACKING);
    }

    @Override
    public void onStopTrackingTouch(brightPicker picker) {
        asyncDimmer(STOP_TRACKING);
    }

    private void asyncDimmer(final String accion){
        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void ...params) {
                switch (accion){
                    case START_TRACKING:{
                        try {
                            InetAddress netadd = InetAddress.getByName(myFoo.replaceAll("p","."));
                            mySocket = new Socket(netadd, 5000);
                            output = new PrintStream(mySocket.getOutputStream());
                            output.println(ESP_CMD_DIMMER_SEND+myPsw+ESP_CLOSER);
                        } catch (UnknownHostException ex) {
                        } catch (IOException ex) {}
                            break;
                    }
                    case PROGRESS_CHANGED:{
                        if (output != null) {
                            char ch = (char) progressOld;
                            output.print(ch);
                        }
                        break;
                    }
                    case STOP_TRACKING:{
                        if (output != null) {
                            output.println(ESP_CMD_DIMMER_CLOSE);
                            output = null;
                        }
                        break;
                    }
                }
                return null;
            }
        }.execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == showDevicesActivity.EDIT_DEV_CODE){
                setResult(RESULT_OK);
                dimmerActivity.this.finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (output != null) {
            output.println(ESP_CMD_DIMMER_CLOSE);
            output = null;
        }
        super.onBackPressed();
    }

}
