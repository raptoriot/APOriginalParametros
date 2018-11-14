package vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;

import org.json.JSONException;
import org.json.JSONObject;

import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.activity.MainActivity;

public class SyncService extends Service {
    Thread slowWork = new Thread(new Runnable()
    {
        public void run()
        {
            try {
                boolean sync_correct;
                String user_id = (String) Util.loadFromSP(getApplicationContext(),String.class,Cons.User_ID);
                String user_pass = (String) Util.loadFromSP(getApplicationContext(),String.class,Cons.User_Pass);
                while(user_id != null && user_pass != null) {
                    sync_correct = false;
                    String current_database_location = ((String) Util.loadFromSP(
                            getApplicationContext(),String.class,Cons.Current_Database_Location));
                    if(current_database_location != null) {
                        Long last_sync_id = (Long) Util.loadFromSP(getApplicationContext(),Long.class,Cons.last_sync_id);
                        if(last_sync_id == null)
                        {
                            last_sync_id = (long) 0;
                        }
                        if(!MainActivity.dialog_moving_db) {
                            MainActivity.sync_flag = true;
                            DBHelper db = new DBHelper(getApplicationContext());
                            Cursor nes = db.getRegistrosSyncNext(last_sync_id);
                            if(MainActivity.status_online) {
                                if (true) { // (nes.getCount() > 0) {
                                    boolean fail = false;
                                    while (nes.moveToNext() && !fail) {
                                        //try {
                                            MainActivity.SyncMessage("Sincronizando " + (nes.getPosition() + 1) + "/"
                                                    + nes.getCount());
                                            JSONObject json = new JSONObject();
                                            Long this_sync_id = (long) nes.getInt(nes.getColumnIndex("_ID"));
                                            /*String android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                                                    Settings.Secure.ANDROID_ID);
                                            json.put("device_id", android_id);
                                            json.put("id", nes.getInt(nes.getColumnIndex("_ID")));
                                            json.put("nivel", nes.getDouble(nes.getColumnIndex("nivel")));
                                            json.put("potenciometro", nes.getDouble(nes.getColumnIndex("potenciometro")));
                                            json.put("manometro", nes.getDouble(nes.getColumnIndex("manometro")));
                                            json.put("comentario", nes.getString(nes.getColumnIndex("comentario")));
                                            json.put("foto", nes.getString(nes.getColumnIndex("foto")));
                                            json.put("operador", nes.getInt(nes.getColumnIndex("operador")));
                                            json.put("fecha", nes.getString(nes.getColumnIndex("fecha")));
                                            json.put("fecha_t", nes.getLong(nes.getColumnIndex("fecha_t")));
                                            json.put("latitud", nes.getDouble(nes.getColumnIndex("latitud")));
                                            json.put("longitud", nes.getDouble(nes.getColumnIndex("longitud")));*/
                                            boolean pass = false;
                                            /*String ans_raw = API.readWs(API.SEND_NIVEL_ESPESADOR, user_id, user_pass,
                                                    json.toString());
                                            if(ans_raw != null && ans_raw.length() > 0) {
                                                JSONObject ans = new JSONObject(ans_raw);
                                                String status = Util.getJSONStringOrNull(ans, "status");
                                                if (status != null && status.contentEquals("ok")) {
                                                    pass = true;
                                                    Util.saveToSP(getApplicationContext(),this_sync_id,Cons.last_sync_id);
                                                }
                                            }*/
                                            fail = !pass;
                                       // } catch (JSONException ignored) {
                                       //     fail = true;
                                       // }
                                    }
                                    if (fail) {
                                        MainActivity.SyncMessage("Error de Sincronización.");
                                    }
                                    else
                                    {
                                        sync_correct = true;
                                    }
                                } else {
                                    MainActivity.SyncMessage(null);
                                }
                            }
                            else
                            {
                                if(nes.getCount() > 0)
                                {
                                    MainActivity.SyncMessage("Pendiente Envío de Datos.");
                                }
                                else
                                {
                                    MainActivity.SyncMessage(null);
                                }
                            }
                            db.close();
                            if(sync_correct)
                            {
                                MainActivity.SyncMessage("Sincronización Correcta.");
                                Thread.sleep(3000);
                                MainActivity.SyncMessage(null);
                            }
                            MainActivity.sync_flag = false;
                        }
                    }
                    Thread.sleep(5000);
                }
            }
            catch(InterruptedException e){}
        }
    });

    public IBinder onBind(Intent intent)
    {
        return null;
    }

    public void onCreate(){
        if(slowWork.getState() == Thread.State.NEW)
        {
            slowWork.start();
        }
        super.onCreate();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
}
