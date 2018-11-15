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
                final String user_id = (String) Util.loadFromSP(getApplicationContext(),String.class,Cons.User_ID);
                final String user_pass = (String) Util.loadFromSP(getApplicationContext(),String.class,Cons.User_Pass);
                final String device_id = (String) Util.loadFromSP(getApplicationContext(),String.class,Cons.Device_ID);
                final String device_register_id = (String) Util.loadFromSP(getApplicationContext(),String.class,Cons.Device_Register_ID);
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
                                if (nes.getCount() > 0) {
                                    boolean fail = false;
                                    while (nes.moveToNext() && !fail) {
                                        try {
                                            MainActivity.SyncMessage("Sincronizando " + (nes.getPosition() + 1) + "/"
                                                    + nes.getCount());
                                            JSONObject json = new JSONObject();
                                            Long this_sync_id = (long) nes.getInt(nes.getColumnIndex("_ID"));
                                            json.put("android_bd_id", nes.getString(nes.getColumnIndex("_ID")));
                                            json.put("dispositivos", device_register_id);
                                            json.put("formularios", nes.getString(nes.getColumnIndex("formularios")));
                                            json.put("usuarios", nes.getString(nes.getColumnIndex("usuarios")));
                                            json.put("fecha", nes.getString(nes.getColumnIndex("fecha")));
                                            json.put("datos", nes.getString(nes.getColumnIndex("datos")));
                                            json.put("alerta_nivel", nes.getString(nes.getColumnIndex("alerta_nivel")));
                                            json.put("latitud", nes.getString(nes.getColumnIndex("latitud")));
                                            json.put("longitud", nes.getString(nes.getColumnIndex("longitud")));
                                            boolean pass = false;
                                            String ans_raw = API.readWs(API.SYNC_REGISTROS_DATA_FROM_DEVICE, user_id, user_pass,
                                                    device_register_id,device_id,json.toString());
                                            if(ans_raw != null && ans_raw.length() > 0) {
                                                JSONObject ans = new JSONObject(ans_raw);
                                                String status = Util.getJSONStringOrNull(ans, "status");
                                                if (status != null && status.contentEquals("ok")) {
                                                    pass = true;
                                                    Util.saveToSP(getApplicationContext(),this_sync_id,Cons.last_sync_id);
                                                }
                                            }
                                            fail = !pass;
                                        } catch (JSONException ignored) {
                                            fail = true;
                                        }
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
