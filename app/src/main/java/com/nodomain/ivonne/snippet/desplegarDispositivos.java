package com.nodomain.ivonne.snippet;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;

import com.nodomain.ivonne.snippet.configuracionESP.snippetEditar;
import com.nodomain.ivonne.snippet.herramientas.sendToEsp;
import com.nodomain.ivonne.snippet.configuracionESP.snippetNuevo;
import com.nodomain.ivonne.snippet.herramientas.dataManager;
import com.nodomain.ivonne.snippet.herramientas.funcionesAuxiliares;
import com.nodomain.ivonne.snippet.objetos.Dispositivo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nodomain.ivonne.snippet.MainActivity.DISPOSITIVO_NUEVO;
import static com.nodomain.ivonne.snippet.MainActivity.ELEMENTO;
import static com.nodomain.ivonne.snippet.MainActivity.RED;
import static com.nodomain.ivonne.snippet.MainActivity.SNIPPET_SharedPreferences;

//TODO: Iniciar actividad de fondo que monitorea constantemente el estado de los focos y reportará
//a actividad "desplegar" si es que esta abierta

public class desplegarDispositivos extends AppCompatActivity implements AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener, Spinner.OnItemSelectedListener {
    protected static String RECUPERAR_LISTA = "RECUPERAR_LISTA";
    protected static String INICIALIZAR_LISTA = "INICIALIZAR_LISTA";
    protected static String ACTUALIZAR = "ACTUALIZAR";
    protected static String ACTUALIZAR_ESTADOS = "ACTUALIZAR_ESTADOS";

    static int DISPOSITIVO_EDITAR = 51;
    static int ACTIVIDAD_DIMMER = 52;

    private SharedPreferences settings;
    private ListView vistaDeLista;
    private GridView vistaDeCuadros;
    protected Bundle imagenes_almacenadas;

