package vesat.bsa.registromaquinariagc.bsaregistromaquinaria.activity;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.R;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib.API;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib.AdapterFormularioListIngresoAislado;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib.Cons;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib.ThreadSharedContent;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib.Util;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.obj.Formulario;

public class IngresoAisladoSelectFormularioActivity extends AppCompatActivity {

    public static IngresoAisladoSelectFormularioActivity self = null;
    private String device_id;
    private String device_register_id;
    private String user_id;
    private String user_pass;
    private String user_email;

    private Thread thread_timer = null;
    private Thread thread_online = null;
    private ThreadSharedContent thread_shared = new ThreadSharedContent();

    protected AdapterFormularioListIngresoAislado adapterFormularioListIngresoAislado = null;
    private ArrayList<Formulario> arrFormulario = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingreso_aislado_select_formulario);
        self = this;
        String last_msg = getIntent().getStringExtra("last_msg");
        if(last_msg != null)
        {
            SyncMessage(last_msg);
        }
        loadVars();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Ingreso Aislado");
        ((TextView) findViewById(R.id.current_user)).setText(user_email);
    }

    private void loadVars()
    {
        device_id = (String) Util.loadFromSP(getApplicationContext(),String.class,Cons.Device_ID);
        device_register_id = (String) Util.loadFromSP(getApplicationContext(),String.class,Cons.Device_Register_ID);
        user_id = (String) Util.loadFromSP(getApplicationContext(),String.class,Cons.User_ID);
        user_pass = (String) Util.loadFromSP(getApplicationContext(),String.class,Cons.User_Pass);
        user_email = (String) Util.loadFromSP(getApplicationContext(),String.class,Cons.User_Email);
    }

    public void onDestroy()
    {
        self = null;
        super.onDestroy();
    }

    protected void onResume()
    {
        super.onResume();
        self = this;
        loadVars();
        if(thread_timer == null) {
            thread_timer = thread_shared.threadTimer(this);
            thread_timer.start();
        }
        if(thread_online == null)
        {
            thread_online = thread_shared.threadPing(this);
            thread_online.start();
        }
        loadFormularios();
    }

    protected void onPause()
    {
        if(thread_timer != null)
        {
            thread_timer.interrupt();
            thread_timer = null;
        }
        if(thread_online != null)
        {
            thread_online.interrupt();
            thread_online = null;
        }
        super.onPause();
    }

    private void loadFormularios()
    {
        findViewById(R.id.mainFormPreload).setVisibility(View.VISIBLE);
        findViewById(R.id.mainFormList).setVisibility(View.GONE);
        Thread net = new Thread(new Runnable()
        {
            @Override
            public void run() {
                try {
                    if (adapterFormularioListIngresoAislado != null) {
                        arrFormulario.clear();
                    }
                    String ans_raw = API.readWs(API.GET_FORMULARIOS_LIST,user_id,user_pass,device_register_id,
                            device_id,null);
                    if(ans_raw != null && ans_raw.length() > 0) {
                        JSONObject ans = new JSONObject(ans_raw);
                        String status = Util.getJSONStringOrNull(ans, "status");
                        if (status != null && status.contentEquals("ok") && ans.has("formularios"))
                        {
                            Util.saveToSP(getApplicationContext(),ans_raw,Cons.QueryCache_FormList);
                            JSONArray jformularios = ans.getJSONArray("formularios");
                            for(int x = 0;x < jformularios.length();x++)
                            {
                                JSONObject jform = jformularios.getJSONObject(x);
                                Formulario f = new Formulario();
                                f.loadFromJSON(jform);
                                arrFormulario.add(f);
                            }
                        }
                    }
                    else
                    {
                        String ans_cache = (String) Util.loadFromSP(getApplicationContext(),String.class,Cons.QueryCache_FormList);
                        if(ans_cache != null && ans_cache.length() > 0) {
                            JSONObject ans = new JSONObject(ans_cache);
                            String status = Util.getJSONStringOrNull(ans, "status");
                            if (status != null && status.contentEquals("ok") && ans.has("formularios")) {
                                JSONArray jformularios = ans.getJSONArray("formularios");
                                for (int x = 0; x < jformularios.length(); x++) {
                                    JSONObject jform = jformularios.getJSONObject(x);
                                    Formulario f = new Formulario();
                                    f.loadFromJSON(jform);
                                    arrFormulario.add(f);
                                }
                            }
                        }
                    }
                    try {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(adapterFormularioListIngresoAislado == null) {
                                    adapterFormularioListIngresoAislado = new AdapterFormularioListIngresoAislado(getApplicationContext(),
                                            arrFormulario);
                                    ((RecyclerView) findViewById(R.id.mainFormListView)).setNestedScrollingEnabled(false);
                                    ((RecyclerView) findViewById(R.id.mainFormListView)).setAdapter(adapterFormularioListIngresoAislado);
                                    ((RecyclerView) findViewById(R.id.mainFormListView)).setLayoutManager(
                                            new GridLayoutManager(self,2));
                                }
                                adapterFormularioListIngresoAislado.notifyDataSetChanged();
                            }
                        });
                    } catch (NullPointerException ignored) {
                    }
                } catch (JSONException ignored) {

                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            findViewById(R.id.mainFormPreload).setVisibility(View.GONE);
                            findViewById(R.id.mainFormList).setVisibility(View.VISIBLE);
                        }
                        catch (NullPointerException e){e.printStackTrace();}
                    }
                });
            }
        });
        net.start();
    }

    public static void SyncMessage(final String msg)
    {
        try {
            if (self != null) {
                self.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (msg == null) {
                                self.findViewById(R.id.sync_layout).setVisibility(View.GONE);
                            } else {
                                ((TextView) self.findViewById(R.id.sync_msg)).setText(msg);
                                self.findViewById(R.id.sync_layout).setVisibility(View.VISIBLE);
                            }
                        }
                        catch (NullPointerException ignored){}
                    }
                });
            }
        }
        catch (NullPointerException ignored){}
    }

    public static void SyncMessageColor(final boolean status)
    {
        try {
            if (self != null) {
                self.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if(status) {
                                self.findViewById(R.id.sync_layout).
                                        setBackgroundColor(Color.rgb(0xff,0x00,0x00));
                            }
                            else {
                                self.findViewById(R.id.sync_layout).
                                        setBackgroundColor(Color.rgb(0xff,0xd9,0x99));
                            }
                        }
                        catch (NullPointerException ignored){}
                    }
                });
            }
        }
        catch (NullPointerException ignored){}
    }
}
