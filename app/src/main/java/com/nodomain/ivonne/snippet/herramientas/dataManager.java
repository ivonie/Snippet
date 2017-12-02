package com.nodomain.ivonne.snippet.herramientas;

import android.content.Context;

import com.nodomain.ivonne.snippet.objetos.Dispositivo;
import com.nodomain.ivonne.snippet.objetos.Network;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Ivonne on 21/09/2017.
 */

public class dataManager {

    private dataBase mydataBase;

    public dataManager (Context context){mydataBase = new dataBase(context);}

    public void agregarActualizarRed(Network network){mydataBase.agregarActualizarRed(network);}

    public String getContrasena (String SSID){return mydataBase.getContrasena(SSID);}

    public boolean agregarActualizarDispositivo(Dispositivo dispositivo){return mydataBase.agregarActualizarDispositivo(dispositivo);}

    public String getFooAlmacenada(String mMAC){return mydataBase.getFooAlmacenada(mMAC);}

    public List getTipos(String SSID){
        List tipos = mydataBase.getTipos(SSID);
        Set hashset = new HashSet();
        hashset.addAll(tipos);
        tipos.clear();
        tipos.addAll(hashset);
        return tipos;
    }

    public List getAmbientes(String SSID){
        List ambientes = mydataBase.getAmbientes(SSID);
        Set hashset = new HashSet();
        hashset.addAll(ambientes);
        ambientes.clear();
        ambientes.addAll(hashset);
        return ambientes;
    }

    public List<Dispositivo> getDispositivosPorAmbiente(String RED, String AMBIENTE){return mydataBase.getDispositivosPorAmbiente(RED, AMBIENTE);}

    public List<Dispositivo> getDispositivosPorTipo(String RED, String TIPO){return mydataBase.getDispositivosPorTipo(RED, TIPO);}

    public void actualizarIP(String mac, String MyFoo){
        Dispositivo dispositivo = mydataBase.getDispositivoPorMac(mac);
        dispositivo.setFoo(MyFoo);
        mydataBase.agregarActualizarDispositivo(dispositivo);
    }

    public Dispositivo getDispositivoEnPosicion(int position){
        return mydataBase.getDispositivo(position);
    }

    public Dispositivo getDispositivoPorMac(String myMAC){
        return mydataBase.getDispositivoPorMac(myMAC);
    }

    public boolean existeDispositivo (String myMAC){
        return mydataBase.existeDispositivo(myMAC);
    }

    public void borrar(String mac){mydataBase.borrarDispositivo(mac);}

    public boolean encendidoAutomatico (){return mydataBase.autoEncendido();}
}
