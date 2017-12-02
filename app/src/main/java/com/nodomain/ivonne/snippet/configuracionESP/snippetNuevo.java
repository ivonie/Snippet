package com.nodomain.ivonne.snippet.configuracionESP;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.nodomain.ivonne.snippet.R;
import com.nodomain.ivonne.snippet.herramientas.dataManager;
import com.nodomain.ivonne.snippet.servicios.segundoPlano;

public class snippetNuevo extends AppCompatActivity implements Button.OnClickListener{

    static int RECONECTAR_CODE = 32;
    static int VALIDAR_CONTRASENA_CODE = 33;
    static int MOSTRAR_MAC_CODE = 34;
    static int CONFIGURAR_SNIPPET_CODE = 35;
    static int GUARDAR_SNIPPET_CODE = 36;
    static String GUARDAR_SNIPPET_ACCION = "GUARDAR_SNIPPET";
    public static String VALIDAR_CONTRASENA_ACCION = "VALIDAR_CONTRASENA";
    public static String MOSTRAR_MAC_ACCION = "MOSTRAR_MAC";
    public static String CONFIGURAR_SNIPPET_ACCION = "CONFIGURAR_SNIPPET";
    public static String RECONECTAR_ACCION = "RECONECTAR";
    public static String ACCION = "ACCION";
    public static String PARAMETRO1 = "PARAMETRO1";
    public static String PARAMETRO2 = "PARAMETRO2";
    public static String PARAMETRO3 = "PARAMETRO3";
    public static String PARAMETRO4 = "PARAMETRO4";

    private Button configurar;
    private EditText textoNombre;
    private EditText textoContrasena;
    private CheckBox mostrarMAC;

    private Context context = this;
    private String contrasena = "0000";
    private String networkSSID;
    private String networkPSW;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snippet_nuevo);

        configurar = (Button) findViewById(R.id.botonConfigurar);
        textoNombre = (EditText) findViewById(R.id.textoNombre);
        textoContrasena = (EditText) findViewById(R.id.textoConstrasena);
        mostrarMAC = (CheckBox) findViewById(R.id.mostrarMAC);

        textoNombre.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.equals(""))
                    configurar.setEnabled(false);
                else
                    configurar.setEnabled(true);
            }
        });

        WifiManager wifiManager = (WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo currentWifi = wifiManager.getConnectionInfo();
        networkSSID = currentWifi.getSSID();

        if (networkSSID.contains("ESP8266")){
            int netId = currentWifi.getNetworkId();
            wifiManager.disconnect();
            wifiManager.disableNetwork(netId);
            wifiManager.removeNetwork(netId);
            //wifiManager.saveConfiguration();
            wifiManager.reconnect();
        }
        else if (networkSSID.contains("unknown ssid") || networkSSID.isEmpty()) {
            new AlertDialog.Builder(snippetNuevo.this).setMessage(getResources().getString(R.string.error1))
                    .setPositiveButton(getString(R.string.reconectar), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(snippetNuevo.this, segundoPlano.class);
                            intent.putExtra(ACCION, RECONECTAR_ACCION);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton(getString(R.string.cerrar), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    }).show();
        }
        else {
            try {
                dataManager myDataManager = new dataManager(snippetNuevo.this);
                if ((networkPSW = myDataManager.getContrasena(networkSSID)).isEmpty())
                    pedirContrasena();
            }catch (Exception e) {}
        }
        configurar.setOnClickListener(this);
    }

    private void pedirContrasena() {
        View dialogview = View.inflate(this, R.layout.contrasena, null);
        final EditText inputContrasena = (EditText)dialogview.findViewById(R.id.contrasenaInput);
        final AlertDialog dialog = new AlertDialog.Builder(this, R.style.miDialogo).setView(dialogview)
                .setMessage(getString(R.string.solicitarContrasena) + " " + networkSSID)
                .setPositiveButton(getString(R.string.aceptar), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        networkPSW = inputContrasena.getText().toString();
                        if (!(networkPSW.isEmpty())){
                            Intent intent = new Intent(snippetNuevo.this, segundoPlano.class);
                            intent.putExtra(ACCION, VALIDAR_CONTRASENA_ACCION);
                            intent.putExtra(PARAMETRO1, networkSSID);
                            intent.putExtra(PARAMETRO2, networkPSW);
                            startActivityForResult(intent, VALIDAR_CONTRASENA_CODE);
                            dialog.dismiss();
                        }
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                }).create();
        dialog.show();
        //((TextView) dialog.findViewById(android.R.id.message)).setTextSize(getResources().getDimensionPixelSize(R.dimen.not_scaled_texto_normal));
        final Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
        button.setEnabled(false);
        inputContrasena.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (!s.toString().equals(""))
                    button.setEnabled(true);
                else
                    button.setEnabled(false);
            }
        });
        final CheckBox mostrarPSW = (CheckBox) dialog.findViewById(R.id.checkBoxMostrar);
        mostrarPSW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mostrarPSW.isChecked())
                    inputContrasena.setInputType(InputType.TYPE_CLASS_TEXT |
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                else
                    inputContrasena.setInputType(InputType.TYPE_CLASS_TEXT |
                            InputType.TYPE_TEXT_VARIATION_PASSWORD);
                inputContrasena.setSelection(inputContrasena.getText().length());
            }
        });
    }

    @Override
    public void onClick(View view) {
        boolean mMac = mostrarMAC.isChecked();
        if (mMac)
            mostarLaMac();
        else
            configurarSnippet();
    }

    private void mostarLaMac(){
        Intent intent = new Intent(snippetNuevo.this, snippetMac.class);
        startActivityForResult(intent, MOSTRAR_MAC_CODE);
    }

    private void configurarSnippet(){
        if (!textoContrasena.getText().toString().equals("")) {
            String aux = contrasena.concat(textoContrasena.getText().toString());
            contrasena = aux.substring(aux.length()-4);
        }
        String nombreSnippet = textoNombre.getText().toString();
        Intent intent = new Intent(snippetNuevo.this, snippetGuardar.class);
        intent.putExtra(ACCION, GUARDAR_SNIPPET_ACCION);
        intent.putExtra(PARAMETRO1, nombreSnippet);
        intent.putExtra(PARAMETRO2, contrasena);
        intent.putExtra(PARAMETRO3, networkSSID);
        intent.putExtra(PARAMETRO4, networkPSW);
        startActivityForResult(intent, GUARDAR_SNIPPET_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case 33:{//VALIDAR_CONTRASENA_CODE
                if (resultCode != RESULT_OK) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.error2), Toast.LENGTH_SHORT).show();
                    pedirContrasena();
                }
                break;
            }
            case 34:{//MOSTRAR_MAC_CODE
                if (resultCode == RESULT_OK)
                    configurarSnippet();
                else
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.error6), Toast.LENGTH_SHORT).show();
                break;
            }
            case 36: {//GUARDAR_SNIPPET_CODE
                if (resultCode == RESULT_OK) {
                    setResult(RESULT_OK, data);
                    finish();
                }
                else {
                    if (data.getStringExtra("RESULTADO").equals("CANCELADO")) {
                        Toast.makeText(getApplicationContext(), getString(R.string.cancelado), Toast.LENGTH_SHORT).show();
                        setResult(RESULT_CANCELED, data);
                        finish();
                    }
                    else
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.error5), Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}
