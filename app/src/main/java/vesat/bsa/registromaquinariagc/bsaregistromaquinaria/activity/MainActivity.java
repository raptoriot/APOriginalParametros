package vesat.bsa.registromaquinariagc.bsaregistromaquinaria.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.R;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib.API;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib.AdapterFormularioHistoryRonda;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib.AdapterFormularioListHistorico;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib.Cons;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib.DBHelper;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib.SyncService;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib.ThreadSharedContent;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib.Util;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.obj.Formulario;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.obj.RondaElem;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    protected static MainActivity self = null;
    protected static String last_msg = null;

    private String device_id;
    private String device_register_id;
    private String user_id;
    private String user_pass;
    private String user_email;
    private String user_name;
    private String current_database_location;
    private String current_ronda_id;

    private Thread thread_timer = null;
    private Thread thread_online = null;
    private ThreadSharedContent thread_shared = new ThreadSharedContent();
    public static boolean sync_flag = false;
    public static boolean dialog_moving_db = false;
    private String dialog_cur_path = null;
    private int dialog_checked_item = -1;

    protected AdapterFormularioListHistorico adapterFormularioList = null;
    private ArrayList<Formulario> arrFormulario = new ArrayList<>();

    protected AdapterFormularioHistoryRonda adapterHistoryRonda = null;
    private ArrayList<RondaElem> arrRondas = new ArrayList<>();

    public static boolean getStatusOnline()
    {
        if(self != null) {
            if (self.thread_shared != null) {
                return self.thread_shared.status_online;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    private void loadVars()
    {
        device_id = (String) Util.loadFromSP(getApplicationContext(),String.class,Cons.Device_ID);
        device_register_id = (String) Util.loadFromSP(getApplicationContext(),String.class,Cons.Device_Register_ID);
        user_id = (String) Util.loadFromSP(getApplicationContext(),String.class,Cons.User_ID);
        user_pass = (String) Util.loadFromSP(getApplicationContext(),String.class,Cons.User_Pass);
        user_email = (String) Util.loadFromSP(getApplicationContext(),String.class,Cons.User_Email);
        user_name = (String) Util.loadFromSP(getApplicationContext(),String.class,Cons.User_Name);
        current_database_location = ((String) Util.loadFromSP(getApplicationContext(),String.class,Cons.Current_Database_Location));
        current_ronda_id = ((String) Util.loadFromSP(getApplicationContext(),String.class,Cons.Current_Ronda_ID));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        self = this;
        loadVars();
        if(current_database_location == null)
        {
            selectDBLocation(true,this);
        }
        if (device_id == null || device_register_id == null) {
            ((findViewById(R.id.mainElem))).setVisibility(View.GONE);
            ((findViewById(R.id.mainPreload))).setVisibility(View.VISIBLE);
            if(device_id == null)
            {
                device_id = Build.MANUFACTURER+"-"+Build.MODEL+"-"+System.currentTimeMillis();
                Util.saveToSP(getApplicationContext(),device_id,Cons.Device_ID);
            }
            Thread net = new Thread(new Runnable() {
                @Override
                public void run() {
                boolean pass = false;
                try {
                    String ans_raw = API.readWs(API.CREATE_DEVICE, user_id, user_pass
                            , "0",device_id,null);
                    if(ans_raw != null && ans_raw.length() > 0) {
                        JSONObject ans = new JSONObject(ans_raw);
                        String status = Util.getJSONStringOrNull(ans, "status");
                        device_register_id = Util.getJSONStringOrNull(ans, "device_reg_id");
                        if (status != null && status.contentEquals("ok") && device_register_id != null) {
                            Util.saveToSP(getApplicationContext(), device_register_id, Cons.Device_Register_ID);
                            pass = true;
                        }
                    }

                }
                catch (JSONException ignored){}
                if(pass)
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            startApp();
                        }
                    });
                }
                else
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),"No se puede registrar dispositivo.",Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                }
                }
            });
            net.start();
        } else {
            startApp();
        }
    }

    private void startApp()
    {
        Thread login_log = new Thread(new Runnable() {
            @Override
            public void run() {
                API.readWs(API.REGISTER_LOGIN,user_id,user_pass,device_register_id,device_id,null);
            }
        });
        login_log.start();
        setTitle("Formularios");
        ((findViewById(R.id.mainElem))).setVisibility(View.VISIBLE);
        ((findViewById(R.id.mainPreload))).setVisibility(View.GONE);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        if(navigationView.getHeaderCount() > 0)
        {
            ((TextView) navigationView.getHeaderView(0).findViewById(R.id.navUserName)).setText(user_name);
            ((TextView) navigationView.getHeaderView(0).findViewById(R.id.navUserEmail)).setText(user_email);
            ((TextView) navigationView.getHeaderView(0).findViewById(R.id.navUserDevice)).setText((
                    "ID Dispositivo: " + device_register_id));
        }
        ((TextView) findViewById(R.id.current_user)).setText(user_email);

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
        startService(new Intent(this,SyncService.class));
        if(thread_timer == null) {
            thread_timer = thread_shared.threadTimer(this);
            thread_timer.start();
        }
        if(thread_online == null)
        {
            thread_online = thread_shared.threadPing(this);
            thread_online.start();
        }
        if(current_ronda_id == null)
        {
            ((Button) findViewById(R.id.btnNewRonda)).setText(("Nueva Ronda"));
        }
        else
        {
            ((Button) findViewById(R.id.btnNewRonda)).setText(("Continuar Ronda"));
        }
        loadFormulariosAndTurnos();
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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_bd) {
            selectDBLocation(false,this);
        } else if (id == R.id.nav_perfil) {
            Intent act = new Intent(this,PerfilActivity.class);
            startActivity(act);
        } else if (id == R.id.nav_salir) {
            if(!dialog_moving_db) {
                stopService(new Intent(getApplicationContext(),SyncService.class));
                Util.saveToSP(getApplicationContext(), null, Cons.User_Email);
                Util.saveToSP(getApplicationContext(), null, Cons.User_Pass);
                Util.saveToSP(getApplicationContext(), null, Cons.User_ID);
                Util.saveToSP(getApplicationContext(), null, Cons.User_Name);
                Util.saveToSP(getApplicationContext(), null, Cons.Last_Online_Login_Succesfully);
                Intent act = new Intent(getApplicationContext(), LoginActivity.class);
                act.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(act);
                finish();
            }
            else
            {
                Toast.makeText(getApplicationContext(),"Espere Operación Mover Base de Datos.",Toast.LENGTH_SHORT).show();
            }
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void selectDBLocation(final boolean appStarted, final MainActivity act)
    {
        if(!sync_flag && !dialog_moving_db) {
            final ArrayList<String> paths = Util.getFilesDirList(getApplicationContext());
            if (paths.size() > 0) {
                dialog_cur_path = Util.getCurrentDatabaseLocation(getApplicationContext());
                dialog_checked_item = -1;
                final AlertDialog.Builder adb = new AlertDialog.Builder(this);
                CharSequence items[] = new CharSequence[paths.size()];
                for (int x = 0; x < paths.size(); x++) {
                    if (dialog_cur_path != null && dialog_cur_path.contentEquals(paths.get(x))) {
                        dialog_checked_item = x;
                    }
                    StatFs stat = new StatFs(paths.get(x));
                    long blockSize;
                    long totalBlocks;
                    blockSize = stat.getBlockSizeLong();
                    totalBlocks = stat.getAvailableBlocksLong();
                    items[x] = paths.get(x).
                            replace(getApplicationContext().getPackageName(), "").
                            replace("/files", "").replace("Android/data/", "")
                            + " [" + Util.formatSize(totalBlocks * blockSize) + "]";
                }
                if (dialog_checked_item == -1) {
                    current_database_location = paths.get(0);
                    Util.saveToSP(getApplicationContext(), paths.get(0), Cons.Current_Database_Location);
                    dialog_checked_item = 0;
                }
                adb.setSingleChoiceItems(items, dialog_checked_item, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog_checked_item = which;
                    }
                });
                adb.setNegativeButton("Salir", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(appStarted) // Si no no carga la lista de formularios, ¿bug de android?
                        {
                            act.recreate();
                        }
                        dialog.dismiss();
                    }
                });
                adb.setNeutralButton("Aplicar Cambio", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(!dialog_moving_db) {
                            dialog_moving_db = true;
                            if (dialog_cur_path != null &&
                                    !dialog_cur_path.contentEquals(paths.get(dialog_checked_item))) {
                                File dbfile = new File(Util.getCurrentDatabaseLocationSafe(getApplicationContext())
                                        + Cons.Database_Name);
                                if (dbfile.exists()) {
                                    Toast.makeText(getApplicationContext(),
                                            "...Moviendo Base de Datos...", Toast.LENGTH_SHORT).show();
                                    File dbfile2 = new File(Util.getCurrentDatabaseLocationSafe(getApplicationContext())
                                            + Cons.Database_Name + "-journal");
                                    File destpath = new File(Util.getDatabaseLocationSafe(paths.get(dialog_checked_item)));
                                    File destfile = new File(Util.getDatabaseLocationSafe(paths.get(dialog_checked_item)) + Cons.Database_Name);
                                    File destfile2 = new File(Util.getDatabaseLocationSafe(paths.get(dialog_checked_item)) + Cons.Database_Name + "-journal");
                                    if (!destpath.exists()) {
                                        destpath.mkdir();
                                    }
                                    boolean pass1;
                                    boolean pass2;
                                    pass1 = Util.copyFile(dbfile, destfile);
                                    pass2 = !dbfile2.exists() || Util.copyFile(dbfile2, destfile2);
                                    if (pass1 && pass2) {
                                        dbfile.delete();
                                        if(dbfile2.exists())
                                        {
                                            dbfile2.delete();
                                        }
                                        Toast.makeText(getApplicationContext(),
                                                "¡Base de Datos Movida!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        if (pass1) {
                                            destfile.delete();
                                        }
                                        if (pass2 && dbfile2.exists()) {
                                            destfile2.delete();
                                        }
                                        Toast.makeText(getApplicationContext(),
                                                "¡Error Al Mover Base de Datos!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                            else
                            {
                                Toast.makeText(getApplicationContext(),
                                        "Base de Datos Movida.", Toast.LENGTH_SHORT).show();
                            }
                            current_database_location = paths.get(dialog_checked_item);
                            Util.saveToSP(getApplicationContext(), paths.get(dialog_checked_item), Cons.Current_Database_Location);
                            dialog_moving_db = false;
                        }
                        if(appStarted) // Si no no carga la lista de formularios, ¿bug de android?
                        {
                            act.recreate();
                        }
                        dialog.dismiss();
                    }
                });
                if(dialog_cur_path != null) {
                    File dbfile = new File(Util.getDatabaseLocationSafe(dialog_cur_path) + Cons.Database_Name);
                    if(dbfile.exists()) {
                        adb.setTitle("Seleccione Ubicación de Base de Datos. [Tamaño: " +
                                Util.formatSize(dbfile.length()) + "]");
                    }
                    else {
                        adb.setTitle("Seleccione Ubicación de Base de Datos.");
                    }
                }
                else {
                    adb.setTitle("Seleccione Ubicación de Base de Datos.");
                }
                adb.show();
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(),
                    "No Se Puede Cambiar La Ubicación De La Base De Datos Si Hay Operaciones Pendientes.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public static void SyncMessage(final String msg)
    {
        last_msg = msg;
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

    private void loadRondas()
    {
        if(adapterHistoryRonda == null) {
            adapterHistoryRonda = new AdapterFormularioHistoryRonda(getApplicationContext(), arrRondas);
            ((RecyclerView) findViewById(R.id.lastRondasListView)).setNestedScrollingEnabled(false);
            ((RecyclerView) findViewById(R.id.lastRondasListView)).setAdapter(adapterHistoryRonda);
            ((RecyclerView) findViewById(R.id.lastRondasListView)).setLayoutManager(
                    new LinearLayoutManager(this));
        }
        DBHelper db = new DBHelper(this);
        arrRondas.clear();
        arrRondas.addAll(db.getLast5Rondas(this));
        db.close();
        if(arrRondas.size() > 0)
        {
            findViewById(R.id.headerRondas).setVisibility(View.VISIBLE);
        }
        else
        {
            findViewById(R.id.headerRondas).setVisibility(View.GONE);
        }
        adapterHistoryRonda.notifyDataSetChanged();
    }

    private void loadFormulariosAndTurnos()
    {
        findViewById(R.id.mainFormPreload).setVisibility(View.VISIBLE);
        findViewById(R.id.mainFormDataLayout).setVisibility(View.GONE);
        Thread net = new Thread(new Runnable()
        {
            @Override
            public void run() {
                try {
                    if (adapterFormularioList != null) {
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
                            if(ans.has("turno_vars"))
                            {
                                String turno_1_i_h = null;
                                String turno_1_i_m = null;
                                String turno_1_f_h = null;
                                String turno_1_f_m = null;
                                String turno_2_i_h = null;
                                String turno_2_i_m = null;
                                String turno_2_f_h = null;
                                String turno_2_f_m = null;
                                String turno_2_day_overlap = null;
                                try {
                                    JSONArray t_vars = ans.getJSONArray("turno_vars");
                                    for (int x = 0; x < t_vars.length(); x++) {
                                        if (t_vars.getJSONObject(x).has("id") &&
                                                t_vars.getJSONObject(x).has("valor")) {
                                            String id = t_vars.getJSONObject(x).getString("id");
                                            String valor = t_vars.getJSONObject(x).getString("valor");
                                            if (id.contentEquals("turno_1_i_h")) {
                                                turno_1_i_h = valor;
                                            } else if (id.contentEquals("turno_1_i_m")) {
                                                turno_1_i_m = valor;
                                            } else if (id.contentEquals("turno_1_f_h")) {
                                                turno_1_f_h = valor;
                                            } else if (id.contentEquals("turno_1_f_m")) {
                                                turno_1_f_m = valor;
                                            } else if (id.contentEquals("turno_2_i_h")) {
                                                turno_2_i_h = valor;
                                            } else if (id.contentEquals("turno_2_i_m")) {
                                                turno_2_i_m = valor;
                                            } else if (id.contentEquals("turno_2_f_h")) {
                                                turno_2_f_h = valor;
                                            } else if (id.contentEquals("turno_2_f_m")) {
                                                turno_2_f_m = valor;
                                            } else if (id.contentEquals("turno_2_day_overlap")) {
                                                turno_2_day_overlap = valor;
                                            }
                                        }
                                    }
                                }
                                catch (JSONException ignored){}
                                if(turno_1_i_h == null){turno_1_i_h = "8";}
                                if(turno_1_i_m == null){turno_1_i_m = "0";}
                                if(turno_1_f_h == null){turno_1_f_h = "19";}
                                if(turno_1_f_m == null){turno_1_f_m = "59";}
                                if(turno_2_i_h == null){turno_2_i_h = "20";}
                                if(turno_2_i_m == null){turno_2_i_m = "0";}
                                if(turno_2_f_h == null){turno_2_f_h = "7";}
                                if(turno_2_f_m == null){turno_2_f_m = "59";}
                                if(turno_2_day_overlap == null){turno_2_day_overlap = "true";}
                                try {
                                    Util.saveToSP(getApplicationContext(),
                                            Util.leftZeros(Integer.parseInt(turno_1_i_h), 2) + ":" +
                                            Util.leftZeros(Integer.parseInt(turno_1_i_m), 2) + ":00",
                                            Cons.Turno1_Inicio);
                                    Util.saveToSP(getApplicationContext(),
                                            Util.leftZeros(Integer.parseInt(turno_1_f_h), 2) + ":" +
                                            Util.leftZeros(Integer.parseInt(turno_1_f_m), 2) + ":59",
                                            Cons.Turno1_Fin);
                                    Util.saveToSP(getApplicationContext(),
                                            Util.leftZeros(Integer.parseInt(turno_2_i_h), 2) + ":" +
                                            Util.leftZeros(Integer.parseInt(turno_2_i_m), 2) + ":00",
                                            Cons.Turno2_Inicio);
                                    Util.saveToSP(getApplicationContext(),
                                            Util.leftZeros(Integer.parseInt(turno_2_f_h), 2) + ":" +
                                            Util.leftZeros(Integer.parseInt(turno_2_f_m), 2) + ":59",
                                            Cons.Turno2_Fin);
                                    Util.saveToSP(getApplicationContext(),turno_2_day_overlap,Cons.Turno2_Overlap);
                                }
                                catch (NumberFormatException ignored){}
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
                                if(adapterFormularioList == null) {
                                    adapterFormularioList = new AdapterFormularioListHistorico(getApplicationContext(), arrFormulario);
                                    ((RecyclerView) findViewById(R.id.mainFormListView)).setNestedScrollingEnabled(false);
                                    ((RecyclerView) findViewById(R.id.mainFormListView)).setAdapter(adapterFormularioList);
                                    ((RecyclerView) findViewById(R.id.mainFormListView)).setLayoutManager(
                                            new GridLayoutManager(self,2));
                                }
                                adapterFormularioList.notifyDataSetChanged();
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
                            findViewById(R.id.mainFormDataLayout).setVisibility(View.VISIBLE);
                            loadRondas();
                        }
                        catch (NullPointerException e){e.printStackTrace();}
                    }
                });
            }
        });
        net.start();
    }

    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.btnNewRonda:
            {
                Intent i = new Intent(this,RondaSelectFormularioActivity.class);
                i.putExtra("last_msg",last_msg);
                startActivity(i);
            }
            break;
            case R.id.btnNewRegistroAislado:
            {
                Intent i = new Intent(this, IngresoAisladoSelectFormularioActivity.class);
                i.putExtra("last_msg",last_msg);
                startActivity(i);
            }
            break;
        }
    }
}
