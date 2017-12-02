package com.nodomain.ivonne.snippet.configuracionESP;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.nodomain.ivonne.snippet.objetos.Dispositivo;
import com.nodomain.ivonne.snippet.R;
import com.nodomain.ivonne.snippet.herramientas.dataManager;
import com.nodomain.ivonne.snippet.servicios.segundoPlano;

import static com.nodomain.ivonne.snippet.configuracionESP.snippetNuevo.ACCION;
import static com.nodomain.ivonne.snippet.configuracionESP.snippetNuevo.CONFIGURAR_SNIPPET_ACCION;
import static com.nodomain.ivonne.snippet.configuracionESP.snippetNuevo.CONFIGURAR_SNIPPET_CODE;
import static com.nodomain.ivonne.snippet.configuracionESP.snippetNuevo.PARAMETRO1;
import static com.nodomain.ivonne.snippet.configuracionESP.snippetNuevo.PARAMETRO2;
import static com.nodomain.ivonne.snippet.configuracionESP.snippetNuevo.PARAMETRO3;
import static com.nodomain.ivonne.snippet.configuracionESP.snippetNuevo.PARAMETRO4;

public class snippetGuardar extends AppCompatActivity implements Button.OnClickListener, Spinner.OnItemSelectedListener {

    static int SELECCIONAR_IMAGEN = 81;

    private LinearLayout miLayout;
    private TextView texto1;
    private Button agregar;
    private ImageView snippetImagen;

    private String snippetFoo;
    private String snippetMac;
    private String snippetTipo;
    private String snippetNombre;
    private String snippetContrasena;
    private String redNombre;
    private String redContrasena;
    private String imagen;

    private Boolean reintento = true;
    private int posicion = 0;
    private Boolean salirsinguardar = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snippet_guardar);

        miLayout = (LinearLayout) findViewById(R.id.guardar_layout);
        texto1 = (TextView) findViewById(R.id.texto1);
        snippetImagen = (ImageView) findViewById(R.id.imagen);
        Spinner ambientes = (Spinner) findViewById(R.id.textoAmbiente);
        agregar = (Button) findViewById(R.id.botonGuardar);
        agregar.setOnClickListener(this);

        snippetNombre = getIntent().getStringExtra(PARAMETRO1);
        snippetContrasena = getIntent().getStringExtra(PARAMETRO2);
        redNombre = getIntent().getStringExtra(PARAMETRO3);
        redContrasena = getIntent().getStringExtra(PARAMETRO4);

        Intent intent = new Intent(snippetGuardar.this, segundoPlano.class);
        intent.putExtra(ACCION, CONFIGURAR_SNIPPET_ACCION);
        intent.putExtra(PARAMETRO1, snippetNombre);
        intent.putExtra(PARAMETRO2, snippetContrasena);
        intent.putExtra(PARAMETRO3, redNombre);
        intent.putExtra(PARAMETRO4, redContrasena);
        startActivityForResult(intent, CONFIGURAR_SNIPPET_CODE);

        snippetImagen.setOnClickListener(this);
        ambientes.setOnItemSelectedListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view.getId()==agregar.getId())
            agregarSnippet();
        else if(view.getId()==snippetImagen.getId()){
            Intent getImagen = new Intent(snippetGuardar.this,seleccionarImagen.class);
            getImagen.putExtra("TIPO",snippetTipo);
            getImagen.putExtra("ACTUAL",imagen);
            startActivityForResult(getImagen,SELECCIONAR_IMAGEN);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        posicion = position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

    private void leerConfiguracion(String datos){
        String[] valores = datos.split(",");
        snippetFoo = valores[0];
        if (valores.length > 1)
            snippetMac = valores[1];
        if (valores.length > 2) {
            snippetTipo = valores[2];
            snippetImagen.setImageResource(getApplicationContext().getResources().getIdentifier(snippetTipo.toLowerCase()+"0", "drawable", getApplicationContext().getPackageName()));
            imagen = snippetTipo.toLowerCase()+"0";
        }
        texto1.setText(getString(R.string.mostrarGUARDAR1)+" "+snippetTipo.toLowerCase()+" \""+snippetNombre+"\" "+getString(R.string.mostrarGUARDAR2));
    }

    private void agregarSnippet(){
        Dispositivo dispositivo = new Dispositivo();
        dispositivo.setRed(redNombre);
        dispositivo.setNombre(snippetNombre);
        dispositivo.setTipo(snippetTipo);
        dispositivo.setFoo(snippetFoo);
        dispositivo.setMac(snippetMac);
        dispositivo.setContrasena(snippetContrasena);
        String[] listaAmbientes = getResources().getStringArray(R.array.ambientes);
        dispositivo.setAmbiente(listaAmbientes[posicion]);
        dispositivo.setImagen(imagen);
        dataManager myDM = new dataManager(snippetGuardar.this);
        myDM.agregarActualizarDispositivo(dispositivo);
        setResult(RESULT_OK, getIntent().putExtra("MAC",dispositivo.getMac()));
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case 35: {//CONFIGURAR_SNIPPET_CODE
                if (resultCode == RESULT_OK) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.mensaje5), Toast.LENGTH_SHORT).show();
                    leerConfiguracion(data.getStringExtra(PARAMETRO1));
                }
                else {
                    if (reintento) {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.error4), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(snippetGuardar.this, segundoPlano.class);
                        intent.putExtra(ACCION, CONFIGURAR_SNIPPET_ACCION);
                        intent.putExtra(PARAMETRO1, snippetNombre);
                        intent.putExtra(PARAMETRO2, snippetContrasena);
                        intent.putExtra(PARAMETRO3, redNombre);
                        intent.putExtra(PARAMETRO4, redContrasena);
                        startActivityForResult(intent, CONFIGURAR_SNIPPET_CODE);
                        reintento = false;
                    }
                    else {
                        setResult(RESULT_CANCELED,getIntent().putExtra("RESULTADO","ERROR"));
                        finish();
                    }
                }
                break;
            }
            case 81:{//SELECCIONAR_IMAGEN
                if (resultCode == RESULT_OK) {
                    imagen = data.getStringExtra("IMAGEN");
                    snippetImagen.setImageResource(getApplicationContext().getResources().getIdentifier(imagen, "drawable", getApplicationContext().getPackageName()));
                }
                break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (salirsinguardar) {
            //Snackbar.make(miLayout, getResources().getString(R.string.guardarforzoso), Snackbar.LENGTH_SHORT).show();
            Toast.makeText(getApplicationContext(), getString(R.string.guardarforzoso), Toast.LENGTH_LONG).show();
            salirsinguardar = false;
        }
        else{
            setResult(RESULT_CANCELED,getIntent().putExtra("RESULTADO","CANCELADO"));
            super.onBackPressed();
        }
    }
}
