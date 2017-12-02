package com.nodomain.ivonne.snippet.configuracionESP;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.nodomain.ivonne.snippet.R;
import com.nodomain.ivonne.snippet.servicios.segundoPlano;

import static com.nodomain.ivonne.snippet.configuracionESP.snippetNuevo.ACCION;
import static com.nodomain.ivonne.snippet.configuracionESP.snippetNuevo.MOSTRAR_MAC_ACCION;
import static com.nodomain.ivonne.snippet.configuracionESP.snippetNuevo.MOSTRAR_MAC_CODE;
import static com.nodomain.ivonne.snippet.configuracionESP.snippetNuevo.PARAMETRO1;

public class snippetMac extends AppCompatActivity {
    private TextView snippetMAC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snippet_mac);

        TextView texto1 = (TextView) findViewById(R.id.texto1);
        snippetMAC = (TextView) findViewById(R.id.mostrarMAC);

        texto1.setText(getResources().getString(R.string.mostrarMAC1)+" "+getIntent().getStringExtra(PARAMETRO1)+" "+getResources().getString(R.string.mostrarMAC1));

        Intent intent = new Intent(snippetMac.this, segundoPlano.class);
        intent.putExtra(ACCION, MOSTRAR_MAC_ACCION);
        startActivityForResult(intent, MOSTRAR_MAC_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case 34:{//MOSTRAR_MAC_CODE
                if (resultCode == RESULT_OK){
                    snippetMAC.setText(data.getStringExtra(PARAMETRO1));
                    setResult(RESULT_OK);
                }
                else
                    setResult(RESULT_CANCELED);
                break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
