package vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib;

import android.app.Activity;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.R;

public class ThreadSharedContent {

    public boolean status_online = false;

    public Thread threadTimer(final Activity act)
    {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                while(true)
                {
                    try {
                        act.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((TextView) act.findViewById(R.id.tag_hora)).setText(Util.getHoraActual());
                            }
                        });
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException ignored){}
                }
            }
        });
    }

    public Thread threadPing(final Activity act)
    {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while(true) {
                        boolean pass = false;
                        String ans_raw = API.readWs(API.PING, "0","0","0","0",null);
                        if(ans_raw != null && ans_raw.length() > 0)
                        {
                            try {
                                JSONObject ans = new JSONObject(ans_raw);
                                String status = Util.getJSONStringOrNull(ans, "status");
                                if (status != null && status.contentEquals("ok")) {
                                    pass = true;
                                }
                            }
                            catch (JSONException ignored){}
                        }
                        status_online = pass;
                        act.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(status_online) {
                                    ((ImageView) act.findViewById(R.id.semaforo_estado)).setImageResource(R.drawable.verde);
                                }
                                else
                                {
                                    ((ImageView) act.findViewById(R.id.semaforo_estado)).setImageResource(R.drawable.rojo);
                                }
                            }
                        });
                        Thread.sleep(5000);
                    }
                }
                catch (InterruptedException ignored){}
            }
        });
    }
}
