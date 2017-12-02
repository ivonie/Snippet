package com.nodomain.ivonne.snippet.adaptadores;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nodomain.ivonne.snippet.R;

import java.util.ArrayList;

/**
 * Created by Ivonne on 23/09/2017.
 */

public class adaptadorPrincipal extends BaseAdapter {
/*
    public adaptadorPrincipal(Context context, int layout, Cursor c, String[] from,
                              int[] to, int flags) {
        super(context, layout , c, from, to, flags);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);
        TextView titulo = (TextView) view.findViewById(R.id.titulo);
        int resid = context.getResources().getIdentifier(titulo.toString(), "drawable", context.getPackageName());
        titulo.setBackgroundResource(resid);
    }
*/

    private Activity activity;
    private LayoutInflater inflater = null;
    ArrayList<String> list;

    public adaptadorPrincipal(Activity a, ArrayList<String> list) {
        activity = a;
        inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.list = list;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (convertView == null)
            v = inflater.inflate(R.layout.primer_vista, null);
        String imagen = "default_"+(list.get(position).replaceAll(" ","_").toLowerCase());
        int resid = activity.getResources().getIdentifier(imagen, "drawable", activity.getPackageName());
        v.setBackgroundResource(resid);
        TextView titulo = (TextView) v.findViewById(R.id.titulo);
        titulo.setText(list.get(position));
        return v;
    }
}
