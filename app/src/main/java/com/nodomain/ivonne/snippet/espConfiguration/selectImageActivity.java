package com.nodomain.ivonne.snippet.espConfiguration;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

import com.nodomain.ivonne.snippet.R;
import com.nodomain.ivonne.snippet.adapters.imageAdapter;

public class selectImageActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    private imageAdapter miAdaptador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seleccionar_imagen);
        GridView imagenes = (GridView) findViewById(R.id.layout_imagenes);

        miAdaptador = new imageAdapter(this, getIntent().getStringExtra("TIPO"), getIntent().getStringExtra("ACTUAL"));
        imagenes.setAdapter(miAdaptador);
        imagenes.setOnItemClickListener(this);

        Button guardar = (Button) findViewById(R.id.button_save);
        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Button cancelar = (Button) findViewById(R.id.botonCancelar);
        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        String imagen = (String) miAdaptador.getItem(position);
        miAdaptador.setSelectedItem(position);
        miAdaptador.notifyDataSetChanged();
        setResult(RESULT_OK, getIntent().putExtra("IMAGEN",imagen));
        //finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
    }
}
