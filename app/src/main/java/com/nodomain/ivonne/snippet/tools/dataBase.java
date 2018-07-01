package com.nodomain.ivonne.snippet.tools;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.nodomain.ivonne.snippet.objects.Device;
import com.nodomain.ivonne.snippet.objects.Network;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ivonne on 21/09/2017.
 */

class dataBase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "SnippetDataBase";//nombre de la base de datos
    private static final int DATABASE_VERSION = 1;//version de la base de datos

    // Nombres de las tablas
    private static final String TABLE_NETWORKS = "NETWORKS";
    private static final String TABLE_DEVICES = "DEVICES";

    // Columnas de la tabla redes
    private static final String KEY_NETWORK_ID = "netID";
    private static final String KEY_NETWORK_SSID = "netSSID";
    private static final String KEY_NETWORK_PSW = "netPSW";
    private static final String KEY_NETWORK_NAME = "netName";

    // columnas de la tabla dispositivos
    private static final String KEY_DEVICE_ID = "devID";
    private static final String KEY_DEVICE_RED = "devNetwork";
    private static final String KEY_DEVICE_NAME = "devName";
    private static final String KEY_DEVICE_TYPE = "devType";
    private static final String KEY_DEVICE_FOO = "devFoo";
    private static final String KEY_DEVICE_MAC = "devMac";
    private static final String KEY_DEVICE_PSW = "devPsw";
    private static final String KEY_DEVICE_SPACE = "devSpace";
    private static final String KEY_DEVICE_GRUPO = "devGroup";
    private static final String KEY_DEVICE_XX = "devXx";
    private static final String KEY_DEVICE_YY = "devYy";
    private static final String KEY_DEVICE_IMAGE = "devImage";
    private static final String KEY_DEVICE_ON = "devOn";
    private static final String KEY_DEVICE_OFF = "devOff";
    private static final String KEY_DEVICE_TIME = "devTime";
    private static final String KEY_DEVICE_ARG1 = "devArg1";
    private static final String KEY_DEVICE_ARG2 = "devArg2";
    private static final String KEY_DEVICE_ARG3 = "devArg3";

    dataBase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    //Frase para crear la tabla dispos
    String CREATE_DEVICE_TABLE = "CREATE TABLE " + TABLE_DEVICES +
            "(" +
            KEY_DEVICE_ID + " INTEGER PRIMARY KEY," +
            KEY_DEVICE_RED + " TEXT," +
            KEY_DEVICE_NAME + " TEXT," +
            KEY_DEVICE_TYPE + " TEXT," +
            KEY_DEVICE_FOO + " TEXT," +
            KEY_DEVICE_MAC + " TEXT," +
            KEY_DEVICE_PSW + " TEXT," +
            KEY_DEVICE_SPACE + " TEXT," +
            KEY_DEVICE_GRUPO + " TEXT," +
            KEY_DEVICE_XX + " TEXT," +
            KEY_DEVICE_YY + " TEXT," +
            KEY_DEVICE_IMAGE + " TEXT," +
            KEY_DEVICE_ON + " TEXT," +
            KEY_DEVICE_OFF + " TEXT," +
            KEY_DEVICE_TIME + " TEXT," +
            KEY_DEVICE_ARG1 + " TEXT," +
            KEY_DEVICE_ARG2 + " TEXT," +
            KEY_DEVICE_ARG3 + " TEXT)";

    String CREAR_TABLA_REDES = "CREATE TABLE " + TABLE_NETWORKS +
            "(" +
            KEY_NETWORK_ID + " INTEGER PRIMARY KEY," + // Define a primary key
            KEY_NETWORK_SSID + " TEXT," +
            KEY_NETWORK_PSW + " TEXT," +
            KEY_NETWORK_NAME + " TEXT)";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREAR_TABLA_REDES);
        db.execSQL(CREATE_DEVICE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    void nuevaTablaDispositivos() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEVICES);
        db.execSQL(CREATE_DEVICE_TABLE);
    }

    void agregarActualizarRed(Network network) {
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_NETWORK_SSID, network.getNetworkSSID());
            values.put(KEY_NETWORK_PSW, network.getNetworkPSW());
            values.put(KEY_NETWORK_NAME, network.getNetworkName());

            // Trata de actualizar la red si es que ya existe, la busca por su SSID
            int rows = db.update(TABLE_NETWORKS, values, KEY_NETWORK_SSID + "= ?", new String[]{network.getNetworkSSID()});
            if (rows == 1) {
                String usersSelectQuery = String.format("SELECT %s FROM %s WHERE %s = ?",
                        KEY_NETWORK_ID, TABLE_DEVICES, KEY_NETWORK_SSID);
                Cursor cursor = db.rawQuery(usersSelectQuery, new String[]{String.valueOf(network.getNetworkSSID())});
                try {
                    if (cursor.moveToFirst()) {
                        db.setTransactionSuccessful();
                    }
                } finally {
                    if (cursor != null && !cursor.isClosed()) {
                        cursor.close();
                    }
                }
            } else {
                db.insertOrThrow(TABLE_NETWORKS, null, values);
                db.setTransactionSuccessful();
            }
        } catch (Exception e) {
            //Log.d(TAG, "Error while trying to add or update network");
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    boolean agregarActualizarDispositivo(Device myDevice) {
        boolean preexistente = false;
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_DEVICE_RED, myDevice.getDevNetwork());
            values.put(KEY_DEVICE_NAME, myDevice.getDevName());
            values.put(KEY_DEVICE_TYPE, myDevice.getDevType());
            values.put(KEY_DEVICE_FOO, myDevice.getDevFoo());
            values.put(KEY_DEVICE_MAC, myDevice.getDevMac());
            values.put(KEY_DEVICE_PSW, myDevice.getDevPsw());
            values.put(KEY_DEVICE_SPACE, myDevice.getDevHouseSpace());
            values.put(KEY_DEVICE_GRUPO, myDevice.getDevGroup());
            values.put(KEY_DEVICE_XX, myDevice.getDevXx());
            values.put(KEY_DEVICE_YY, myDevice.getDevYy());
            values.put(KEY_DEVICE_IMAGE, myDevice.getDevImage());
            values.put(KEY_DEVICE_ON, myDevice.getDevOn());
            values.put(KEY_DEVICE_OFF, myDevice.getDevOff());
            values.put(KEY_DEVICE_TIME, myDevice.getDevTime());
            values.put(KEY_DEVICE_ARG1, myDevice.getDevArg1());
            values.put(KEY_DEVICE_ARG2, myDevice.getDevArg2());
            values.put(KEY_DEVICE_ARG3, myDevice.getDevArg3());

            // Trata de actualizar el myDevice si es que ya existe, lo busca por su mac
            int rows = db.update(TABLE_DEVICES, values, KEY_DEVICE_MAC + "= ?", new String[]{myDevice.getDevMac()});
            if (rows == 1) {//update
                preexistente = true;
                String usersSelectQuery = String.format("SELECT %s FROM %s WHERE %s = ?",
                        KEY_DEVICE_SPACE, TABLE_DEVICES, KEY_DEVICE_MAC);
                Cursor cursor = db.rawQuery(usersSelectQuery, new String[]{String.valueOf(myDevice.getDevMac())});
                try {
                    if (cursor.moveToFirst()) {
                        db.setTransactionSuccessful();
                    }
                } finally {
                    if (cursor != null && !cursor.isClosed()) {
                        cursor.close();
                    }
                }
            } else {
                //nuevo
                db.insertOrThrow(TABLE_DEVICES, null, values);
                db.setTransactionSuccessful();
            }
        } catch (Exception e) {
            //Log.d(TAG, "Error while trying to add or update device");
        } finally {
            db.endTransaction();
            db.close();
        }
        return preexistente;
    }

    List getTipos(String SSID){
        List tipos = new ArrayList();
        SQLiteDatabase db = getReadableDatabase();
        String SELECT_QUERY =
                String.format("SELECT %s FROM %s WHERE %s LIKE ?", KEY_DEVICE_TYPE, TABLE_DEVICES, KEY_DEVICE_RED);
        Cursor cursor = db.rawQuery(SELECT_QUERY, new String[]{'%'+SSID+'%'});
        try {
            if (cursor.moveToFirst()) {//cursor al inicio
                do {
                    tipos.add(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_TYPE)));
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            //Log.d(TAG, "Error while trying to get ambientes from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            db.close();
        }
        return tipos;
    }

    List getAmbientes(String SSID){
        List ambientes = new ArrayList();
        SQLiteDatabase db = getReadableDatabase();
        String SELECT_QUERY =
                String.format("SELECT %s FROM %s WHERE %s LIKE ?", KEY_DEVICE_SPACE, TABLE_DEVICES, KEY_DEVICE_RED);
        Cursor cursor = db.rawQuery(SELECT_QUERY, new String[]{'%'+SSID+'%'});
        try {
            if (cursor.moveToFirst()) {//cursor al inicio
                do {
                    ambientes.add(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_SPACE)));
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            //Log.d(TAG, "Error while trying to get ambientes from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            db.close();
        }
        return ambientes;
    }

    String getContrasena(String SSID) {// Buscar password de la red
        String contrasena = "";
        String SELECT_QUERY = String.format("SELECT %s FROM %s WHERE %s = ?",
                KEY_NETWORK_PSW, TABLE_NETWORKS, KEY_NETWORK_SSID);//frase "selecciona el password de la tabla de redes donde el SSID es igual al que estoy solicitando"
        SQLiteDatabase db = getReadableDatabase();//base de datos como lectura
        Cursor cursor = db.rawQuery(SELECT_QUERY, new String[]{SSID+""});//buscar en la base de datos la frase
        try {
            if(cursor.getCount() > 0) {
                cursor.moveToFirst();
                contrasena = cursor.getString(cursor.getColumnIndex(KEY_NETWORK_PSW));//busca el password y ponlo en la variable
            }
            return contrasena;//regresa la variable
        }finally {
            cursor.close();//cierra el cursor
            db.close();
        }
    }

    String getFooAlmacenada(String mMac) {
        String mi_ip = "";
        String SELECT_QUERY = String.format("SELECT %s FROM %s WHERE %s = ?",
                KEY_DEVICE_FOO, TABLE_DEVICES, KEY_DEVICE_MAC);//frase "selecciona el password de la tabla de redes donde el SSID es igual al que estoy solicitando"
        SQLiteDatabase db = getReadableDatabase();//base de datos como lectura
        Cursor cursor = db.rawQuery(SELECT_QUERY, new String[]{mMac+""});//buscar en la base de datos la frase
        try {
            if(cursor.getCount() > 0) {//el cursor esta apuntando a alguna fila
                cursor.moveToFirst();//lee la entrada desde el principio
                mi_ip = cursor.getString(cursor.getColumnIndex(KEY_DEVICE_FOO));//busca el password y ponlo en la variable
            }
            return mi_ip;//regresa la variable
        }finally {
            cursor.close();//cierra el cursor
            db.close();
        }
    }

    List<Device> getDispositivosPorAmbiente(String red, String ambiente) {
        List<Device> myDevice = new ArrayList<>();
        String SELECT_QUERY = String.format("SELECT * FROM %s WHERE %s = ?", TABLE_DEVICES, KEY_DEVICE_SPACE);

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(SELECT_QUERY, new String[]{ambiente+""});
        try {
            if (cursor.moveToFirst()) {
                do {
                    if (cursor.getString(cursor.getColumnIndex(KEY_DEVICE_RED)).contains(red)) {
                        Device newDevice = new Device();
                        newDevice.setDevName(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_NAME)));
                        newDevice.setDevType(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_TYPE)));
                        newDevice.setDevFoo(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_FOO)));
                        newDevice.setDevMac(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_MAC)));
                        newDevice.setDevPsw(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_PSW)));
                        newDevice.setDevXx(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_XX)));
                        newDevice.setDevYy(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_XX)));
                        newDevice.setDevImage(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_IMAGE)));
                        myDevice.add(newDevice);
                    }
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            //Log.d(TAG, "Error while trying to get devices from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            db.close();
        }
        return myDevice;
    }

    List<Device> getDispositivosPorTipo(String red, String tipo) {
        List<Device> myDevice = new ArrayList<>();
        String SELECT_QUERY = String.format("SELECT * FROM %s WHERE %s = ?", TABLE_DEVICES, KEY_DEVICE_TYPE);

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(SELECT_QUERY, new String[]{tipo+""});
        try {
            if (cursor.moveToFirst()) {
                do {
                    if (cursor.getString(cursor.getColumnIndex(KEY_DEVICE_RED)).contains(red)) {
                        Device newDevice = new Device();
                        newDevice.setDevName(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_NAME)));
                        newDevice.setDevType(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_TYPE)));
                        newDevice.setDevFoo(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_FOO)));
                        newDevice.setDevMac(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_MAC)));
                        newDevice.setDevPsw(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_PSW)));
                        newDevice.setDevXx(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_XX)));
                        newDevice.setDevYy(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_XX)));
                        newDevice.setDevImage(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_IMAGE)));
                        myDevice.add(newDevice);
                    }
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            //Log.d(TAG, "Error while trying to get devices from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            db.close();
        }
        return myDevice;
    }

    boolean existeDispositivo(String mi_mac) {// Buscar password de la red
        boolean resultado = false;
        String SELECT_QUERY = String.format("SELECT * FROM %s WHERE %s = ?",
                TABLE_DEVICES, KEY_DEVICE_MAC);//frase "selecciona el password de la tabla de redes donde el SSID es igual al que estoy solicitando"
        SQLiteDatabase db = getReadableDatabase();//base de datos como lectura
        Cursor cursor = db.rawQuery(SELECT_QUERY, new String[]{mi_mac+""});//buscar en la base de datos la frase
        try {
            if(cursor.getCount() > 0) {
                resultado = true;
            }
            return resultado;
        }finally {
            cursor.close();//cierra el cursor
            db.close();
        }
    }

    Device getDispositivoPorMac(String mi_mac) {// Buscar password de la red
        Device myDevice = new Device();
        String SELECT_QUERY = String.format("SELECT * FROM %s WHERE %s = ?",
                TABLE_DEVICES, KEY_DEVICE_MAC);//frase "selecciona el password de la tabla de redes donde el SSID es igual al que estoy solicitando"
        SQLiteDatabase db = getReadableDatabase();//base de datos como lectura
        Cursor cursor = db.rawQuery(SELECT_QUERY, new String[]{mi_mac+""});//buscar en la base de datos la frase
        try {
            if(cursor.getCount() > 0) {
                cursor.moveToFirst();
                myDevice.setDevNetwork(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_RED)));
                myDevice.setDevName(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_NAME)));
                myDevice.setDevType(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_TYPE)));
                myDevice.setDevFoo(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_FOO)));
                myDevice.setDevMac(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_MAC)));
                myDevice.setDevPsw(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_PSW)));
                myDevice.setDevHouseSpace(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_SPACE)));
                myDevice.setDevXx(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_XX)));
                myDevice.setDevYy(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_YY)));
                myDevice.setDevImage(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_IMAGE)));
                myDevice.setDevOn(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_ON)));
                myDevice.setDevOff(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_OFF)));
                myDevice.setDevTime(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_TIME)));
            }
            return myDevice;//regresa la variable
        }finally {
            cursor.close();//cierra el cursor
            db.close();
        }
    }

    Device getDispositivo(int posicion){
        Device myDevice = new Device();
        String SELECT_QUERY = String.format("SELECT * FROM %s ", TABLE_DEVICES);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(SELECT_QUERY, null);
        try {
            if (cursor.moveToPosition(posicion)) {
                myDevice.setDevName(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_NAME)));
                myDevice.setDevType(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_TYPE)));
                myDevice.setDevFoo(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_FOO)));
                myDevice.setDevMac(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_MAC)));
                myDevice.setDevXx(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_MAC)));
                myDevice.setDevYy(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_MAC)));
                myDevice.setDevImage(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_MAC)));
            }
        } catch (Exception e) {
            //Log.d(TAG, "Error while trying to get names from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            db.close();
        }
        return myDevice;
    }

    boolean autoEncendido(){
        boolean resultado = false;
        String SELECT_QUERY = String.format("SELECT * FROM %s WHERE %s = ?",
                TABLE_DEVICES, KEY_DEVICE_ON);//frase "selecciona el password de la tabla de redes donde el SSID es igual al que estoy solicitando"
        SQLiteDatabase db = getReadableDatabase();//base de datos como lectura
        Cursor cursor = db.rawQuery(SELECT_QUERY, new String[]{"true"});
        try {
            if(cursor.getCount() > 0) {
                resultado = true;
            }
        } catch (Exception e) {
            //Log.d(TAG, "Error while trying to get names from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            db.close();
        }
        return resultado;
    }

    void borrarDispositivo(String mac) {
        String SELECT_QUERY = String.format("DELETE FROM %s WHERE %s = ?",
                TABLE_DEVICES, KEY_DEVICE_MAC);
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(SELECT_QUERY, new String[]{mac+""});
        db.close();
    }

}
