package com.nodomain.ivonne.snippet.espConfiguration;

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
import com.nodomain.ivonne.snippet.tools.dataManager;
import com.nodomain.ivonne.snippet.services.backgroundActivity;

import static com.nodomain.ivonne.snippet.espConfiguration.espManager.ESP_SSID;

public class snippetNewActivity extends AppCompatActivity implements Button.OnClickListener{

    static final String TAG = "NEW";

    public static final String SAVE_SNIPPET = "SAVE_SNIPPET";
    public static final String VALIDATE_PSW = "VALIDATE_PSW";
    public static final String SHOW_MAC = "SHOW_MAC";
    public static final String CONFIGURE_SNIPPET = "CONFIGURE_SNIPPET";
    public static final String RECONNECT = "RECONNECT";
    public static final String ACTION = "ACTION";
    public static final String PARAM1 = "PARAM1";
    public static final String PARAM2 = "PARAM2";
    public static final String PARAM3 = "PARAM3";
    public static final String PARAM4 = "PARAM4";

    public static final int SAVE_SNIPPET_CODE = 31;
    public static final int VALIDATE_PSW_CODE = 32;
    public static final int SHOW_MAC_CODE = 33;
    public static final int CONFIGURE_SNIPPET_CODE = 34;
    public static final int RECONNECT_CODE = 35;

    private Button configure;
    private EditText textName;
    private EditText textPsw;
    private CheckBox showMAC;

    private Context context = this;
    private String devPsw = "0000";
    private String networkSSID;
    private String networkPSW;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_snippet);

        configure = (Button) findViewById(R.id.button_configure);
        textName = (EditText) findViewById(R.id.text_name);
        textPsw = (EditText) findViewById(R.id.text_psw);
        showMAC = (CheckBox) findViewById(R.id.checkbox_mac);

        textName.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.equals(""))
                    configure.setEnabled(false);
                else
                    configure.setEnabled(true);
            }
        });

        WifiManager wifiManager = (WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo currentWifi = wifiManager.getConnectionInfo();
        networkSSID = currentWifi.getSSID();

        if (networkSSID.contains("unknown ssid") || networkSSID.isEmpty()) {
            new AlertDialog.Builder(snippetNewActivity.this).setMessage(getResources().getString(R.string.error1))
                    .setPositiveButton(getString(R.string.reconectar), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(snippetNewActivity.this, backgroundActivity.class);
                            intent.putExtra(ACTION, RECONNECT);
                            intent.putExtra(PARAM1, "");
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
                dataManager myDataManager = new dataManager(snippetNewActivity.this);
                if ((networkPSW = myDataManager.getPsw(networkSSID)).isEmpty())
                    askPsw();
            }catch (Exception e) {}
        }
        configure.setOnClickListener(this);
    }

    private void askPsw() {
        View dialogview = View.inflate(this, R.layout.password, null);
        final EditText inputPsw = (EditText)dialogview.findViewById(R.id.text_psw);
        final AlertDialog dialog = new AlertDialog.Builder(this, R.style.myDialog).setView(dialogview)
                .setMessage(getString(R.string.solicitarContrasena) + " " + networkSSID)
                .setPositiveButton(getString(R.string.aceptar), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        networkPSW = inputPsw.getText().toString();
                        if (!(networkPSW.isEmpty())){
                            Intent intent = new Intent(snippetNewActivity.this, backgroundActivity.class);
                            intent.putExtra(ACTION, VALIDATE_PSW);
                            intent.putExtra(PARAM1, networkSSID);
                            intent.putExtra(PARAM2, networkPSW);
                            startActivityForResult(intent, VALIDATE_PSW_CODE);
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
        inputPsw.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (!s.toString().equals(""))
                    button.setEnabled(true);
                else
                    button.setEnabled(false);
            }
        });
        final CheckBox showPsw = (CheckBox) dialog.findViewById(R.id.checkBox_show);
        showPsw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (showPsw.isChecked())
                    inputPsw.setInputType(InputType.TYPE_CLASS_TEXT |
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                else
                    inputPsw.setInputType(InputType.TYPE_CLASS_TEXT |
                            InputType.TYPE_TEXT_VARIATION_PASSWORD);
                inputPsw.setSelection(inputPsw.getText().length());
            }
        });
    }

    @Override
    public void onClick(View view) {
        boolean devMac = showMAC.isChecked();
        if (devMac)
            showMacAddress();
        else
            configureSnippet();
    }

    private void showMacAddress(){
        Intent intent = new Intent(snippetNewActivity.this, snippetMacActivity.class);
        startActivityForResult(intent, SHOW_MAC_CODE);
    }

    private void configureSnippet(){
        if (!textPsw.getText().toString().equals("")) {
            String aux = devPsw.concat(textPsw.getText().toString());
            devPsw = aux.substring(aux.length()-4);
        }
        String nombreSnippet = textName.getText().toString();
        Intent intent = new Intent(snippetNewActivity.this, snippetSaveActivity.class);
        intent.putExtra(ACTION, SAVE_SNIPPET);
        intent.putExtra(PARAM1, nombreSnippet);
        intent.putExtra(PARAM2, devPsw);
        intent.putExtra(PARAM3, networkSSID);
        intent.putExtra(PARAM4, networkPSW);
        startActivityForResult(intent, SAVE_SNIPPET_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case VALIDATE_PSW_CODE:{
                if (resultCode != RESULT_OK) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.error2), Toast.LENGTH_SHORT).show();
                    askPsw();
                }
                break;
            }
            case SHOW_MAC_CODE:{
                if (resultCode == RESULT_OK)
                    configureSnippet();
                else
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.error6), Toast.LENGTH_SHORT).show();
                break;
            }
            case SAVE_SNIPPET_CODE: {
                if (resultCode == RESULT_OK) {
                    setResult(RESULT_OK, data);
                    finish();
                }
                else {
                    if (data.getStringExtra("RESULT").equals("CANCELADO")) {
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
