package com.nodomain.ivonne.snippet.adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.nodomain.ivonne.snippet.R;

/**
 * Created by Ivonne on 08/11/2017.
 */

public class horizontalNumberPicker extends LinearLayout {
    private LinearLayout thisLayout;
    private Button aumentar;
    private Button disminuir;
    private EditText tiempoApagado;

    public horizontalNumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater layoutInflater = (LayoutInflater)context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.horizontalpicker, this);

        OnClickListener onClickListener = new OnClickListener() {
            public void onClick(View v) {
                int valor = Integer.parseInt(tiempoApagado.getText().toString());
                if(v.getId() == aumentar.getId()){
                    if (valor == 60)
                        valor = 0;
                    else
                        valor ++;
                }
                if(v.getId() == disminuir.getId()){
                    if (valor == 0)
                        valor = 60;
                    else
                        valor --;
                }
                tiempoApagado.setText(Integer.toString(valor));
            }
        };

        TextWatcher onTextChanged = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override public void afterTextChanged(Editable editable) {}
            @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().equals("")){
                    int valor = Integer.parseInt(charSequence.toString());
                    if (valor > 60)
                        tiempoApagado.setText(Integer.toString(60));
                }
            }
        };

        thisLayout = (LinearLayout) findViewById(R.id.pickerLayout);
        tiempoApagado = (EditText) findViewById(R.id.tiempoApagado);
        tiempoApagado.addTextChangedListener(onTextChanged);
        aumentar = (Button) findViewById(R.id.btn_plus);
        aumentar.setOnClickListener(onClickListener);
        disminuir = (Button) findViewById(R.id.btn_minus);
        disminuir.setOnClickListener(onClickListener);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled)
            thisLayout.setBackgroundResource(R.drawable.borde_resaltado);
        else
            thisLayout.setBackgroundResource(R.drawable.borde_sombreado);
        tiempoApagado.setFocusableInTouchMode(enabled);
        tiempoApagado.setFocusable(enabled);
        aumentar.setEnabled(enabled);
        disminuir.setEnabled(enabled);
    }

    public int getIntValue() {
        return Integer.parseInt(tiempoApagado.getText().toString());
    }

    public String getStringValue(){
        return tiempoApagado.getText().toString();
    }

    public void setValue(int value) {
        tiempoApagado.setText(Integer.toString(value));
    }

}
