package com.nodomain.ivonne.snippet.espConfiguration;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.nodomain.ivonne.snippet.objects.Device;
import com.nodomain.ivonne.snippet.R;
import com.nodomain.ivonne.snippet.tools.dataManager;
import com.nodomain.ivonne.snippet.services.backgroundActivity;

import static com.nodomain.ivonne.snippet.espConfiguration.espManager.ESP_MAC;
import static com.nodomain.ivonne.snippet.espConfiguration.espManager.ESP_RES_FAILED;
import static com.nodomain.ivonne.snippet.espConfiguration.espManager.ESP_TYPE;
import static com.nodomain.ivonne.snippet.espConfiguration.snippetNewActivity.ACTION;
import static com.nodomain.ivonne.snippet.espConfiguration.snippetNewActivity.CONFIGURE_SNIPPET_CODE;
import static com.nodomain.ivonne.snippet.espConfiguration.snippetNewActivity.CONFIGURE_SNIPPET;
import static com.nodomain.ivonne.snippet.espConfiguration.snippetNewActivity.PARAM1;
import static com.nodomain.ivonne.snippet.espConfiguration.snippetNewActivity.PARAM2;
import static com.nodomain.ivonne.snippet.espConfiguration.snippetNewActivity.PARAM3;
import static com.nodomain.ivonne.snippet.espConfiguration.snippetNewActivity.PARAM4;
import static com.nodomain.ivonne.snippet.espConfiguration.snippetNewActivity.RECONNECT_CODE;

