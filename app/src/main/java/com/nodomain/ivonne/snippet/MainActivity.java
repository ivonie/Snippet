package com.nodomain.ivonne.snippet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;

import com.nodomain.ivonne.snippet.adaptadores.adaptadorPrincipal;
import com.nodomain.ivonne.snippet.configuracionESP.snippetNuevo;
import com.nodomain.ivonne.snippet.herramientas.dataManager;
import com.nodomain.ivonne.snippet.servicios.monitorWiFi;
import com.nodomain.ivonne.snippet.servicios.segundoPlano;

import java.util.ArrayList;
import java.util.List;

import static com.nodomain.ivonne.snippet.configuracionESP.snippetNuevo.ACCION;

//TODO: Si la vista es 2, iniciar actividad de fondo que monitorea constantemente el estado de los focos y reportará
//a actividad "desplegar" si es que esta abierta

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemClickListener,
                Spinner.OnItemSelectedListener{
    static final String SNIPPET_SharedPreferences = "SNIPPET_SharedPreferences";
    static final String ELEMENTO = "ELEMENTO";
    static final String RED = "RED";
    static public Boolean MONITOR_WIFI = false;

    static final int DISPOSITIVO_NUEVO = 71;
    static final int DESPLEGAR_DISPOSITIVOS = 72;
    static final String ESCANEAR_NUEVOS = "ESCANEAR_NUEVOS";
    static final int ESCANEAR_NUEVOS_ID = 73;

    private SharedPreferences settings;
    private ListView vista_principal;
    private String networkSSID;
    private int vista_principal_seleccionada;
    private ArrayList<String> listaPrincipal = null;
    protected Intent WiFiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        vista_principal = (ListView) findViewById(R.id.vista_principal);

        WifiManager wifiManager = (WifiManager)MainActivity.this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo currentWifi = wifiManager.getConnectionInfo();
        networkSSID = currentWifi.getSSID().replaceAll("\"","");
        if (networkSSID.contains("-EXT"))
            networkSSID = networkSSID.substring(networkSSID.indexOf("-"));
        if (networkSSID.contains("("))
            networkSSID = networkSSID.substring(networkSSID.indexOf("("));

        settings = getSharedPreferences(SNIPPET_SharedPreferences, 0);
        if (settings.getBoolean("PRIMERA", true)) {
            //TODO: Actividad que explica como funciona la app (tabbed activity)
            settings.edit().putBoolean("PRIMERA", false).apply();
        }

        vista_principal_seleccionada = settings.getInt("VISTA_PRINCIPAL", 0);
        Spinner spinner = (Spinner)findViewById(R.id.spinnerPrincipal);
        spinner.setSelection(vista_principal_seleccionada);
        spinner.setOnItemSelectedListener(this);

        inicializarVistaPrincipal();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(MainActivity.this, snippetNuevo.class),DISPOSITIVO_NUEVO);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_scan){
            Intent intent = new Intent(MainActivity.this, segundoPlano.class);
            intent.putExtra(ACCION, ESCANEAR_NUEVOS);
            startActivityForResult(intent, ESCANEAR_NUEVOS_ID);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position != vista_principal_seleccionada){
            vista_principal_seleccionada = position;
            settings.edit().putInt("VISTA_PRINCIPAL", vista_principal_seleccionada).apply();
            inicializarVistaPrincipal();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        Intent intent = new Intent(MainActivity.this, desplegarDispositivos.class);
        intent.putExtra(ELEMENTO, listaPrincipal.get(position));
        intent.putExtra(RED,networkSSID);
        startActivityForResult(intent,DESPLEGAR_DISPOSITIVOS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK)
            inicializarVistaPrincipal();
    }

    private void inicializarVistaPrincipal(){
        Log.w("MAIN", "inicializarVistaPrincipal");
        new AsyncTask<Void, Void, Void>(){
            dataManager datamanager = new dataManager(MainActivity.this);
            adaptadorPrincipal adapter;

            @Override
            protected Void doInBackground(Void ...params) {
                listaPrincipal = new ArrayList<String>();
                switch (vista_principal_seleccionada){
                    case 0: {
                        List ambientes = datamanager.getAmbientes(networkSSID);
                        if (ambientes.size() > 0){
                            for (int i = 0; i < ambientes.size(); i++) {
                                listaPrincipal.add(i,ambientes.get(i).toString());
                            }
                        }
                        break;
                    }
                    case 1: {
                        List tipos = datamanager.getTipos(networkSSID);
                        if (tipos.size() > 0){
                            for (int i = 0; i < tipos.size(); i++) {
                                listaPrincipal.add(i,tipos.get(i).toString());
                            }
                        }
                        break;
                    }
                    default: {
                        listaPrincipal.add(0,"Encendidos");
                        listaPrincipal.add(1,"Apagados");
                        listaPrincipal.add(2,"Desconectado");
                        break;
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                adapter = new adaptadorPrincipal(MainActivity.this, listaPrincipal);
                vista_principal.setAdapter(adapter);
                vista_principal.setOnItemClickListener(MainActivity.this);
                if (datamanager.encendidoAutomatico()){
                    if (!MONITOR_WIFI){
                        WiFiService = new Intent(MainActivity.this, monitorWiFi.class);
                        startService(WiFiService);
                        MONITOR_WIFI = true;
                    }
                }
                else
                    if (MONITOR_WIFI) {
                        stopService(WiFiService);
                        MONITOR_WIFI = false;
                    }
            }
        }.execute();
    }
}
