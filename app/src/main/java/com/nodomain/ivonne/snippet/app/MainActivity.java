package com.nodomain.ivonne.snippet.app;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;

import com.nodomain.ivonne.snippet.R;
import com.nodomain.ivonne.snippet.adapters.firstScreenAdapter;
import com.nodomain.ivonne.snippet.espConfiguration.espManager;
import com.nodomain.ivonne.snippet.espConfiguration.snippetNewActivity;
import com.nodomain.ivonne.snippet.tools.dataManager;
import com.nodomain.ivonne.snippet.services.monitorWiFi;
import com.nodomain.ivonne.snippet.services.backgroundActivity;

import java.util.ArrayList;
import java.util.List;

import static com.nodomain.ivonne.snippet.espConfiguration.snippetNewActivity.ACTION;

//TODO: Si la vista es 2, iniciar actividad de fondo que monitorea constantemente el state de los
// focos y reportará a actividad "desplegar" si es que esta abierta

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemClickListener,
                Spinner.OnItemSelectedListener{
    static final String TAG = "MAIN";

    public static final String SCAN_NEW = "SCAN_NEW";

    static final String SHARED_PREF = "SHARED_PREF";
    static final String FIRST_TIME = "FIRST"; //Primer vez que se utiliza la aplciacion
    static final String DEVICE = "DEVICE";
    static final String NETWORK = "NETWORK";
    static final String MAIN_VIEW = "MAIN";

    static final int NEW_DEVICE_CODE = 11;
    static final int SHOW_DEVICES_CODE = 12;
    static final int SCAN_NEW_CODE = 13;

    static public Boolean MONITOR_WIFI = false;

    private SharedPreferences settings;
    private ListView mainView;
    private ArrayList<String> mainList = null;
    private String networkSSID;
    private int mainViewSelected;
    protected Intent wifiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        mainView = (ListView) findViewById(R.id.vista_principal);

        WifiManager wifiManager = (WifiManager)MainActivity.this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo currentWifi = wifiManager.getConnectionInfo();
        networkSSID = currentWifi.getSSID().replaceAll("\"","");
        if (networkSSID.contains("-EXT"))
            networkSSID = networkSSID.substring(networkSSID.indexOf("-"));
        if (networkSSID.contains("("))
            networkSSID = networkSSID.substring(networkSSID.indexOf("("));

        settings = getSharedPreferences(SHARED_PREF, 0);
        if (settings.getBoolean(FIRST_TIME, true)) {
            //TODO: Actividad que explica como funciona la app (tabbed activity)
            settings.edit().putBoolean(FIRST_TIME, false).apply();
        }

        mainViewSelected = settings.getInt(MAIN_VIEW, 0);
        Spinner spinner = (Spinner)findViewById(R.id.spinnerPrincipal);
        spinner.setSelection(mainViewSelected);
        spinner.setOnItemSelectedListener(this);

        new espManager(this).checkIfHomeNetwork();

        initMainView();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(MainActivity.this, snippetNewActivity.class),NEW_DEVICE_CODE);
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
            Intent intent = new Intent(MainActivity.this, backgroundActivity.class);
            intent.putExtra(ACTION, SCAN_NEW);
            startActivityForResult(intent, SCAN_NEW_CODE);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position != mainViewSelected){
            mainViewSelected = position;
            settings.edit().putInt("VISTA_PRINCIPAL", mainViewSelected).apply();
            initMainView();
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
        Intent intent = new Intent(MainActivity.this, showDevicesActivity.class);
        intent.putExtra(DEVICE, mainList.get(position));
        intent.putExtra(NETWORK,networkSSID);
        startActivityForResult(intent,SHOW_DEVICES_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK)
            initMainView();
    }

    private void initMainView(){
        new AsyncTask<Void, Void, Void>(){
            dataManager datamanager = new dataManager(MainActivity.this);
            firstScreenAdapter adapter;

            @Override
            protected Void doInBackground(Void ...params) {
                mainList = new ArrayList<String>();
                switch (mainViewSelected){
                    case 0: {
                        List houseSpaces = datamanager.getHouseSpaces(networkSSID);
                        if (houseSpaces.size() > 0){
                            for (int i = 0; i < houseSpaces.size(); i++) {
                                mainList.add(i,houseSpaces.get(i).toString());
                            }
                        }
                        break;
                    }
                    case 1: {
                        List devTypes = datamanager.getTypes(networkSSID);
                        if (devTypes.size() > 0){
                            for (int i = 0; i < devTypes.size(); i++) {
                                mainList.add(i,devTypes.get(i).toString());
                            }
                        }
                        break;
                    }
                    default: {
                        mainList.add(0,"on");
                        mainList.add(1,"off");
                        mainList.add(2,"disconnected");
                        break;
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                adapter = new firstScreenAdapter(MainActivity.this, mainList);
                mainView.setAdapter(adapter);
                mainView.setOnItemClickListener(MainActivity.this);
                if (datamanager.autoOn()){
                    if (!MONITOR_WIFI){
                        wifiService = new Intent(MainActivity.this, monitorWiFi.class);
                        startService(wifiService);
                        MONITOR_WIFI = true;
                    }
                }
                else
                    if (MONITOR_WIFI) {
                        stopService(wifiService);
                        MONITOR_WIFI = false;
                    }
            }
        }.execute();
    }
}