public class snippetSaveActivity extends AppCompatActivity implements Button.OnClickListener,
        Spinner.OnItemSelectedListener {
    static final String TAG = "SAVE";

    static final int SELECT_IMAGE_CODE = 41;

    private LinearLayout myLayout;
    private TextView text1;
    private Button save;
    private ImageView myImageView;

    private String snippetFoo;
    private String snippetMac;
    private String snippetType;
    private String snippetName;
    private String snippetPsw = "----";
    private String networkName;
    private String networkPsw;
    private String snippetImage;

    private Boolean retry = true;
    private int position = 0;
    private Boolean dontSave = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_snippet);

        myLayout = (LinearLayout) findViewById(R.id.layout_save);
        text1 = (TextView) findViewById(R.id.text1);
        myImageView = (ImageView) findViewById(R.id.image_device);
        Spinner space = (Spinner) findViewById(R.id.text_space);
        save = (Button) findViewById(R.id.button_save);
        save.setOnClickListener(this);

        snippetName = getIntent().getStringExtra(PARAM1);
        snippetPsw = getIntent().getStringExtra(PARAM2);
        networkName = getIntent().getStringExtra(PARAM3);
        networkPsw = getIntent().getStringExtra(PARAM4);

        Intent intent = new Intent(snippetSaveActivity.this, backgroundActivity.class);
        intent.putExtra(ACTION, CONFIGURE_SNIPPET);
        intent.putExtra(PARAM1, snippetName);
        intent.putExtra(PARAM2, snippetPsw);
        intent.putExtra(PARAM3, networkName);
        intent.putExtra(PARAM4, networkPsw);
        startActivityForResult(intent, CONFIGURE_SNIPPET_CODE);

        myImageView.setOnClickListener(this);
        space.setOnItemSelectedListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view.getId()==save.getId())
            addSnippet();
        else if(view.getId()==myImageView.getId()){
            Intent getImagen = new Intent(snippetSaveActivity.this,selectImageActivity.class);
            getImagen.putExtra(ESP_TYPE,snippetType);
            getImagen.putExtra("CURRENT",snippetImage);
            startActivityForResult(getImagen,SELECT_IMAGE_CODE);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        this.position = position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

    private boolean readData(String data){
        String[] value = data.split(",");
        Log.w(TAG,data);
        if (value[0].equals(ESP_RES_FAILED))
            return false;//mesage received was "FAILCONNECTTOAP"
        else {
            snippetFoo = value[0];
            if (value.length > 1)
                snippetMac = value[1];
            if (value.length > 2) {
                snippetType = value[2];
                myImageView.setImageResource(getApplicationContext().getResources()
                        .getIdentifier(snippetType.toLowerCase() + "0", "drawable",
                                getApplicationContext().getPackageName()));
                snippetImage = snippetType.toLowerCase() + "0";
            }
            text1.setText(getString(R.string.mostrarGUARDAR1) + " " + snippetType.toLowerCase() + " \"" + snippetName +
                    "\" " + getString(R.string.mostrarGUARDAR2));
            return true;
        }
    }

    private void addSnippet(){
        Device newDevice = new Device();
        newDevice.setDevNetwork(networkName);
        newDevice.setDevName(snippetName);
        newDevice.setDevType(snippetType);
        newDevice.setDevFoo(snippetFoo);
        newDevice.setDevMac(snippetMac);
        newDevice.setDevPsw(snippetPsw);
        String[] listaAmbientes = getResources().getStringArray(R.array.ambientes);
        newDevice.setDevHouseSpace(listaAmbientes[position]);
        newDevice.setDevImage(snippetImage);
        dataManager myDM = new dataManager(snippetSaveActivity.this);
        myDM.addorUpdateDevice(newDevice);
        setResult(RESULT_OK, getIntent().putExtra(ESP_MAC,newDevice.getDevMac()));
        finish();
    }

    private void retryConfigure(){
        if (retry) {
            Log.w(TAG,"Failed, retrying");
            retry = false;
            Toast.makeText(getApplicationContext(), getResources()
                    .getString(R.string.error4), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(snippetSaveActivity.this, backgroundActivity.class);
            intent.putExtra(ACTION, CONFIGURE_SNIPPET);
            intent.putExtra(PARAM1, snippetName);
            intent.putExtra(PARAM2, snippetPsw);
            intent.putExtra(PARAM3, networkName);
            intent.putExtra(PARAM4, networkPsw);
            startActivityForResult(intent, CONFIGURE_SNIPPET_CODE);
        }
        else {
            setResult(RESULT_CANCELED,getIntent().putExtra("RESULT","ERROR"));
            reconnectToLocalWiFi();
            finish();
        }
    }

    private void reconnectToLocalWiFi(){
        Intent intent = new Intent(snippetSaveActivity.this, backgroundActivity.class);
        intent.putExtra(ACTION, snippetNewActivity.RECONNECT);
        intent.putExtra(PARAM1, networkName);
        startActivityForResult(intent, RECONNECT_CODE);//reconnect to local network
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case CONFIGURE_SNIPPET_CODE: {
                if (resultCode == RESULT_OK) {
                    Boolean success = readData(data.getStringExtra(PARAM1));
                    if (!success){
                        retry = true;
                        retryConfigure();
                    }
                    else {
                        Toast.makeText(getApplicationContext(), getResources()
                                .getString(R.string.mensaje5), Toast.LENGTH_SHORT).show();
                        reconnectToLocalWiFi();
                    }
                }
                else {
                    retryConfigure();
                }
                break;
            }
            case SELECT_IMAGE_CODE:{//SELECCIONAR_IMAGEN
                if (resultCode == RESULT_OK) {
                    snippetImage = data.getStringExtra("IMAGE");
                    myImageView.setImageResource(getApplicationContext().getResources()
                            .getIdentifier(snippetImage, "drawable", getApplicationContext()
                                    .getPackageName()));
                }
                break;
            }
            case RECONNECT_CODE:{//RECONECTAR}
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (dontSave) {
            //Snackbar.make(miLayout, getResources().getString(R.string.guardarforzoso), Snackbar.LENGTH_SHORT).show();
            Toast.makeText(getApplicationContext(), getString(R.string.guardarforzoso), Toast.LENGTH_LONG).show();
            dontSave = false;
        }
        else{
            setResult(RESULT_CANCELED,getIntent().putExtra("RESULT","CANCELED"));
            super.onBackPressed();
        }
    }
}
