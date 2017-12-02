package com.nodomain.ivonne.snippet.herramientas;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.nodomain.ivonne.snippet.objetos.Dispositivo;
import com.nodomain.ivonne.snippet.objetos.Network;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ivonne on 21/09/2017.
 */

class dataBase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "BaseDeDatos";//nombre de la base de datos
    private static final int DATABASE_VERSION = 1;//version de la base de datos

    // Nombres de las tablas
    private static final String TABLA_REDES = "REDES";
    private static final String TABLA_DISPOSITIVOS = "DISPOSITIVOS";

    // Columnas de la tabla redes
    private static final String KEY_RED_ID = "r_ID";
    private static final String KEY_RED_SSID = "r_SSID";
    private static final String KEY_RED_PSW = "r_PSW";
    private static final String KEY_RED_NOMBRE = "r_Nombre";

    // columnas de la tabla dispositivos
    private static final String KEY_DISPOSITIVO_ID = "d_ID";
    private static final String KEY_DISPOSITIVO_RED = "d_Red";
    private static final String KEY_DISPOSITIVO_NOMBRE = "d_Nombre";
    private static final String KEY_DISPOSITIVO_TIPO = "d_Tipo";
    private static final String KEY_DISPOSITIVO_FOO = "d_Foo";
    private static final String KEY_DISPOSITIVO_MAC = "d_Mac";
    private static final String KEY_DISPOSITIVO_CONTRASENA = "d_Constrasena";
    private static final String KEY_DISPOSITIVO_AMBIENTE = "d_Ambiente";
    private static final String KEY_DISPOSITIVO_GRUPO = "d_Grupo";
    private static final String KEY_DISPOSITIVO_XX = "d_Xx";
    private static final String KEY_DISPOSITIVO_YY = "d_Yy";
    private static final String KEY_DISPOSITIVO_IMAGEN = "d_Imagen";
    private static final String KEY_DISPOSITIVO_ENCENDIDO = "d_Encendido";
    private static final String KEY_DISPOSITIVO_APAGADO = "d_Apagado";
    private static final String KEY_DISPOSITIVO_TIEMPO = "d_Tiempo";
    private static final String KEY_DISPOSITIVO_ARG1 = "d_Arg1";
    private static final String KEY_DISPOSITIVO_ARG2 = "d_Arg2";
    private static final String KEY_DISPOSITIVO_ARG3 = "d_Arg3";

    dataBase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    //Frase para crear la tabla dispos
    String CREAR_TABLA_DISPOSITIVOS = "CREATE TABLE " + TABLA_DISPOSITIVOS +
            "(" +
            KEY_DISPOSITIVO_ID + " INTEGER PRIMARY KEY," +
            KEY_DISPOSITIVO_RED + " TEXT," +
            KEY_DISPOSITIVO_NOMBRE + " TEXT," +
            KEY_DISPOSITIVO_TIPO + " TEXT," +
            KEY_DISPOSITIVO_FOO + " TEXT," +
            KEY_DISPOSITIVO_MAC + " TEXT," +
            KEY_DISPOSITIVO_CONTRASENA + " TEXT," +
            KEY_DISPOSITIVO_AMBIENTE + " TEXT," +
            KEY_DISPOSITIVO_GRUPO + " TEXT," +
            KEY_DISPOSITIVO_XX + " TEXT," +
            KEY_DISPOSITIVO_YY + " TEXT," +
            KEY_DISPOSITIVO_IMAGEN + " TEXT," +
            KEY_DISPOSITIVO_ENCENDIDO + " TEXT," +
            KEY_DISPOSITIVO_APAGADO + " TEXT," +
            KEY_DISPOSITIVO_TIEMPO + " TEXT," +
            KEY_DISPOSITIVO_ARG1 + " TEXT," +
            KEY_DISPOSITIVO_ARG2 + " TEXT," +
            KEY_DISPOSITIVO_ARG3 + " TEXT)";

    String CREAR_TABLA_REDES = "CREATE TABLE " + TABLA_REDES +
            "(" +
            KEY_RED_ID + " INTEGER PRIMARY KEY," + // Define a primary key
            KEY_RED_SSID + " TEXT," +
            KEY_RED_PSW + " TEXT," +
            KEY_RED_NOMBRE + " TEXT)";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREAR_TABLA_REDES);
        db.execSQL(CREAR_TABLA_DISPOSITIVOS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    void nuevaTablaDispositivos() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLA_DISPOSITIVOS);
        db.execSQL(CREAR_TABLA_DISPOSITIVOS);
    }

    void agregarActualizarRed(Network network) {
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_RED_SSID, network.getNombre_ssid());
            values.put(KEY_RED_PSW, network.getContrasena());
            values.put(KEY_RED_NOMBRE, network.getNombre());

            // Trata de actualizar la red si es que ya existe, la busca por su SSID
            int rows = db.update(TABLA_REDES, values, KEY_RED_SSID + "= ?", new String[]{network.getNombre_ssid()});
            if (rows == 1) {
                String usersSelectQuery = String.format("SELECT %s FROM %s WHERE %s = ?",
                        KEY_RED_ID, TABLA_DISPOSITIVOS, KEY_RED_SSID);
                Cursor cursor = db.rawQuery(usersSelectQuery, new String[]{String.valueOf(network.getNombre_ssid())});
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
                db.insertOrThrow(TABLA_REDES, null, values);
                db.setTransactionSuccessful();
            }
        } catch (Exception e) {
            //Log.d(TAG, "Error while trying to add or update network");
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    boolean agregarActualizarDispositivo(Dispositivo dispositivo) {
        boolean preexistente = false;
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_DISPOSITIVO_RED, dispositivo.getRed());
            values.put(KEY_DISPOSITIVO_NOMBRE, dispositivo.getNombre());
            values.put(KEY_DISPOSITIVO_TIPO, dispositivo.getTipo());
            values.put(KEY_DISPOSITIVO_FOO, dispositivo.getFoo());
            values.put(KEY_DISPOSITIVO_MAC, dispositivo.getMac());
            values.put(KEY_DISPOSITIVO_CONTRASENA, dispositivo.getContrasena());
            values.put(KEY_DISPOSITIVO_AMBIENTE, dispositivo.getAmbiente());
            values.put(KEY_DISPOSITIVO_GRUPO, dispositivo.getGrupo());
            values.put(KEY_DISPOSITIVO_XX, dispositivo.getXx());
            values.put(KEY_DISPOSITIVO_YY, dispositivo.getYy());
            values.put(KEY_DISPOSITIVO_IMAGEN, dispositivo.getImagen());
            values.put(KEY_DISPOSITIVO_ENCENDIDO, dispositivo.getEncendido());
            values.put(KEY_DISPOSITIVO_APAGADO, dispositivo.getApagado());
            values.put(KEY_DISPOSITIVO_TIEMPO, dispositivo.getTiempo());
            values.put(KEY_DISPOSITIVO_ARG1, dispositivo.getArg1());
            values.put(KEY_DISPOSITIVO_ARG2, dispositivo.getArg2());
            values.put(KEY_DISPOSITIVO_ARG3, dispositivo.getArg3());

            // Trata de actualizar el dispositivo si es que ya existe, lo busca por su mac
            int rows = db.update(TABLA_DISPOSITIVOS, values, KEY_DISPOSITIVO_MAC + "= ?", new String[]{dispositivo.getMac()});
            if (rows == 1) {//update
                preexistente = true;
                String usersSelectQuery = String.format("SELECT %s FROM %s WHERE %s = ?",
                        KEY_DISPOSITIVO_AMBIENTE, TABLA_DISPOSITIVOS, KEY_DISPOSITIVO_MAC);
                Cursor cursor = db.rawQuery(usersSelectQuery, new String[]{String.valueOf(dispositivo.getMac())});
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
                db.insertOrThrow(TABLA_DISPOSITIVOS, null, values);
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
                String.format("SELECT %s FROM %s WHERE %s LIKE ?", KEY_DISPOSITIVO_TIPO, TABLA_DISPOSITIVOS, KEY_DISPOSITIVO_RED);
        Cursor cursor = db.rawQuery(SELECT_QUERY, new String[]{'%'+SSID+'%'});
        try {
            if (cursor.moveToFirst()) {//cursor al inicio
                do {
                    tipos.add(cursor.getString(cursor.getColumnIndex(KEY_DISPOSITIVO_TIPO)));
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
                String.format("SELECT %s FROM %s WHERE %s LIKE ?", KEY_DISPOSITIVO_AMBIENTE, TABLA_DISPOSITIVOS, KEY_DISPOSITIVO_RED);
        Cursor cursor = db.rawQuery(SELECT_QUERY, new String[]{'%'+SSID+'%'});
        try {
            if (cursor.moveToFirst()) {//cursor al inicio
                do {
                    ambientes.add(cursor.getString(cursor.getColumnIndex(KEY_DISPOSITIVO_AMBIENTE)));
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
                KEY_RED_PSW, TABLA_REDES, KEY_RED_SSID);//frase "selecciona el password de la tabla de redes donde el SSID es igual al que estoy solicitando"
        SQLiteDatabase db = getReadableDatabase();//base de datos como lectura
        Cursor cursor = db.rawQuery(SELECT_QUERY, new String[]{SSID+""});//buscar en la base de datos la frase
        try {
            if(cursor.getCount() > 0) {
                cursor.moveToFirst();
                contrasena = cursor.getString(cursor.getColumnIndex(KEY_RED_PSW));//busca el password y ponlo en la variable
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
                KEY_DISPOSITIVO_FOO, TABLA_DISPOSITIVOS, KEY_DISPOSITIVO_MAC);//frase "selecciona el password de la tabla de redes donde el SSID es igual al que estoy solicitando"
        SQLiteDatabase db = getReadableDatabase();//base de datos como lectura
        Cursor cursor = db.rawQuery(SELECT_QUERY, new String[]{mMac+""});//buscar en la base de datos la frase
        try {
            if(cursor.getCount() > 0) {//el cursor esta apuntando a alguna fila
                cursor.moveToFirst();//lee la entrada desde el principio
                mi_ip = cursor.getString(cursor.getColumnIndex(KEY_DISPOSITIVO_FOO));//busca el password y ponlo en la variable
            }
            return mi_ip;//regresa la variable
        }finally {
            cursor.close();//cierra el cursor
            db.close();
        }
    }

    List<Dispositivo> getDispositivosPorAmbiente(String red, String ambiente) {
        List<Dispositivo> dispositivo = new ArrayList<>();
        String SELECT_QUERY = String.format("SELECT * FROM %s WHERE %s = ?", TABLA_DISPOSITIVOS, KEY_DISPOSITIVO_AMBIENTE);

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(SELECT_QUERY, new String[]{ambiente+""});
        try {
            if (cursor.moveToFirst()) {
                do {
                    if (cursor.getString(cursor.getColumnIndex(KEY_DISPOSITIVO_RED)).contains(red)) {
                        Dispositivo newDispositivo = new Dispositivo();
                        newDispositivo.setNombre(cursor.getString(cursor.getColumnIndex(KEY_DISPOSITIVO_NOMBRE)));
                        newDispositivo.setTipo(cursor.getString(cursor.getColumnIndex(KEY_DISPOSITIVO_TIPO)));
                        newDispositivo.setFoo(cursor.getString(cursor.getColumnIndex(KEY_DISPOSITIVO_FOO)));
                        newDispositivo.setMac(cursor.getString(cursor.getColumnIndex(KEY_DISPOSITIVO_MAC)));
                        newDispositivo.setXx(cursor.getString(cursor.getColumnIndex(KEY_DISPOSITIVO_XX)));
                        newDispositivo.setYy(cursor.getString(cursor.getColumnIndex(KEY_DISPOSITIVO_XX)));
                        newDispositivo.setImagen(cursor.getString(cursor.getColumnIndex(KEY_DISPOSITIVO_IMAGEN)));
                        dispositivo.add(newDispositivo);
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
        return dispositivo;
    }

    List<Dispositivo> getDispositivosPorTipo(String red, String tipo) {
        List<Dispositivo> dispositivo = new ArrayList<>();
        String SELECT_QUERY = String.format("SELECT * FROM %s WHERE %s = ?", TABLA_DISPOSITIVOS, KEY_DISPOSITIVO_TIPO);

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(SELECT_QUERY, new String[]{tipo+""});
        try {
            if (cursor.moveToFirst()) {
                do {
                    if (cursor.getString(cursor.getColumnIndex(KEY_DISPOSITIVO_RED)).contains(red)) {
                        Dispositivo newDispositivo = new Dispositivo();
                        newDispositivo.setNombre(cursor.getString(cursor.getColumnIndex(KEY_DISPOSITIVO_NOMBRE)));
                        newDispositivo.setTipo(cursor.getString(cursor.getColumnIndex(KEY_DISPOSITIVO_TIPO)));
                        newDispositivo.setFoo(cursor.getString(cursor.getColumnIndex(KEY_DISPOSITIVO_FOO)));
                        newDispositivo.setMac(cursor.getString(cursor.getColumnIndex(KEY_DISPOSITIVO_MAC)));
                        newDispositivo.setXx(cursor.getString(cursor.getColumnIndex(KEY_DISPOSITIVO_XX)));
                        newDispositivo.setYy(cursor.getString(cursor.getColumnIndex(KEY_DISPOSITIVO_XX)));
                        newDispositivo.setImagen(cursor.getString(cursor.getColumnIndex(KEY_DISPOSITIVO_IMAGEN)));
                        dispositivo.add(newDispositivo);
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
        return dispositivo;
    }

    boolean existeDispositivo(String mi_mac) {// Buscar password de la red
        boolean resultado = false;
        String SELECT_QUERY = String.format("SELECT * FROM %s WHERE %s = ?",
                TABLA_DISPOSITIVOS, KEY_DISPOSITIVO_MAC);//frase "selecciona el password de la tabla de redes donde el SSID es igual al que estoy solicitando"
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

    Dispositivo getDispositivoPorMac(String mi_mac) {// Buscar password de la red
        Dispositivo dispositivo = new Dispositivo();
        String SELECT_QUERY = String.format("SELECT * FROM %s WHERE %s = ?",
                TABLA_DISPOSITIVOS, KEY_DISPOSITIVO_MAC);//frase "selecciona el password de la tabla de redes donde el SSID es igual al que estoy solicitando"
        SQLiteDatabase db = getReadableDatabase();//base de datos como lectura
        Cursor cursor = db.rawQuery(SELECT_QUERY, new String[]{mi_mac+""});//buscar en la base de datos la frase
        try {
            if(cursor.getCount() > 0) {
                cursor.moveToFirst();
                dispositivo.setRed(cursor.getString(cursor.getColumnIndex(KEY_DISPOSITIVO_RED)));
                dispositivo.setNombre(cursor.getString(cursor.getColumnIndex(KEY_DISPOSITIVO_NOMBRE)));
                dispositivo.setTipo(cursor.getString(cursor.getColumnIndex(KEY_DISPOSITIVO_TIPO)));
                dispositivo.setFoo(cursor.getString(cursor.getColumnIndex(KEY_DISPOSITIVO_FOO)));
                dispositivo.setMac(cursor.getString(cursor.getColumnIndex(KEY_DISPOSITIVO_MAC)));
                dispositivo.setContrasena(cursor.getString(cursor.getColumnIndex(KEY_DISPOSITIVO_CONTRASENA)));
                dispositivo.setAmbiente(cursor.getString(cursor.getColumnIndex(KEY_DISPOSITIVO_AMBIENTE)));
                dispositivo.setXx(cursor.getString(cursor.getColumnIndex(KEY_DISPOSITIVO_XX)));
                dispositivo.setYy(cursor.getString(cursor.getColumnIndex(KEY_DISPOSITIVO_YY)));
                dispositivo.setImagen(cursor.getString(cursor.getColumnIndex(KEY_DISPOSITIVO_IMAGEN)));
                dispositivo.setEncendido(cursor.getString(cursor.getColumnIndex(KEY_DISPOSITIVO_ENCENDIDO)));
                dispositivo.setApagado(cursor.getString(cursor.getColumnIndex(KEY_DISPOSITIVO_APAGADO)));
                dispositivo.setTiempo(cursor.getString(cursor.getColumnIndex(KEY_DISPOSITIVO_TIEMPO)));
            }
            return dispositivo;//regresa la variable
        }finally {
            cursor.close();//cierra el cursor
            db.close();
        }
    }

    Dispositivo getDispositivo(int posicion){
        Dispositivo dispositivo = new Dispositivo();
        String SELECT_QUERY = String.format("SELECT * FROM %s ", TABLA_DISPOSITIVOS);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(SELECT_QUERY, null);
        try {
            if (cursor.moveToPosition(posicion)) {
                dispositivo.setNombre(cursor.getString(cursor.getColumnIndex(KEY_DISPOSITIVO_NOMBRE)));
                dispositivo.setTipo(cursor.getString(cursor.getColumnIndex(KEY_DISPOSITIVO_TIPO)));
                dispositivo.setFoo(cursor.getString(cursor.getColumnIndex(KEY_DISPOSITIVO_FOO)));
                dispositivo.setMac(cursor.getString(cursor.getColumnIndex(KEY_DISPOSITIVO_MAC)));
                dispositivo.setXx(cursor.getString(cursor.getColumnIndex(KEY_DISPOSITIVO_MAC)));
                dispositivo.setYy(cursor.getString(cursor.getColumnIndex(KEY_DISPOSITIVO_MAC)));
                dispositivo.setImagen(cursor.getString(cursor.getColumnIndex(KEY_DISPOSITIVO_MAC)));
            }
        } catch (Exception e) {
            //Log.d(TAG, "Error while trying to get names from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            db.close();
        }
        return dispositivo;
    }

    boolean autoEncendido(){
        boolean resultado = false;
        String SELECT_QUERY = String.format("SELECT * FROM %s WHERE %s = ?",
                TABLA_DISPOSITIVOS, KEY_DISPOSITIVO_ENCENDIDO);//frase "selecciona el password de la tabla de redes donde el SSID es igual al que estoy solicitando"
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
                TABLA_DISPOSITIVOS, KEY_DISPOSITIVO_MAC);
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(SELECT_QUERY, new String[]{mac+""});
        db.close();
    }

}
