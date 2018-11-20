package vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.obj.Formulario;

public class DBHelper extends SQLiteOpenHelper {

    private static final int Database_Version = 2;

    public DBHelper(Context context) {
        super(context, Util.getCurrentDatabaseLocationSafe(context) + Cons.Database_Name, null,
                Database_Version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
      /* v=1   db.execSQL("CREATE TABLE IF NOT EXISTS registros(_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + " formularios INTEGER NOT NULL, usuarios INTEGER NOT NULL, fecha TEXT NOT NULL, "
                + " datos TEXT NOT NULL, alerta_nivel INTEGER NOT NULL, latitud REAL, longitud REAL);"); */
      /* v=2 */
        db.execSQL("CREATE TABLE IF NOT EXISTS registros(_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + " formularios INTEGER NOT NULL, usuarios INTEGER NOT NULL, fecha TEXT NOT NULL, "
                + " datos TEXT NOT NULL, alerta_nivel INTEGER NOT NULL, latitud REAL, longitud REAL, "
                + " rondas_uuid INTEGER, enable_sync INTEGER NOT NULL, synced INTEGER NOT NULL);");
        db.execSQL("CREATE TABLE IF NOT EXISTS rondas(_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + " usuarios INTEGER NOT NULL, fecha TEXT NOT NULL, comentario TEXT,"
                + " cerrada INTEGER NOT NULL, synced INTEGER NOT NULL, uuid TEXT NOT NULL)"); /* v=2 */
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion == 1 && newVersion == 2){upgradev1Tov2(db);}
    }

    private void upgradev1Tov2(SQLiteDatabase db)
    {
        db.execSQL("ALTER TABLE registros ADD COLUMN rondas_uuid TEXT");
        db.execSQL("ALTER TABLE registros ADD COLUMN enable_sync INTEGER NOT NULL DEFAULT '1'");
        db.execSQL("ALTER TABLE registros ADD COLUMN synced INTEGER NOT NULL DEFAULT '1'");
        db.execSQL("CREATE TABLE IF NOT EXISTS rondas(_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + " usuarios INTEGER NOT NULL, fecha TEXT NOT NULL, comentario TEXT,"
                + " cerrada INTEGER NOT NULL, synced INTEGER NOT NULL, uuid TEXT NOT NULL)");
    }

    public long addNewRegistro(int formularios,int usuarios,String fecha,String datos,int alerta_nivel,
                            Double latitud, Double longitud, String rondas_uuid)
    {
        try {
            ContentValues values = new ContentValues();
            // Pares clave-valor
            values.put("formularios", formularios);
            values.put("usuarios", usuarios);
            values.put("fecha", fecha);
            values.put("datos", datos);
            values.put("alerta_nivel", alerta_nivel);
            if(latitud != null) {
                values.put("latitud", latitud);
            } else {
                values.putNull("latitud");
            }
            if(longitud != null) {
                values.put("longitud", longitud);
            } else {
                values.putNull("longitud");
            }
            if(rondas_uuid != null) {
                values.put("rondas_uuid", rondas_uuid);
                values.put("enable_sync", "0");
            } else {
                values.putNull("rondas_uuid");
                values.put("enable_sync", "1");
            }
            values.put("synced","0");
            SQLiteDatabase db = getWritableDatabase();
            return db.insert("registros", null, values);
        }
        catch(NumberFormatException e){return -1;}
    }

    public String addNewRonda(int usuarios,String fecha, String dev_reg_id)
    {
        try {
            String uuid = dev_reg_id + "-" + System.currentTimeMillis();
            ContentValues values = new ContentValues();
            // Pares clave-valor
            values.put("usuarios", usuarios);
            values.put("fecha", fecha);
            values.putNull("comentario");
            values.put("cerrada","0");
            values.put("synced","0");
            values.put("uuid",uuid);
            SQLiteDatabase db = getWritableDatabase();
            if(db.insert("rondas", null, values) > 0)
            {
                return  uuid;
            }
            else
            {
                return null;
            }
        }
        catch(NumberFormatException e){return null;}
    }

    public Cursor getRegistrosSyncNext() {
        String selection = "synced = '0' AND enable_sync = '1'";
        return getReadableDatabase()
                .query(
                        "registros",
                        null,
                         selection,
                        null,
                        null,
                        null,
                        "_ID ASC",
                        null);
    }

    public void markRegistroAsSynced(long id) {
        ContentValues cv = new ContentValues();
        cv.put("synced","1");
        getWritableDatabase().update("registros",cv,"_ID = ?",new String[]{""+id});
    }

    public Cursor getRondasSyncNext() {
        String selection = "synced = '0' AND cerrada = '1'";
        return getReadableDatabase()
                .query(
                        "rondas",
                        null,
                        selection,
                        null,
                        null,
                        null,
                        "_ID ASC",
                        null);
    }

    public void markRondaAsCerrada(String uuid,String comentario) {
        ContentValues cv = new ContentValues();
        cv.put("cerrada","1");
        if(comentario != null) {
            cv.put("comentario", comentario);
        }
        getWritableDatabase().update("rondas",cv,"uuid = ?",new String[]{""+uuid});
        enableSyncOnRegistrosOfRonda(uuid);
    }

    public boolean rondaIsSynced(String uuid)
    {
        boolean ret = false;
        Cursor cur = getReadableDatabase().query(
                        "rondas",
                        null,
                        "uuid = ? AND synced = '1'",
                        new String[]{""+uuid},
                        null,
                        null,
                        null,
                        "1");
        if(cur.getCount() > 0)
        {
            ret = true;
        }
        cur.close();
        return ret;
    }

    private void enableSyncOnRegistrosOfRonda(String rondas_uuid)
    {
        ContentValues cv = new ContentValues();
        cv.put("enable_sync","1");
        getWritableDatabase().update("registros",cv,"rondas_uuid = ?",
                new String[]{""+rondas_uuid});
    }

    public void markRondaAsSynced(String uuid) {
        ContentValues cv = new ContentValues();
        cv.put("synced","1");
        getWritableDatabase().update("rondas",cv,"uuid = ?",new String[]{""+uuid});
    }

    public boolean isRondaComplete(String rondas_uuid,ArrayList<Formulario> forms)
    {
        for(Formulario f : forms)
        {
            try {
                if (!isFormSubmittedForRonda(rondas_uuid, Long.parseLong(f.id))) {
                    return false;
                }
            }
            catch(NumberFormatException ignored){return false;}
        }
        return true;
    }

    public boolean isFormSubmittedForRonda(String rondas_uuid,long form_id)
    {
        boolean ret = false;
        Cursor cur = getReadableDatabase().query(
                "registros",
                null,
                "rondas_uuid = ? AND formularios = ?",
                new String[]{""+rondas_uuid, ""+form_id},
                null,
                null,
                null,
                "1");
        if(cur.getCount() > 0)
        {
            ret = true;
        }
        cur.close();
        return ret;
    }

    public Cursor getAllRegistrosOfFormulario(int formulario_id) {
        return getReadableDatabase()
                .query(
                        "registros",
                        null,
                        "formularios = ?",
                        new String[]{""+formulario_id},
                        null,
                        null,
                        "_ID DESC",
                        "100");
    }

    public Cursor getRegistroById(int elem_id) {
        return getReadableDatabase()
                .query(
                        "registros",
                        null,
                        "_ID = ?",
                        new String[]{""+elem_id},
                        null,
                        null,
                        null,
                        null);
    }
}

