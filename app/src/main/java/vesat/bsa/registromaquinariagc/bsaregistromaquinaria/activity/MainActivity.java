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
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.R;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib.API;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib.Cons;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib.SyncService;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib.Util;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private String device_id;
    private String device_register_id;
    private String user_id;
    private String user_pass;
    private String user_email;
    private String user_name;

    private Thread thread_timer = null;
    protected static boolean sync_flag = false;
    protected static boolean dialog_moving_db = false;
    private String dialog_cur_path = null;
    private int dialog_checked_item = -1;

    private void loadVars()
    {
        device_id = (String) Util.loadFromSP(getApplicationContext(),String.class,Cons.Device_ID);
        device_register_id = (String) Util.loadFromSP(getApplicationContext(),String.class,Cons.Device_Register_ID);
        user_id = (String) Util.loadFromSP(getApplicationContext(),String.class,Cons.User_ID);
        user_pass = (String) Util.loadFromSP(getApplicationContext(),String.class,Cons.User_Pass);
        user_email = (String) Util.loadFromSP(getApplicationContext(),String.class,Cons.User_Email);
        user_name = (String) Util.loadFromSP(getApplicationContext(),String.class,Cons.User_Name);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadVars();
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
        }
        ((TextView) findViewById(R.id.current_user)).setText(user_email);
        String current_database_location = ((String) Util.loadFromSP(
                getApplicationContext(),String.class,Cons.Current_Database_Location));
        if(current_database_location == null)
        {
            selectDBLocation();
        }
    }

    protected void onResume()
    {
        super.onResume();
        loadVars();
        thread_timer = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true)
                {
                    try {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((TextView) findViewById(R.id.tag_hora)).setText(Util.getHoraActual());
                            }
                        });
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException ignored){}
                }
            }
        });
        thread_timer.start();
    }

    protected void onPause()
    {
        if(thread_timer != null)
        {
            thread_timer.interrupt();
            thread_timer = null;
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
            selectDBLocation();
        } else if (id == R.id.nav_alert) {
            Intent act = new Intent(this,AlertaConfigActivity.class);
            startActivity(act);
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

    private void selectDBLocation()
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
                        dialog.dismiss();
                    }
                });
                adb.setNeutralButton("Aplicar Cambio", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
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
                            Util.saveToSP(getApplicationContext(), paths.get(dialog_checked_item), Cons.Current_Database_Location);
                            dialog_moving_db = false;
                        }
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
}