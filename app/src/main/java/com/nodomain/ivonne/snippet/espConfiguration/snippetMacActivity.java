package com.nodomain.ivonne.snippet.espConfiguration;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.nodomain.ivonne.snippet.R;
import com.nodomain.ivonne.snippet.services.backgroundActivity;

import static com.nodomain.ivonne.snippet.espConfiguration.snippetNewActivity.ACTION;
import static com.nodomain.ivonne.snippet.espConfiguration.snippetNewActivity.PARAM1;
import static com.nodomain.ivonne.snippet.espConfiguration.snippetNewActivity.SHOW_MAC_CODE;
import static com.nodomain.ivonne.snippet.espConfiguration.snippetNewActivity.SHOW_MAC;

public class snippetMacActivity extends AppCompatActivity {
    private TextView snippetMAC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snippet_mac);

        TextView texto1 = (TextView) findViewById(R.id.text1);
        snippetMAC = (TextView) findViewById(R.id.checkbox_mac);

        texto1.setText(getResources().getString(R.string.mostrarMAC1)+" "+getIntent().getStringExtra(PARAM1)+" "+getResources().getString(R.string.mostrarMAC1));

        Intent intent = new Intent(snippetMacActivity.this, backgroundActivity.class);
        intent.putExtra(ACTION, SHOW_MAC);
        startActivityForResult(intent, SHOW_MAC_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case SHOW_MAC_CODE:{//MOSTRAR_MAC_CODE
                if (resultCode == RESULT_OK){
                    snippetMAC.setText(data.getStringExtra(PARAM1));
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
