package vesat.bsa.registromaquinariagc.bsaregistromaquinaria.activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.R;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib.API;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib.AdapterFormularioListRonda;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib.Cons;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib.DBHelper;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib.ThreadSharedContent;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib.Util;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.obj.Formulario;

public class RondaSelectFormularioActivity extends AppCompatActivity {

    public static RondaSelectFormularioActivity self = null;
    private String device_id;
    private String device_register_id;
    private String user_id;
    private String user_pass;
    private String user_email;
    private String current_ronda_id;
    private Thread thread_timer = null;
    private Thread thread_online = null;
    private ThreadSharedContent thread_shared = new ThreadSharedContent();

    protected AdapterFormularioListRonda adapterFormularioListRonda= null;
    private ArrayList<Formulario> arrFormulario = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ronda_select_formulario);
        self = this;
        String last_msg = getIntent().getStringExtra("last_msg");
        if(last_msg != null)
        {
            SyncMessage(last_msg);
        }
        loadVars();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Ronda");
        ((TextView) findViewById(R.id.current_user)).setText(user_email);
    }

    private void loadVars()
    {
        device_id = (String) Util.loadFromSP(getApplicationContext(),String.class,Cons.Device_ID);
        device_register_id = (String) Util.loadFromSP(getApplicationContext(),String.class,Cons.Device_Register_ID);
        user_id = (String) Util.loadFromSP(getApplicationContext(),String.class,Cons.User_ID);
        user_pass = (String) Util.loadFromSP(getApplicationContext(),String.class,Cons.User_Pass);
        user_email = (String) Util.loadFromSP(getApplicationContext(),String.class,Cons.User_Email);
        current_ronda_id = ((String) Util.loadFromSP(getApplicationContext(),String.class,Cons.Current_Ronda_ID));
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
        checkCurRonda();
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

    public void onDestroy()
    {
        self = null;
        super.onDestroy();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        final RondaSelectFormularioActivity m_self = this;
        getMenuInflater().inflate(R.menu.activity_ronda_menu, menu);
        MenuItem saveItem = menu.findItem(R.id.menu_save);
        if (saveItem != null) {
            saveItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if(current_ronda_id != null) {
                        final DBHelper db = new DBHelper(m_self);
                        if (db.isRondaComplete(current_ronda_id, arrFormulario)) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(m_self);
                            builder.setTitle("Ronda");
                            builder.setPositiveButton("Cerrar Ronda", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    db.markRondaAsCerrada(current_ronda_id, null);
                                    Util.saveToSP(m_self,null,Cons.Current_Ronda_ID);
                                    dialog.dismiss();
                                    m_self.finish();
                                }
                            });
                            builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                            builder.show();
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(m_self);
                            builder.setTitle("¡RONDA INCOMPLETA!");
                            @SuppressLint("InflateParams") View viewInflated = LayoutInflater.from(m_self
                            ).inflate(R.layout.dialog_text, null, false);
                            final EditText input = viewInflated.findViewById(R.id.input);
                            input.setFilters(new InputFilter[]{
                                    new InputFilter() {
                                        public CharSequence filter(CharSequence src, int start,
                                                                   int end, Spanned dst, int dstart, int dend) {
                                            if (src.equals("")) {
                                                return src;
                                            }
                                            if (src.toString().matches
                                                    ("[0-9A-Za-z*+?$.|" +
                                                            "()\\- =¿!#@%&,;<>_¬¡:ñÑçÇáóéúíÁÓÉÚÍ]+")) {
                                                return src;
                                            }
                                            return "";
                                        }
                                    }
                            });
                            builder.setView(viewInflated);
                            builder.setPositiveButton("Cerrar Ronda Incompleta", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String justificacion = input.getText().toString();
                                    if (justificacion.length() > 0) {
                                        db.markRondaAsCerrada(current_ronda_id, justificacion);
                                        Util.saveToSP(m_self,null,Cons.Current_Ronda_ID);
                                        dialog.dismiss();
                                        m_self.finish();
                                    } else {
                                        Toast.makeText(m_self, "Debe ingresar justificación",
                                                Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }
                                }
                            });
                            builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                            builder.show();
                        }
                        db.close();
                    }
                    return true;
                }
            });
        }
        return super.onCreateOptionsMenu(menu);
    }

    private void checkCurRonda()
    {
        try {
            if (current_ronda_id == null) {
                DBHelper db = new DBHelper(this);
                String t_fecha = Util.getFechaFullActual();
                Util.saveToSP(this,t_fecha,Cons.Current_Ronda_Fecha);
                current_ronda_id = ""+db.addNewRonda(Integer.parseInt(user_id), t_fecha,
                        device_register_id);
                Util.saveToSP(this,current_ronda_id,Cons.Current_Ronda_ID);
                db.close();
            }
        }
        catch (NumberFormatException ignored){}
        String r_fecha = (String) Util.loadFromSP(this,String.class,Cons.Current_Ronda_Fecha);
        if(r_fecha != null)
        {
            ((TextView) findViewById(R.id.tag_horaronda)).setText(("Ronda: " + r_fecha));
        }
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
                    if (adapterFormularioListRonda != null) {
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
                                if(adapterFormularioListRonda == null) {
                                    adapterFormularioListRonda = new AdapterFormularioListRonda(getApplicationContext(), arrFormulario,
                                            current_ronda_id);
                                    ((RecyclerView) findViewById(R.id.mainFormListView)).setNestedScrollingEnabled(false);
                                    ((RecyclerView) findViewById(R.id.mainFormListView)).setAdapter(adapterFormularioListRonda);
                                    ((RecyclerView) findViewById(R.id.mainFormListView)).setLayoutManager(
                                            new GridLayoutManager(self,2));
                                }
                                adapterFormularioListRonda.notifyDataSetChanged();
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
}
