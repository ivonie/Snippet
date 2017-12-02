package com.nodomain.ivonne.snippet;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.nodomain.ivonne.snippet.adaptadores.brightPicker;
import com.nodomain.ivonne.snippet.configuracionESP.snippetEditar;
import com.nodomain.ivonne.snippet.herramientas.sendToEsp;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import static com.nodomain.ivonne.snippet.desplegarDispositivos.DISPOSITIVO_EDITAR;

public class dimmerActivity extends AppCompatActivity implements brightPicker.OnSeekBarChangeListener {
    private brightPicker selectorBrillo;
    private Socket socket;
    private String myFoo;
    private String myMac;
    private String porcentaje = "100";
    private int progressViejo;
    private String estado;

    private static final String TAG = "DIMMER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dimmer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        selectorBrillo = (brightPicker) findViewById(R.id.bright_Picker);
        //selectorBrillo.setEscala(100);

        myFoo = getIntent().getStringExtra("FOO");
        myMac = getIntent().getStringExtra("MAC");
        estado = getIntent().getStringExtra("ESTADO");
        String nombre = getIntent().getStringExtra("NOMBRE");

        toolbar.setTitle(nombre);
        setSupportActionBar(toolbar);
        //toolbar.inflateMenu(R.menu.secondary_menu);

        String mEstado = getResources().getResourceEntryName(Integer.parseInt(estado));

        if (!mEstado.contains("desconectado"))
            funcionSeleccion();
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

        if (id == R.id.action_configurar) {
            Intent intent = new Intent(dimmerActivity.this, snippetEditar.class);
            intent.putExtra("MAC",myMac);
            startActivityForResult(intent, DISPOSITIVO_EDITAR);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void funcionSeleccion (){
        selectorBrillo.setOnSeekBarChangeListener(this);

        sendToEsp conectarESP = new sendToEsp(getApplicationContext(), new sendToEsp.AsyncResponse() {
            @Override
            public void processFinish(String output) {
                porcentaje = output;
                progressViejo = Integer.parseInt(output);
                selectorBrillo.setEnabled(true);
                selectorBrillo.setBrillo(progressViejo, 100);
            }
        });
        conectarESP.execute("ESP8266,:STATUS", myMac, myFoo);//FIXME: : (10) valor del dimmer
    }

        PrintStream output;
        @Override
        public void onProgressChanged(brightPicker picker, int progressValue) {
            if (progressValue != progressViejo) {
                progressViejo = progressValue;
                asyncDimmer("onProgressChanged");
            }
        }

        @Override
        public void onStartTrackingTouch(brightPicker picker) {
            asyncDimmer("onStartTrackingTouch");
        }

        @Override
        public void onStopTrackingTouch(brightPicker picker) {
            asyncDimmer("onStopTrackingTouch");
        }

        private void asyncDimmer(final String accion){
            new AsyncTask<Void, Void, Void>(){

                @Override
                protected Void doInBackground(Void ...params) {
                    switch (accion){
                        case "onStartTrackingTouch":{
                            try {
                                InetAddress netadd = InetAddress.getByName(myFoo.replaceAll("p","."));
                                socket = new Socket(netadd, 5000);
                                output = new PrintStream(socket.getOutputStream());
                                output.println("ESP8266,8\r");//FIXME: 8 abrir el socket para el dimmer
                            } catch (UnknownHostException ex) {
                            } catch (IOException ex) {}
                            break;
                        }
                        case "onProgressChanged":{
                            if (output != null) {
                                char ch = (char) progressViejo;
                                output.print(ch);
                            }
                            break;
                        }
                        case "onStopTrackingTouch":{
                            if (output != null)
                                output.println("e\r");
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
            if (requestCode == DISPOSITIVO_EDITAR){
                setResult(RESULT_OK);
                dimmerActivity.this.finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


}
