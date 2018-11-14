package vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final int Database_Version = 1;

    public DBHelper(Context context) {
        super(context, Util.getCurrentDatabaseLocationSafe(context) + Cons.Database_Name, null,
                Database_Version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crear tablas si no existen
        db.execSQL("CREATE TABLE IF NOT EXISTS registros(_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + " formularios INTEGER NOT NULL, usuarios INTEGER NOT NULL, fecha TEXT NOT NULL, "
                + " datos TEXT NOT NULL, alerta_nivel INTEGER NOT NULL, latitud REAL, longitud REAL);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public long addRegistro(int formularios,int usuarios,String fecha,String datos,int alerta_nivel,
                            Double latitud, Double longitud)
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
            SQLiteDatabase db = getWritableDatabase();
            return db.insert("registros", null, values);
        }
        catch(NumberFormatException e){return -1;}
    }

    public Cursor getAllRegistros() {
        return getReadableDatabase()
                .query(
                        "registros",
                        null,
                        null,
                        null,
                        null,
                        null,
                        "_ID DESC",
                        null);
    }

    public Cursor getRegistrosSyncNext(long id) {
        String selection = "_ID > ?";
        String[] selectionArgs = { (""+id)};
        return getReadableDatabase()
                .query(
                        "registros",
                        null,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        "_ID ASC",
                        null);
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
                        null);
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