    private int vista_principal_seleccionada;
    private int vista_secundaria_seleccionada;
    private String elemento;
    private String red;
    private List<Map<String, String>> listaDispositivos = null;
    private boolean longclick = false;
    private funcionesAuxiliares misFunciones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.w("DESPLEGAR","onCreate");
        setContentView(R.layout.activity_desplegar_dispositivos);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        vistaDeLista = (ListView)findViewById(R.id.vistaLista);
        vistaDeCuadros = (GridView)findViewById(R.id.vistaCuadros);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(desplegarDispositivos.this, snippetNuevo.class),DISPOSITIVO_NUEVO);
            }
        });

        settings = getSharedPreferences(SNIPPET_SharedPreferences, 0);
        vista_secundaria_seleccionada = settings.getInt("VISTA_SECUNDARIA", 0);
        vista_principal_seleccionada = settings.getInt("VISTA_PRINCIPAL", 0);

        Spinner spinner = (Spinner)findViewById(R.id.spinnerSecundario);
        spinner.setSelection(vista_secundaria_seleccionada);
        spinner.setOnItemSelectedListener(this);

        elemento = getIntent().getStringExtra(ELEMENTO);
        red = getIntent().getStringExtra(RED);

        setTitle(elemento);

        misFunciones = new funcionesAuxiliares();

        if (savedInstanceState != null) {
            Log.w("savedInstanceState", "true");
            imagenes_almacenadas = savedInstanceState;
            llenarLista(RECUPERAR_LISTA);
            llenarLista(ACTUALIZAR_ESTADOS);
        }
        else
            llenarLista(INICIALIZAR_LISTA);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        if (!longclick) {
            final ImageView imagen = (ImageView) view.findViewById(R.id.imagen);
            String tipo = listaDispositivos.get(position).get("tipo");
            sendToEsp conectarESP = new sendToEsp(getApplicationContext(), new sendToEsp.AsyncResponse() {
                @Override
                public void processFinish(String output) {
                    cambiarImagen(imagen, position, output);
                }
            });
            switch (tipo) {
                //case "FOCO": {//manda una instruccion y modifica su vista acorde a ésta
                case "FOCO": {
                    conectarESP.execute("ESP8266,1TOOGLE", listaDispositivos.get(position).get("mac"), listaDispositivos.get(position).get("foo"));//FIXME: 1 encender/apagar el foco
                    break;
                }
                //case "DIMMER": {//se inicia actividad para poder mantener una comunicacion abierta con el dispositivo
                case "DIMMER": {
                    Intent intent = new Intent(desplegarDispositivos.this, dimmerActivity.class);
                    intent.putExtra("MAC", listaDispositivos.get(position).get("mac"));
                    intent.putExtra("FOO", listaDispositivos.get(position).get("foo"));
                    intent.putExtra("ESTADO", listaDispositivos.get(position).get("estado"));
                    intent.putExtra("NOMBRE", listaDispositivos.get(position).get("nombre"));
                    startActivityForResult(intent, ACTIVIDAD_DIMMER);
                    break;
                }
            }
        }
        longclick = false;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        if (Build.VERSION.SDK_INT >= 26) {
            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(50,VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(50);
        }
        longclick = true;
        new AlertDialog.Builder(desplegarDispositivos.this, R.style.miDialogo)
                .setMessage(getResources().getString(R.string.pregunta_editar)+" "+listaDispositivos.get(position).get("nombre")+"?")
                .setNegativeButton(getResources().getString(R.string.borrar), new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int id) {
                        new dataManager(desplegarDispositivos.this).borrar(listaDispositivos.get(position).get("mac"));
                        listaDispositivos.remove(position);
                        llenarLista(ACTUALIZAR);
                        setResult(RESULT_OK);
                    }})
                .setPositiveButton(getResources().getString(R.string.configurar), new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(desplegarDispositivos.this, snippetEditar.class);
                        intent.putExtra("MAC",listaDispositivos.get(position).get("mac"));
                        startActivityForResult(intent, DISPOSITIVO_EDITAR);
                    }})
                .show();
        return false;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position != vista_secundaria_seleccionada){
            vista_secundaria_seleccionada = position;
            settings.edit().putInt("VISTA_SECUNDARIA", vista_secundaria_seleccionada).apply();
            llenarLista(ACTUALIZAR);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

    private void llenarLista(final String accion){
        new AsyncTask<Void, Void, Void>(){
            dataManager datamanager = new dataManager(desplegarDispositivos.this);
            SimpleAdapter adaptadorSegundo;

            @Override
            protected void onPreExecute(){
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
                //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
            }

            @Override
            protected Void doInBackground(Void ...params) {
                String[] valores = accion.split(",");
                switch (valores[0]){
                    case "DISPOSITIVO_NUEVO": {
                        agregarNuevo(valores[1]);
                        break;
                    }
                    case "ACTUALIZAR": {//solo manda a actualizar el adaptador por si se cambio la vista o si se borro un elemento
                        break;
                    }
                    case "ACTUALIZAR_ESTADOS": {
                        actualizarEstados();
                        break;
                    }
                    case "RECUPERAR_LISTA": {
                        inicializarLista(true);//hay valores almacenados en imagenes_almacenadas
                        break;
                    }
                    default: {
                        inicializarLista(false);//NO hay valores almacenados en imagenes_almacenadas
                        break;
                    }
                }
                return null;
            }

            private void agregarNuevo(String myMac){
                Dispositivo dispositivoNuevo = datamanager.getDispositivoPorMac(myMac);
                if (dispositivoNuevo.getTipo().equals(elemento) || dispositivoNuevo.getAmbiente().equals(elemento)) {
                    int posicion = listaDispositivos.indexOf(myMac);
                    if (posicion == -1)//dispositivo no existe en la lista, lo que quiere decir que es nuevo
                        posicion = listaDispositivos.size();
                    Map<String, String> datum = new HashMap<>(2);
                    datum.put("nombre", dispositivoNuevo.getNombre());
                    datum.put("mac", dispositivoNuevo.getMac());
                    datum.put("foo", dispositivoNuevo.getFoo());
                    datum.put("tipo", dispositivoNuevo.getTipo());
                    datum.put("imagen", dispositivoNuevo.getImagen());
                    datum.put("estado", solicitarEstado(datum, posicion));//ubicacion del elemento que se esta reescribiendo
                    listaDispositivos.add(posicion, datum);
                }
            }

            private void inicializarLista(boolean recuperando){
                List<Dispositivo> dispositivos;
                switch (vista_principal_seleccionada){
                    case 0:{
                        dispositivos = datamanager.getDispositivosPorAmbiente(red, elemento);
                        break;
                    }
                    default:{
                        dispositivos = datamanager.getDispositivosPorTipo(red, elemento);
                        break;
                    }
                }
                listaDispositivos = new ArrayList<>();
                if (dispositivos.size() > 0) {
                    for (int i = 0; i < dispositivos.size(); i++) {
                        String fooValidada = dispositivos.get(i).getFoo();
                        if (!recuperando)
                            fooValidada = misFunciones.getIPvalida(dispositivos.get(i).getMac(), dispositivos.get(i).getFoo());
                        if (!fooValidada.equals(dispositivos.get(i).getFoo()) & !fooValidada.equals(""))
                            datamanager.actualizarIP(dispositivos.get(i).getMac(),fooValidada);//
                        Map<String, String> datum = new HashMap<>(2);
                        datum.put("nombre", dispositivos.get(i).getNombre());
                        datum.put("mac", dispositivos.get(i).getMac());
                        datum.put("foo", fooValidada);
                        datum.put("tipo", dispositivos.get(i).getTipo());
                        datum.put("imagen", dispositivos.get(i).getImagen());
                        if (recuperando)
                            datum.put("estado", recuperarEstado(dispositivos.get(i).getMac(),dispositivos.get(i).getImagen()));
                        else
                            datum.put("estado", solicitarEstado(datum, i));
                        listaDispositivos.add(i,datum);
                    }
                }
            }

            private void actualizarEstados(){
                for (int i = 0; i < listaDispositivos.size(); i++) {
                    Map<String, String> datum = new HashMap<>(2);
                    datum.put("mac", listaDispositivos.get(i).get("mac"));
                    datum.put("foo", listaDispositivos.get(i).get("foo"));
                    datum.put("imagen", listaDispositivos.get(i).get("imagen"));
                    solicitarEstado(datum, i);
                }
            }

            private String solicitarEstado(Map<String, String> dispositivo, final int posicion){
                final String imagen_pre = dispositivo.get("imagen");
                int idImagen = getApplicationContext().getResources().getIdentifier(imagen_pre+"_desconectado", "drawable", getApplicationContext().getPackageName());
                String imagen = String.valueOf(idImagen);
                sendToEsp conectarESP = new sendToEsp(getApplicationContext(), new sendToEsp.AsyncResponse() {
                    @Override
                    public void processFinish(String output) {
                        String value[]= output.split(",");
                        switch (value[0]){
                            case "ON":{
                                int idImagen = getApplicationContext().getResources().getIdentifier(imagen_pre+"_on", "drawable", getApplicationContext().getPackageName());
                                listaDispositivos.get(posicion).put("estado",String.valueOf(idImagen));
                                if (adaptadorSegundo != null)
                                    adaptadorSegundo.notifyDataSetChanged();
                                break;
                            }
                            case "OFF":{
                                int idImagen = getApplicationContext().getResources().getIdentifier(imagen_pre+"_off", "drawable", getApplicationContext().getPackageName());
                                listaDispositivos.get(posicion).put("estado",String.valueOf(idImagen));
                                if (adaptadorSegundo != null)
                                    adaptadorSegundo.notifyDataSetChanged();
                                break;
                            }
                        }
                    }
                });
                conectarESP.execute("ESP8266,7STATUS", dispositivo.get("mac"), dispositivo.get("foo"));//FIXME: 7 estatus del Snippet
                return imagen;
            }

            private String recuperarEstado(String mac, String imagen_pre){
                String imagen = imagenes_almacenadas.getString(mac + "_Imagen");
                if (imagen == null) {
                    int idImagen = getApplicationContext().getResources().getIdentifier(imagen_pre + "_desconectado", "drawable", getApplicationContext().getPackageName());
                    imagen = String.valueOf(idImagen);
                }
                else if (imagen.equals("")) {
                    int idImagen = getApplicationContext().getResources().getIdentifier(imagen_pre + "_desconectado", "drawable", getApplicationContext().getPackageName());
                    imagen = String.valueOf(idImagen);
                }
                return imagen;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                if (vista_secundaria_seleccionada == 0) {
                    adaptadorSegundo = new SimpleAdapter(desplegarDispositivos.this, listaDispositivos, R.layout.vista_lista, new String[]{"nombre", "estado"}, new int[]{R.id.nombre, R.id.imagen});
                    vistaDeLista.setAdapter(adaptadorSegundo);
                    vistaDeLista.setOnItemClickListener(desplegarDispositivos.this);
                    vistaDeLista.setOnItemLongClickListener(desplegarDispositivos.this);
                    vistaDeLista.setVisibility(View.VISIBLE);
                    vistaDeCuadros.setVisibility(View.INVISIBLE);
                }
                else{
                    adaptadorSegundo = new SimpleAdapter(desplegarDispositivos.this, listaDispositivos, R.layout.vista_cuadro, new String[]{"nombre", "estado"}, new int[]{R.id.nombre, R.id.imagen});
                    vistaDeCuadros.setAdapter(adaptadorSegundo);
                    vistaDeCuadros.setOnItemClickListener(desplegarDispositivos.this);
                    vistaDeCuadros.setOnItemLongClickListener(desplegarDispositivos.this);
                    vistaDeLista.setVisibility(View.INVISIBLE);
                    vistaDeCuadros.setVisibility(View.VISIBLE);
                }
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }
        }.execute();
    }

    private void cambiarImagen(ImageView imagen, int posicion, String estado){
        String imagen_pre = listaDispositivos.get(posicion).get("imagen");
        int idImagen = getApplicationContext().getResources().getIdentifier(imagen_pre+"_desconectado", "drawable", getApplicationContext().getPackageName());
        switch (estado) {
            case "ON": {
                idImagen = getApplicationContext().getResources().getIdentifier(imagen_pre+"_on", "drawable", getApplicationContext().getPackageName());
                break;
            }
            case "OFF": {
                idImagen = getApplicationContext().getResources().getIdentifier(imagen_pre+"_off", "drawable", getApplicationContext().getPackageName());
                break;
            }
        }
        imagen.setImageResource(idImagen);
        listaDispositivos.get(posicion).put("estado", String.valueOf(idImagen));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == DISPOSITIVO_NUEVO) {
                llenarLista(DISPOSITIVO_NUEVO + "," + data.getStringExtra("MAC"));
                setResult(RESULT_OK);//notifica a la actividad anterior que algo se modifico
            }
            else if (requestCode == DISPOSITIVO_EDITAR){
                llenarLista(INICIALIZAR_LISTA);
                setResult(RESULT_OK);
            }
            else if (requestCode == ACTIVIDAD_DIMMER){
                llenarLista(INICIALIZAR_LISTA);
                setResult(RESULT_OK);//notifica a la actividad anterior que algo se modifico
            }
        }
        // siempre actualizar estado de todos los ESP al regresar a esta vista
        llenarLista(ACTUALIZAR_ESTADOS);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        Log.w("DESPLEGAR","onSaveInstanceState");
        if (listaDispositivos != null)
            for (int i = 0; i < listaDispositivos.size(); i++){
                outState.putString(listaDispositivos.get(i).get("mac")+"_Imagen",listaDispositivos.get(i).get("estado"));
            }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.w("DESPLEGAR","onConfigurationChanged");

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){

        }
    }

}

/*
Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
 */