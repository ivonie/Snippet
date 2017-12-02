package com.nodomain.ivonne.snippet.configuracionESP;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.nodomain.ivonne.snippet.objetos.Dispositivo;
import com.nodomain.ivonne.snippet.R;
import com.nodomain.ivonne.snippet.herramientas.sendToEsp;
import com.nodomain.ivonne.snippet.herramientas.dataManager;
import com.nodomain.ivonne.snippet.adaptadores.horizontalNumberPicker;
import com.nodomain.ivonne.snippet.servicios.monitorWiFi;
import com.nodomain.ivonne.snippet.timerActivity;

import static com.nodomain.ivonne.snippet.MainActivity.MONITOR_WIFI;
import static com.nodomain.ivonne.snippet.configuracionESP.snippetGuardar.SELECCIONAR_IMAGEN;

public class snippetEditar extends AppCompatActivity implements View.OnClickListener, Spinner.OnItemSelectedListener, TextWatcher{

    private EditText dispositivoNombre;
    private EditText dispositivoContrasena;
    private CheckBox editarContrasena;
    private CheckBox configuracionAvanzada;
    private Spinner dispositivoAmbiente;
    private ImageView dispositivoImagen;
    private Button guardar;
    private Button cancelar;
    private LinearLayout layoutAvanzadas;
    private Switch apagadoAutom;
    private horizontalNumberPicker selectorDeTiempo;
    private Switch encendidoAutom;
    private Button timer;

