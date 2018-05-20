package com.nodomain.ivonne.snippet.espConfiguration;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

import com.nodomain.ivonne.snippet.R;
import com.nodomain.ivonne.snippet.adapters.imageAdapter;

import static com.nodomain.ivonne.snippet.espConfiguration.espManager.ESP_TYPE;

public class selectImageActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    private imageAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seleccionar_imagen);
        GridView imagenes = (GridView) findViewById(R.id.layout_imagenes);

        myAdapter = new imageAdapter(this, getIntent().getStringExtra(ESP_TYPE), getIntent().getStringExtra("CURRENT"));
        imagenes.setAdapter(myAdapter);
        imagenes.setOnItemClickListener(this);

        Button save = (Button) findViewById(R.id.button_save);
        save.setOnClickListener(new View.OnClickListener() {
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
        String imagen = (String) myAdapter.getItem(position);
        myAdapter.setSelectedItem(position);
        myAdapter.notifyDataSetChanged();
        setResult(RESULT_OK, getIntent().putExtra("IMAGE",imagen));
        //finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
    }
}
