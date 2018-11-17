package vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
                + " rondas INTEGER, enable_sync INTEGER NOT NULL, synced INTEGER NOT NULL);");
        db.execSQL("CREATE TABLE IF NOT EXISTS rondas(_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + " usuarios INTEGER NOT NULL, fecha TEXT NOT NULL, comentario TEXT,"
                + " cerrada INTEGER NOT NULL, synced INTEGER NOT NULL)"); /* v=2 */
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion == 1 && newVersion == 2){upgradev1Tov2(db);}
    }

    private void upgradev1Tov2(SQLiteDatabase db)
    {
        db.execSQL("ALTER TABLE registros ADD COLUMN rondas INTEGER");
        db.execSQL("ALTER TABLE registros ADD COLUMN enable_sync INTEGER NOT NULL DEFAULT '1'");
        db.execSQL("ALTER TABLE registros ADD COLUMN synced INTEGER NOT NULL DEFAULT '1'");
        db.execSQL("CREATE TABLE IF NOT EXISTS rondas(_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + " usuarios INTEGER NOT NULL, fecha TEXT NOT NULL, comentario TEXT,"
                + " cerrada INTEGER NOT NULL, synced INTEGER NOT NULL)");
    }

    public long addRegistro(int formularios,int usuarios,String fecha,String datos,int alerta_nivel,
                            Double latitud, Double longitud, Long rondas)
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
            if(rondas != null) {
                values.put("rondas", rondas);
                values.put("enable_sync", "0");
            } else {
                values.putNull("rondas");
                values.put("enable_sync", "1");
            }
            values.put("synced","0");
            SQLiteDatabase db = getWritableDatabase();
            return db.insert("registros", null, values);
        }
        catch(NumberFormatException e){return -1;}
    }

    public long addNewRonda(int usuarios,String fecha,String comentario)
    {
        try {
            ContentValues values = new ContentValues();
            // Pares clave-valor
            values.put("usuarios", usuarios);
            values.put("fecha", fecha);
            if(comentario != null) {
                values.put("comentario", comentario);
            } else {
                values.putNull("comentario");
            }
            values.put("cerrada","0");
            values.put("synced","0");
            SQLiteDatabase db = getWritableDatabase();
            return db.insert("rondas", null, values);
        }
        catch(NumberFormatException e){return -1;}
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
                        "1");
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
                        "1");
    }

    public void markRondaAsCerrada(long id,String comentario) {
        ContentValues cv = new ContentValues();
        cv.put("cerrada","1");
        if(comentario != null) {
            cv.put("comentario", comentario);
        }
        getWritableDatabase().update("rondas",cv,"_ID = ?",new String[]{""+id});
        enableSyncOnRegistrosOfRonda(id);
    }

    public boolean rondaIsSynced(long ronda_id)
    {
        boolean ret = false;
        Cursor cur = getReadableDatabase().query(
                        "rondas",
                        null,
                        "_ID = ? AND synced = '1'",
                        new String[]{""+ronda_id},
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

    private void enableSyncOnRegistrosOfRonda(long ronda_id)
    {
        ContentValues cv = new ContentValues();
        cv.put("enable_sync","1");
        getWritableDatabase().update("registros",cv,"rondas = ?",new String[]{""+ronda_id});
    }

    public void markRondaAsSynced(long id) {
        ContentValues cv = new ContentValues();
        cv.put("synced","1");
        getWritableDatabase().update("rondas",cv,"_ID = ?",new String[]{""+id});
    }

    public Cursor getRondaById(long id) {
        return getReadableDatabase()
                .query(
                        "rondas",
                        null,
                        "_ID = ",
                        new String[]{""+id},
                        null,
                        null,
                        "_ID DESC",
                        "1");
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