    private Dispositivo dispositivo;
    private int posicion;
    private String imagenNueva;
    private String[] listaAmbientes;
    private String contrasenaVieja;
    private dataManager myDM;
    private Boolean apagado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snippet_editar);
        dispositivoNombre = (EditText) findViewById(R.id.textoNombre);
        dispositivoContrasena = (EditText) findViewById(R.id.textoConstrasena);
        editarContrasena = (CheckBox) findViewById(R.id.editarContrasena);
        configuracionAvanzada = (CheckBox) findViewById(R.id.configuracionesAvanzadas);
        dispositivoAmbiente = (Spinner) findViewById(R.id.textoAmbiente);
        dispositivoImagen = (ImageView) findViewById(R.id.imagen);
        guardar = (Button)findViewById(R.id.botonGuardar);
        cancelar = (Button)findViewById(R.id.botonCancelar);
        layoutAvanzadas = (LinearLayout) findViewById( (R.id.layout_avanzadas));
        apagadoAutom = (Switch) findViewById(R.id.apagadoAtuomatico);
        selectorDeTiempo = (horizontalNumberPicker) findViewById(R.id.selectorTiempo);
        encendidoAutom = (Switch) findViewById(R.id.encendidoAtuomatico);
        timer = (Button) findViewById(R.id.botonTimer);

        String dispositivoMac = getIntent().getStringExtra("MAC");
        recuperarDatos(dispositivoMac);

        setTitle(getResources().getString(R.string.configurando)+" "+dispositivo.getNombre());

        dispositivoAmbiente.setOnItemSelectedListener(this);
        dispositivoImagen.setOnClickListener(this);
        guardar.setOnClickListener(this);
        cancelar.setOnClickListener(this);
        editarContrasena.setOnClickListener(this);
        configuracionAvanzada.setOnClickListener(this);
        apagadoAutom.setOnClickListener(this);
        encendidoAutom.setOnClickListener(this);
        timer.setOnClickListener(this);
        dispositivoNombre.addTextChangedListener(this);
        dispositivoContrasena.addTextChangedListener(this);
    }

    private void recuperarDatos(String mac){
        myDM = new dataManager(snippetEditar.this);
        dispositivo = myDM.getDispositivoPorMac(mac);
        dispositivoNombre.setText(dispositivo.getNombre());
        dispositivoNombre.setSelection(dispositivo.getNombre().length());
        dispositivoContrasena.setText(dispositivo.getContrasena());
        contrasenaVieja = dispositivo.getContrasena();
        dispositivoContrasena.setEnabled(false);
        imagenNueva = dispositivo.getImagen();

        listaAmbientes = getResources().getStringArray(R.array.ambientes);
        for (int i = 0; i < listaAmbientes.length; i++){
            if (listaAmbientes[i].equals(dispositivo.getAmbiente()))
                posicion = i;
        }
        dispositivoAmbiente.setSelection(posicion);

        int resID = getApplicationContext().getResources().getIdentifier(dispositivo.getImagen(), "drawable", getApplicationContext().getPackageName());
        dispositivoImagen.setImageResource(resID);

        if (dispositivo.getEncendido().equals("true"))
            encendidoAutom.setChecked(true);

        /*if (dispositivo.apagado.equals("true")) {
            apagadoAutom.setActivated(true);
            selectorDeTiempo.setValue(Integer.parseInt(dispositivo.tiempo));
        }*/

    }

    @Override
    public void onClick(View view) {
        if(view.getId() == editarContrasena.getId()){
            if (editarContrasena.isChecked()){
                dispositivoContrasena.setText("");
                dispositivoContrasena.setEnabled(true);
            }
            else{
                dispositivoContrasena.setText(dispositivo.getContrasena());
                dispositivoContrasena.setEnabled(false);
            }
        }
        else if(view.getId() == configuracionAvanzada.getId()){
            if (configuracionAvanzada.isChecked())
                layoutAvanzadas.setVisibility(View.VISIBLE);
            else
                layoutAvanzadas.setVisibility(View.GONE);
        }
        else if(view.getId() == apagadoAutom.getId()){
            apagado = apagadoAutom.isChecked();
            selectorDeTiempo.setEnabled(apagadoAutom.isChecked());
            guardar.setEnabled(true);
        }
        else if(view.getId() == encendidoAutom.getId()){
            guardar.setEnabled(true);
        }
        else if (view.getId() == dispositivoImagen.getId()){
            Intent getImagen = new Intent(snippetEditar.this,seleccionarImagen.class);
            getImagen.putExtra("TIPO",dispositivo.getTipo());
            getImagen.putExtra("ACTUAL",imagenNueva);
            startActivityForResult(getImagen,SELECCIONAR_IMAGEN);
        }
        else if (view.getId() == timer.getId()){
            Intent intent = new Intent(snippetEditar.this, timerActivity.class);
            intent.putExtra("MAC",dispositivo.getMac());
            intent.putExtra("FOO",dispositivo.getFoo());
            startActivity(intent);
        }
        else if (view.getId() == guardar.getId()){
            aplicarcambios();
        }
        else if (view.getId() == cancelar.getId()){
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    public void aplicarcambios(){
        if (!dispositivoContrasena.getText().toString().equals(dispositivo.getContrasena()))
            enviarCambiosESP("contrasena");
        else if (!dispositivoNombre.getText().toString().equals(dispositivo.getNombre()))
            enviarCambiosESP("nombre");
        else if (apagado){
            apagado = false;
            sendToEsp conectarESP = new sendToEsp(getApplicationContext(), new sendToEsp.AsyncResponse() {
                @Override
                public void processFinish(String output) {
                    String value[]= output.split(",");
                    switch (value[0]){
                        case "ON":{
                            enviarCambiosESP("apagado");
                            break;
                        }
                        case "OFF":{
                            //MENSAJE de que deberia estar encendido el foco
                            break;
                        }
                    }
                }
            });
            conectarESP.execute("ESP8266,7STATUS", dispositivo.getMac(), dispositivo.getFoo());
        }
        else if (!dispositivo.getEncendido().equals(String.valueOf(encendidoAutom.isChecked()))){
            dispositivo.setEncendido(String.valueOf(encendidoAutom.isChecked()));
            aplicarcambios();
            //TODO: FUNCION QUE manda a encender el foco al detectar la red wifi
        }
        else if (!dispositivo.getImagen().equals(imagenNueva)){
            dispositivo.setImagen(imagenNueva);
            aplicarcambios();
        }
        else if (!dispositivo.getAmbiente().equals(listaAmbientes[posicion])){
            dispositivo.setAmbiente(listaAmbientes[posicion]);
            aplicarcambios();
        }
        else if(!dispositivo.getEncendido().equals(Boolean.toString(encendidoAutom.isChecked()))){
            dispositivo.setEncendido(Boolean.toString(encendidoAutom.isChecked()));
            if (encendidoAutom.isChecked()){
                if (!MONITOR_WIFI){
                    Intent WiFiService = new Intent(snippetEditar.this, monitorWiFi.class);
                    startService(WiFiService);
                    MONITOR_WIFI = true;
                }
            }
            Log.w("EDITAR_S", "encendido automatico: "+Boolean.toString(encendidoAutom.isChecked()));
            aplicarcambios();
        }
        else{
            myDM.agregarActualizarDispositivo(dispositivo);
            guardar.setEnabled(false);
            setResult(RESULT_OK);//algo se modifico
            finish();
        }
    }

    private void enviarCambiosESP(String cambio){
        final sendToEsp enviarCambios = new sendToEsp(snippetEditar.this, new sendToEsp.AsyncResponse() {
            @Override
            public void processFinish(String output) {
                String[] valores = output.split(",");
                switch (valores[0]){
                    case "ERROR":{
                        if (!dispositivoContrasena.getText().toString().equals(contrasenaVieja)) {
                            contrasenaVieja = dispositivoContrasena.getText().toString();
                            enviarCambiosESP("contrasena");
                        } else {
                            Toast.makeText(snippetEditar.this, getResources().getString(R.string.error7), Toast.LENGTH_SHORT).show();
                            guardar.setEnabled(false);
                        }
                        break;
                    }
                    case "CORRECTO":{
                        if (valores[1].equals("PASSWORD")) {
                            dispositivo.setContrasena(dispositivoContrasena.getText().toString());
                            contrasenaVieja = dispositivo.getContrasena();
                        }
                        else if (valores[1].equals("NOMBRE"))
                            dispositivo.setNombre(dispositivoNombre.getText().toString());
                        aplicarcambios();
                        break;
                    }
                }
            }
        });
        switch (cambio){
            case "nombre":{
                enviarCambios.execute("ESP8266,49\n10"+dispositivoNombre.getText().toString()+"10\n1115\n16"+contrasenaVieja+"16\n17", dispositivo.getMac(), dispositivo.getFoo());//FIXME: 4 cambiar nombre
                break;
            }
            case "contrasena":{
                enviarCambios.execute("ESP8266,59\n10"+dispositivoContrasena.getText().toString()+"10\n1115\n16"+contrasenaVieja+"16\n17", dispositivo.getMac(), dispositivo.getFoo());//FIXME: 5 cambiar password
                break;
            }
            case "apagado":{
                WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                int esteAndroid = wifiManager.getDhcpInfo().ipAddress;
                new sendToEsp().execute("ESP8266,<13\n14"+getStringFromIP(esteAndroid)+"14\n1515\n16"+contrasenaVieja+"16\n17", dispositivo.getMac(), dispositivo.getFoo());//FIXME: < (12) configurar apagado automatico
                //TODO: si el usuario selecciono que sea permanente, guardarlo en la base de datos
                aplicarcambios();
                break;
            }
        }
    }

    @Override public void beforeTextChanged(CharSequence var1, int var2, int var3, int var4){}
    @Override public void onTextChanged(CharSequence var1, int var2, int var3, int var4){}

    @Override
    public void afterTextChanged(Editable var1){
        if (dispositivoNombre.getText().toString().equals(""))
            guardar.setEnabled(false);
        else
            guardar.setEnabled(true);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position != posicion) {
            guardar.setEnabled(true);
            posicion = position;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECCIONAR_IMAGEN){
            if (resultCode == RESULT_OK) {
                imagenNueva = data.getStringExtra("IMAGEN");
                if (!dispositivo.getImagen().equals(imagenNueva)) {
                    dispositivoImagen.setImageResource(getApplicationContext().getResources().getIdentifier(imagenNueva, "drawable", getApplicationContext().getPackageName()));
                    guardar.setEnabled(true);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private static String getStringFromIP(int i) {
        //int i = Integer.reverse(ip_reverse);
        return  ((i & 0xFF) + "." +
                ((i >>> 8) & 0xFF) + "." +
                ((i >>> 16) & 0xFF) + "." +
                ((i >>> 24) & 0xFF));
    }
}
