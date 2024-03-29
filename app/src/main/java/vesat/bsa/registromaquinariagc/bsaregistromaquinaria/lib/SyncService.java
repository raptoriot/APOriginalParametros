package vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;

import org.json.JSONObject;

import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.activity.IngresoAisladoSelectFormularioActivity;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.activity.MainActivity;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.activity.RondaSelectFormularioActivity;

public class SyncService extends Service {
    Thread slowWork = new Thread(new Runnable()
    {
        public void run()
        {
            try {
                int loop_counter = 9;
                boolean red_msg_flag = false;
                boolean red_msg_flag_alternator = false;
                boolean sync_correct_reg;
                boolean sync_correct_ron;
                final String user_id = (String) Util.loadFromSP(getApplicationContext(),String.class,Cons.User_ID);
                final String user_pass = (String) Util.loadFromSP(getApplicationContext(),String.class,Cons.User_Pass);
                final String device_id = (String) Util.loadFromSP(getApplicationContext(),String.class,Cons.Device_ID);
                final String device_register_id = (String) Util.loadFromSP(getApplicationContext(),String.class,Cons.Device_Register_ID);
                if(user_id != null && user_pass != null && device_id != null && device_register_id != null) {
                    while(true) {
                        loop_counter++;
                        if(loop_counter >= 10 || loop_counter < 0) {
                            loop_counter = 0;
                            sync_correct_reg = false;
                            sync_correct_ron = false;
                            String current_database_location = ((String) Util.loadFromSP(
                                    getApplicationContext(), String.class, Cons.Current_Database_Location));
                            if (current_database_location != null) {
                                if (!MainActivity.dialog_moving_db) {
                                    MainActivity.sync_flag = true;
                                    try {
                                        DBHelper db = new DBHelper(getApplicationContext());
                                        Cursor nesRondas = db.getRondasSyncNext();
                                        Cursor nesRegistros = db.getRegistrosSyncNext();
                                        if (MainActivity.getStatusOnline()) {
                                            if (nesRondas.getCount() > 0) { // Sincronizar Rondas Primero!
                                                boolean fail2 = false;
                                                while (nesRondas.moveToNext() && !fail2) {
                                                    try {
                                                        SyncMessage("Sincronizando Rondas " + (nesRondas.getPosition() + 1) + "/"
                                                                + nesRondas.getCount());
                                                        JSONObject json = new JSONObject();
                                                        String this_sync_id = nesRondas.getString(nesRondas.getColumnIndex("uuid"));
                                                        String synced_status = nesRondas.getString(nesRondas.getColumnIndex("synced"));
                                                        json.put("android_bd_id", nesRondas.getString(nesRondas.getColumnIndex("_ID")));
                                                        json.put("dispositivos", device_register_id);
                                                        json.put("usuarios", nesRondas.getString(nesRondas.getColumnIndex("usuarios")));
                                                        json.put("fecha", nesRondas.getString(nesRondas.getColumnIndex("fecha")));
                                                        json.put("comentario", nesRondas.getString(nesRondas.getColumnIndex("comentario")));
                                                        json.put("uuid", nesRondas.getString(nesRondas.getColumnIndex("uuid")));
                                                        json.put("synced", synced_status);
                                                        boolean pass = false;
                                                        String ans_raw = API.readWs(API.SYNC_RONDAS_DATA_FROM_DEVICE, user_id, user_pass,
                                                                device_register_id, device_id, "&extra=" + json.toString());
                                                        if (ans_raw != null && ans_raw.length() > 0) {
                                                            JSONObject ans = new JSONObject(ans_raw);
                                                            String status = Util.getJSONStringOrNull(ans, "status");
                                                            if (status != null && status.contentEquals("ok")) {
                                                                pass = true;
                                                                if (synced_status.contentEquals("0")) {
                                                                    db.markRondaAsSynced(this_sync_id);
                                                                } else {
                                                                    db.markRondaAsUpdated(this_sync_id);
                                                                }
                                                            }
                                                        }
                                                        fail2 = !pass;
                                                    } catch (Exception e) {
                                                        Util.serverLogException(e,getApplicationContext());
                                                        fail2 = true;
                                                    }
                                                }
                                                if (!fail2) {
                                                    sync_correct_ron = true;
                                                }
                                            }
                                            if (nesRegistros.getCount() > 0) {
                                                boolean fail = false;
                                                while (nesRegistros.moveToNext() && !fail) {
                                                    try {
                                                        SyncMessage("Sincronizando Registros " + (nesRegistros.getPosition() + 1) + "/"
                                                                + nesRegistros.getCount());
                                                        JSONObject json = new JSONObject();
                                                        Long this_sync_id = (long) nesRegistros.getInt(nesRegistros.getColumnIndex("_ID"));
                                                        json.put("android_bd_id", nesRegistros.getString(nesRegistros.getColumnIndex("_ID")));
                                                        json.put("dispositivos", device_register_id);
                                                        json.put("formularios", nesRegistros.getString(nesRegistros.getColumnIndex("formularios")));
                                                        json.put("usuarios", nesRegistros.getString(nesRegistros.getColumnIndex("usuarios")));
                                                        json.put("fecha", nesRegistros.getString(nesRegistros.getColumnIndex("fecha")));
                                                        json.put("datos", nesRegistros.getString(nesRegistros.getColumnIndex("datos")));
                                                        json.put("alerta_nivel", nesRegistros.getString(nesRegistros.getColumnIndex("alerta_nivel")));
                                                        json.put("latitud", nesRegistros.getString(nesRegistros.getColumnIndex("latitud")));
                                                        json.put("longitud", nesRegistros.getString(nesRegistros.getColumnIndex("longitud")));
                                                        String rondas_uuid = nesRegistros.getString(nesRegistros.getColumnIndex("rondas_uuid"));
                                                        json.put("rondas_uuid", rondas_uuid);
                                                        boolean pass = false;
                                                        if (rondas_uuid == null || db.rondaIsSynced(rondas_uuid)) {
                                                            String ans_raw = API.readWs(API.SYNC_REGISTROS_DATA_FROM_DEVICE, user_id, user_pass,
                                                                    device_register_id, device_id, "&extra=" + json.toString());
                                                            if (ans_raw != null && ans_raw.length() > 0) {
                                                                JSONObject ans = new JSONObject(ans_raw);
                                                                String status = Util.getJSONStringOrNull(ans, "status");
                                                                if (status != null && status.contentEquals("ok")) {
                                                                    pass = true;
                                                                    db.markRegistroAsSynced(this_sync_id);
                                                                }
                                                            }
                                                        }
                                                        fail = !pass;
                                                    } catch (Exception e) {
                                                        Util.serverLogException(e,getApplicationContext());
                                                        fail = true;
                                                    }
                                                }
                                                if (!fail) {
                                                    sync_correct_reg = true;
                                                }
                                            }
                                            if ((sync_correct_reg && sync_correct_ron) ||
                                                    (sync_correct_reg && nesRondas.getCount() == 0) ||
                                                    (sync_correct_ron && nesRegistros.getCount() == 0)) {
                                                SyncMessage("Sincronización Correcta.");
                                                red_msg_flag = false;
                                                Thread.sleep(5000);
                                                SyncMessage(null);
                                            } else if (sync_correct_reg || sync_correct_ron) {
                                                SyncMessage("Sincronización Incompleta.");
                                                red_msg_flag = false;
                                                Thread.sleep(5000);
                                                //SyncMessage(null);
                                            } else if (nesRegistros.getCount() > 0 || nesRondas.getCount() > 0) {
                                                SyncMessage("Error de Sincronización.");
                                                red_msg_flag = true;
                                                Thread.sleep(5000);
                                                //SyncMessage(null);
                                            } else {
                                                red_msg_flag = false;
                                                SyncMessage(null);
                                            }
                                        } else {
                                            if (nesRegistros.getCount() > 0 || nesRondas.getCount() > 0) {
                                                red_msg_flag = false;
                                                SyncMessage("Pendiente Envío de Datos.");
                                            } else {
                                                red_msg_flag = false;
                                                SyncMessage(null);
                                            }
                                        }
                                        nesRegistros.close();
                                        nesRondas.close();
                                        db.close();
                                    }
                                    catch (Exception e) {
                                        Util.serverLogException(e,getApplicationContext());
                                        SyncMessage("Excepción al Sincronizar.");
                                        red_msg_flag = true;
                                        Thread.sleep(5000);
                                    }
                                    MainActivity.sync_flag = false;
                                }
                            }
                        }
                        if(red_msg_flag)
                        {
                            red_msg_flag_alternator = !red_msg_flag_alternator;
                            SyncMessageColor(red_msg_flag_alternator);
                        }
                        else
                        {
                            SyncMessageColor(false);
                        }
                        Thread.sleep(1000);
                    }
                }
            }
            catch(InterruptedException ignored){}
        }
    });
    
    private void SyncMessage(String msg)
    {
        MainActivity.SyncMessage(msg);
        RondaSelectFormularioActivity.SyncMessage(msg);
        IngresoAisladoSelectFormularioActivity.SyncMessage(msg);
    }

    private void SyncMessageColor(boolean status)
    {
        MainActivity.SyncMessageColor(status);
        RondaSelectFormularioActivity.SyncMessageColor(status);
        IngresoAisladoSelectFormularioActivity.SyncMessageColor(status);
    }

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
