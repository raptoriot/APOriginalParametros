package vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.obj.Formulario;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.obj.RondaElem;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.obj.TurnoElem;

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
                values.put("enable_sync", "1"); // Siempre usar 1, en una version temprana se uso 0 para esperar
                                                // a que se completara la ronda
            } else {
                values.putNull("rondas_uuid");
                values.put("enable_sync", "1"); // Siempre usar 1, en una version temprana se uso 0 para esperar
                                                // a que se completara la ronda
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
        String selection = "(synced = '0') OR (synced = '1' AND cerrada = '1')";
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
    }

    public boolean rondaIsSynced(String uuid)
    {
        boolean ret = false;
        Cursor cur = getReadableDatabase().query(
                        "rondas",
                        null,
                        "uuid = ? AND (synced = '1' OR synced = '2')",
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

    public void markRondaAsSynced(String uuid) {
        ContentValues cv = new ContentValues();
        cv.put("synced","1");
        getWritableDatabase().update("rondas",cv,"uuid = ?",new String[]{""+uuid});
    }

    public void markRondaAsUpdated(String uuid) {
        ContentValues cv = new ContentValues();
        cv.put("synced","2");
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

    public byte getFormNivelAlertaForRonda(String rondas_uuid,long form_id)
    {
        byte ret = 0;
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
            try
            {
                cur.moveToFirst();
                ret = Byte.parseByte(cur.getString(cur.getColumnIndex("alerta_nivel")));
            }
            catch (NumberFormatException ignored){}
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

    public ArrayList<RondaElem> getLast5Rondas(Context context)
    {
        ArrayList<RondaElem> t_ronda = new ArrayList<>();
        Cursor cur1 = getReadableDatabase().query(
                "rondas",
                null,
                null,
                null,
                null,
                null,
                "_ID DESC",
                "5");
        if(cur1.getCount() > 0)
        {
            cur1.moveToFirst();
            String fecha_5_rondas = cur1.getString(cur1.getColumnIndex("fecha"));
            ArrayList<TurnoElem> t_elem = Util.getTurnosUntil(context,System.currentTimeMillis(),
                    Util.fechaAMillis(fecha_5_rondas));
            if(t_elem.size() > 0) {
                ArrayList<RondaElem> aux_arr = new ArrayList<>();
                String lower_date = t_elem.get(t_elem.size() - 1).date_init;
                Cursor cur2 = getReadableDatabase().query( // Obtener todas las rondas
                        // del turno de al menos la quinta mas antigua para el conteo
                        "rondas",
                        null,
                        "fecha >= ?",
                        new String[]{lower_date},
                        null,
                        null,
                        "_ID ASC",
                        null);
                cur2.moveToFirst();
                while(cur2.moveToNext())
                {
                    String ronda_fecha = cur2.getString(cur2.getColumnIndex("fecha"));
                    boolean found = false;
                    for(TurnoElem t : t_elem)
                    {
                        if(Util.dateOnTurno(ronda_fecha,t))
                        {
                            aux_arr.add(new RondaElem(ronda_fecha,t.date_init,t.counter));
                            t.counter++;
                            found = true;
                            break;
                        }
                    }
                    if(!found)
                    {
                        aux_arr.add(new RondaElem(ronda_fecha,null,null));
                    }
                }
                cur2.close();
                for(int x = aux_arr.size() - 1;t_ronda.size() < 5 && x >= 0;x--)
                {
                    t_ronda.add(aux_arr.get(x));
                }
            }
        }
        cur1.close();
        return t_ronda;
    }


}

