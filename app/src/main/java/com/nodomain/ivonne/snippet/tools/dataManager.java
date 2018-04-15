package com.nodomain.ivonne.snippet.tools;

import android.content.Context;

import com.nodomain.ivonne.snippet.objects.Device;
import com.nodomain.ivonne.snippet.objects.Network;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Ivonne on 21/09/2017.
 */

public class dataManager {

    private dataBase mydataBase;

    public dataManager (Context context){mydataBase = new dataBase(context);}

    public void addOrUpdateNetwork(Network network){mydataBase.agregarActualizarRed(network);}

    public String getPsw(String networkSSID){return mydataBase.getContrasena(networkSSID);}

    public boolean addorUpdateDevice(Device myDevice){return mydataBase.agregarActualizarDispositivo(myDevice);}

    public String getStoredFoo(String deviceMAC){return mydataBase.getFooAlmacenada(deviceMAC);}

    public List getTypes(String networkSSID){
        List types = mydataBase.getTipos(networkSSID);
        Set hashset = new HashSet();
        hashset.addAll(types);
        types.clear();
        types.addAll(hashset);
        return types;
    }

    public List getHouseSpaces(String SSID){
        List spaces = mydataBase.getAmbientes(SSID);
        Set hashset = new HashSet();
        hashset.addAll(spaces);
        spaces.clear();
        spaces.addAll(hashset);
        return spaces;
    }

    public List<Device> getDevicesBySpaces(String RED, String AMBIENTE){return mydataBase.getDispositivosPorAmbiente(RED, AMBIENTE);}

    public List<Device> getDevicesByType(String RED, String TIPO){return mydataBase.getDispositivosPorTipo(RED, TIPO);}

    public void updateIP(String mac, String myFoo){
        Device myDevice = mydataBase.getDispositivoPorMac(mac);
        myDevice.setDevFoo(myFoo);
        mydataBase.agregarActualizarDispositivo(myDevice);
    }

    public Device getDeviceOnPosition(int position){
        return mydataBase.getDispositivo(position);
    }

    public Device getDeviceByMac(String myMAC){
        return mydataBase.getDispositivoPorMac(myMAC);
    }

    public boolean deviceExists(String myMAC){
        return mydataBase.existeDispositivo(myMAC);
    }

    public void delete(String mac){mydataBase.borrarDispositivo(mac);}

    public boolean autoOn(){return mydataBase.autoEncendido();}
}
