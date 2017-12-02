package com.nodomain.ivonne.snippet.adaptadores;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.nodomain.ivonne.snippet.R;

/**
 * Created by Ivonne on 22/09/2017.
 */

public class adaptadorImagenes extends BaseAdapter {
    private Context mContext;
    private String tipo;
    private String imagen_seleccionada;

    public adaptadorImagenes(Context c, String tipo, String imagen_seleccionada) {
        mContext = c;
        this.tipo = tipo;
        this.imagen_seleccionada = imagen_seleccionada;
    }

    public int getCount() {
        switch (tipo){
            default:{
                return focos.length;
            }
        }
    }

    public Object getItem(int position) {
        switch (tipo){
            case "DIMMER":
                return dimmer[position];
            default:
                return focos[position];
        }
    }

    public long getItemId(int position) {
        switch (tipo) {
            case "DIMMER":
                return mContext.getResources().getIdentifier(dimmer[position], "drawable", mContext.getPackageName());
            default:
                return mContext.getResources().getIdentifier(focos[position], "drawable", mContext.getPackageName());
        }
    }

    public void setSelectedItem(int position){
        switch (tipo){
            case "DIMMER":{
                imagen_seleccionada = dimmer[position];
                break;
            }
            default:{
                imagen_seleccionada = focos[position];
                break;
            }
        }
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams((int) mContext.getResources().getDimension(R.dimen.vista_cuadro), (int) mContext.getResources().getDimension(R.dimen.vista_cuadro)));
        } else {
            imageView = (ImageView) convertView;
        }

        switch (tipo){
            case "DIMMER":{
                imageView.setImageResource(mContext.getResources().getIdentifier(dimmer[position], "drawable", mContext.getPackageName()));
                if (dimmer[position].equals(imagen_seleccionada)){
                    if (Build.VERSION.SDK_INT >= 22)
                        imageView.setBackground(mContext.getDrawable(R.drawable.borde_resaltado));
                    else
                        imageView.setBackground(mContext.getResources().getDrawable(R.drawable.borde_resaltado));
                }
                else{
                    if (Build.VERSION.SDK_INT >= 22)
                        imageView.setBackground(mContext.getDrawable(R.drawable.borde_sombreado));
                    else
                        imageView.setBackground(mContext.getResources().getDrawable(R.drawable.borde_sombreado));
                }
                int paddingvalue = (int) mContext.getResources().getDimension(R.dimen.margen_chico_variable);
                imageView.setPadding(paddingvalue, paddingvalue, paddingvalue, paddingvalue);
                break;
            }
            default:{
                imageView.setImageResource(mContext.getResources().getIdentifier(focos[position], "drawable", mContext.getPackageName()));
                if (focos[position].equals(imagen_seleccionada)){
                    if (Build.VERSION.SDK_INT >= 22)
                        imageView.setBackground(mContext.getDrawable(R.drawable.borde_resaltado));
                    else
                        imageView.setBackground(mContext.getResources().getDrawable(R.drawable.borde_resaltado));
                }
                else{
                    if (Build.VERSION.SDK_INT >= 22)
                        imageView.setBackground(mContext.getDrawable(R.drawable.borde_sombreado));
                    else
                        imageView.setBackground(mContext.getResources().getDrawable(R.drawable.borde_sombreado));
                }
                int paddingvalue = (int) mContext.getResources().getDimension(R.dimen.margen_chico_variable);
                imageView.setPadding(paddingvalue, paddingvalue, paddingvalue, paddingvalue);
                break;
            }
        }
        return imageView;
    }

    // references to our images
    private String[] focos = {
            "foco0", "foco1", "foco2",
            "foco3", "foco4"
    };

    private String[] dimmer = {
            "dimmer0", "dimmer1", "dimmer2",
            "dimmer3", "dimmer4"
    };

}
