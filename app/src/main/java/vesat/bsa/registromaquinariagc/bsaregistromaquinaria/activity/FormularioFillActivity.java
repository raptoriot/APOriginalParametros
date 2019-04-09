package vesat.bsa.registromaquinariagc.bsaregistromaquinaria.activity;

import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.R;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib.Cons;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib.DBHelper;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib.Util;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.obj.FormField;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.obj.FormSection;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.obj.Formulario;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.obj.WidthHeight;

public class FormularioFillActivity extends AppCompatActivity {

    private String fecha;
    private Formulario current_form = null;
    private String current_ronda_id = null;
    private ArrayList<FormSection> sections = new ArrayList<>();

    private boolean saveLock = false;
    private int nivelEnvioAlerta = 0;
    private String alert_body = "";

    private Double lastLatitud = null;
    private Double lastLongitud = null;
    private FusedLocationProviderClient googleClient = null;
    protected LocationCallback googleLocationCallback = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario_fill);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");
        fecha = Util.getFechaFullActual();
        ((EditText) findViewById(R.id.currentHoraRegistro)).setText(fecha);
        current_ronda_id = (String) Util.loadFromSP(this,String.class,Cons.Current_Ronda_ID);
        current_form = (Formulario) Util.loadFromSP(this,Formulario.class,Cons.Current_Form);
        if(current_form != null)
        {
            googleClient = LocationServices.getFusedLocationProviderClient(this);
            loadFormulario();
        }
        else
        {
            Toast.makeText(this,"Formulario Invalido",Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadFormulario()
    {
        LinearLayout variableContent = findViewById(R.id.variableContentLayout);
        findViewById(R.id.auxProgressBar).setVisibility(View.VISIBLE);
        findViewById(R.id.formularioTarea).setVisibility(View.GONE);
        findViewById(R.id.navigationMenu).setVisibility(View.GONE);
        setTitle(current_form.nombre);
        try {
            JSONObject form = new JSONObject(current_form.definicion);
            JSONArray j_sections = form.getJSONArray("section");
            for(int x = 0;x < j_sections.length();x++)
            {
                FormSection section = new FormSection();
                section.loadFromJSON(j_sections.getJSONObject(x));
                sections.add(section);
            }
            for(FormSection section : sections) {
                section.createView(this);
                variableContent.addView(section.view_top_space);
                variableContent.addView(section.view_name);
                for(FormField field : section.fields)
                {
                    field.createView(this,true);
                    if(field.view_value != null) {
                        variableContent.addView(field.view_name);
                        variableContent.addView(field.view_value);
                    }

                }
                variableContent.addView(section.view_bottom_space);
            }
        }
        catch (JSONException e)
        {
            Toast.makeText(this,"Formulario Invalido",Toast.LENGTH_SHORT).show();
            finish();
        }
        findViewById(R.id.auxProgressBar).setVisibility(View.GONE);
        findViewById(R.id.formularioTarea).setVisibility(View.VISIBLE);
        findViewById(R.id.navigationMenu).setVisibility(View.VISIBLE);
    }

    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.btnAlerta: case R.id.txtbtnAlerta:
            {
                CharSequence[] options_cs = {"No Enviar Alerta",
                        "Nivel Emergencia Bajo","Nivel Emergencia Medio","Nivel Emergencia Alto"};
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Seleccione Alerta");
                builder.setItems(options_cs, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        nivelEnvioAlerta = which;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                switch (nivelEnvioAlerta)
                                {
                                    case 1: ((ImageView) findViewById(R.id.btnAlerta)).
                                            setImageDrawable(getResources().getDrawable(R.drawable.amarillo));
                                    break;
                                    case 2: ((ImageView) findViewById(R.id.btnAlerta)).
                                            setImageDrawable(getResources().getDrawable(R.drawable.naranjo));
                                    break;
                                    case 3: ((ImageView) findViewById(R.id.btnAlerta)).
                                            setImageDrawable(getResources().getDrawable(R.drawable.rojo));
                                    break;
                                    default: ((ImageView) findViewById(R.id.btnAlerta)).
                                        setImageDrawable(getResources().getDrawable(R.drawable.gris));
                                    break;
                                }
                            }
                        });
                    }
                });
                builder.show();
            }
            break;
        }
    }

    public void onResume() {
        super.onResume();
        if(current_form != null) {
            if (googleLocationCallback == null) {
                googleLocationCallback = new LocationCallback() {
                    @Override
                    public void onLocationAvailability(LocationAvailability locationAvailability) {

                    }

                    @Override
                    public void onLocationResult(LocationResult result) {
                        Location lastlocation = result.getLastLocation();
                        if (lastlocation != null) {
                            lastLatitud = lastlocation.getLatitude();
                            lastLongitud = lastlocation.getLongitude();
                        }
                    }
                };
            }
            if (googleClient != null) {
                try {
                    LocationRequest locquest = LocationRequest.create();
                    locquest.setFastestInterval(3000);
                    googleClient.requestLocationUpdates(locquest, googleLocationCallback, Looper.myLooper());
                } catch (SecurityException ignored){}
            }
            String PictureLoad = (String) Util.loadFromSP(getApplicationContext(), String.class, Cons.CAMERA_PictureLoad);
            if (PictureLoad != null && PictureLoad.equalsIgnoreCase("true")) {
                Boolean pictureTaked = (Boolean)
                        Util.loadFromSP(getApplicationContext(), Boolean.class, Cons.CAMERA_PictureTaked);
                String pictureLoadID = (String) Util.loadFromSP(getApplicationContext(), String.class, Cons.CAMERA_PictureLoadID);
                if (pictureTaked != null && pictureTaked && pictureLoadID != null) {
                    for(FormSection section : sections) {
                        for (FormField field : section.fields) {
                            if (field.id.equalsIgnoreCase(pictureLoadID)) {
                                if ((field.type.equalsIgnoreCase("Photo") ||
                                        field.type.equalsIgnoreCase("Signature"))) {
                                    ImageView imgv = (ImageView) field.view_value;
                                    String imgbytes = (String) Util.loadFromSP(this, String.class,
                                            Cons.CAMERA_FormFieldViewHolderPrefix + field.id);
                                    if (imgbytes != null && imgbytes.length() > 0) {
                                        WidthHeight w_h = Util.loadBase64Img(imgbytes, imgv, this);
                                        View parentView = (View) imgv.getParent();
                                        if (w_h != null && w_h.width > 0) {
                                            int cur_width = Util.safeGetMatchedScreenSizeLayoutWidth(parentView, this, 40);
                                            if (cur_width > 0) {
                                                float factor = ((float) cur_width) / ((float) w_h.width);
                                                int new_height = (int) (w_h.height * factor);
                                                LinearLayout.LayoutParams params2 =
                                                        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                                new_height);
                                                params2.setMargins(0, 0, 0, 16);
                                                imgv.setLayoutParams(params2);
                                            }
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
                Util.saveToSP(getApplicationContext(), "false", Cons.CAMERA_PictureLoad);
                Util.saveToSP(getApplicationContext(), "-1", Cons.CAMERA_PictureLoadID);
            }
        }
    }

    public void onPause()
    {
        if(googleClient != null)
        {
            googleClient.removeLocationUpdates(googleLocationCallback);
            googleLocationCallback = null;
        }
        super.onPause();
    }

    public void onBackPressed()
    {
        if(!saveLock)
        {
            super.onBackPressed();
        }
        else
        {
            Toast.makeText(this,"...Guardando...",Toast.LENGTH_SHORT).show();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_canvas_act_menu, menu);
        MenuItem saveItem = menu.findItem(R.id.menu_save);
        if (saveItem != null) {
            saveItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if(!saveLock)
                    {
                        guardar();
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),"...Guardando...",Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });
        }
        MenuItem delItem = menu.findItem(R.id.menu_delete);
        if (delItem != null){
            delItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if(!saveLock)
                    {
                        finish();
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),"...Guardando...",Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });
        }
        return super.onCreateOptionsMenu(menu);
    }

    private void guardar()
    {
        saveLock = true;
        Toast.makeText(this,"Guardando",Toast.LENGTH_SHORT).show();
        findViewById(R.id.auxProgressBar).setVisibility(View.VISIBLE);
        findViewById(R.id.formularioTarea).setVisibility(View.GONE);
        findViewById(R.id.navigationMenu).setVisibility(View.GONE);
        Thread net = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean pass = false;
                boolean requireds = true;
                try {
                    String s_usuarios = (String) Util.loadFromSP(getApplicationContext(),String.class,Cons.User_ID);
                    if(s_usuarios != null) {
                        int formularios = Integer.parseInt(current_form.id);
                        int usuarios = Integer.parseInt(s_usuarios);
                        int alerta_nivel = nivelEnvioAlerta;
                        Double latitud = lastLatitud;
                        Double longitud = lastLongitud;
                        JSONArray datos = new JSONArray();
                        String current_user = ((String)
                                Util.loadFromSP(getApplicationContext(), String.class, Cons.User_Name)) + " [" +
                                ((String) Util.loadFromSP(getApplicationContext(), String.class, Cons.User_Email)) + "]";
                        alert_body = "Enviado Por: " + current_user + "\n";
                        alert_body += "Formulario: " + current_form.nombre + " [" + current_form.id + "]\n\nValores:\n";
                        check_data:
                        for (FormSection section : sections) {
                            for (FormField field : section.fields) {
                                if ((field.type.equalsIgnoreCase("Photo") ||
                                        field.type.equalsIgnoreCase("Signature"))) {
                                    String picture_bytes = (String) Util.loadFromSP(getApplicationContext()
                                            , String.class, Cons.CAMERA_FormFieldViewHolderPrefix + field.id);
                                    if (picture_bytes != null && picture_bytes.length() > 0) {
                                        JSONObject obj = new JSONObject();
                                        obj.put("id", field.id);
                                        obj.put("type", field.type);
                                        obj.put("title", field.title);
                                        obj.put("value", picture_bytes);
                                        datos.put(obj);
                                    } else if (field.required.equalsIgnoreCase("true")) {
                                        requireds = false;
                                        break check_data;
                                    }
                                } else if (field.type.equalsIgnoreCase("Combo")) {
                                    if (field.view_aux != null && field.view_aux.length() > 0 &&
                                            !field.view_aux.contentEquals(Cons.Combo_Not_Selected)) {
                                        JSONObject obj = new JSONObject();
                                        obj.put("id", field.id);
                                        obj.put("type", field.type);
                                        obj.put("title", field.title);
                                        obj.put("value", field.view_aux);
                                        datos.put(obj);
                                        alert_body += field.title + ": " + field.view_aux + "\n";
                                    } else if (field.required.equalsIgnoreCase("true")) {
                                        requireds = false;
                                        break check_data;
                                    }
                                } else if (field.type.equalsIgnoreCase("Boolean")) {
                                    if (field.view_aux != null && field.view_aux.length() > 0) {
                                        JSONObject obj = new JSONObject();
                                        obj.put("id", field.id);
                                        obj.put("type", field.type);
                                        obj.put("title", field.title);
                                        obj.put("value", field.view_aux);
                                        datos.put(obj);
                                        alert_body += field.title + ": " +
                                                (field.view_aux.equalsIgnoreCase("1") ? "Si" : "No") + "\n";
                                    } else if (field.required.equalsIgnoreCase("true")) {
                                        requireds = false;
                                        break check_data;
                                    }
                                } else if (field.type.equalsIgnoreCase("Label") ||
                                        field.type.equalsIgnoreCase("Image")) {
                                    // No hacer nada
                                } else /* Text, Textarea, Number, Fallback */ {
                                    String value_edit_text = ((EditText) field.view_value).getText().toString().trim();
                                    if (value_edit_text.length() > 0) {
                                        JSONObject obj = new JSONObject();
                                        obj.put("id", field.id);
                                        obj.put("type", field.type);
                                        obj.put("title", field.title);
                                        obj.put("value", value_edit_text);
                                        datos.put(obj);
                                        alert_body += field.title + ": " + value_edit_text + "\n";
                                    } else if (field.required.equalsIgnoreCase("true")) {
                                        requireds = false;
                                        break check_data;
                                    }
                                }
                            }
                        }
                        if (requireds) {
                            try {
                                DBHelper db = new DBHelper(getApplicationContext());
                                if (db.addNewRegistro(formularios, usuarios, fecha,
                                        Base64.encodeToString(datos.toString().getBytes(), Base64.NO_WRAP | Base64.URL_SAFE | Base64.NO_PADDING)
                                        , alerta_nivel, latitud, longitud, current_ronda_id) > 0) {
                                    pass = true;
                                }
                                db.close();
                            }
                            catch (Exception e)
                            {
                                Util.serverLogException(e,getApplicationContext());
                            }
                        } else {
                            pass = true;
                        }
                    }
                }
                catch (NullPointerException | NumberFormatException | JSONException ignored){}
                final boolean hpass = pass;
                final boolean hrequireds = requireds;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(hrequireds) {
                            if (hpass) {
                                Toast.makeText(getApplicationContext(), "Guardado Correctamente", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "Error Al Guardar", Toast.LENGTH_SHORT).show();
                            }
                            saveLock = false;
                            findViewById(R.id.auxProgressBar).setVisibility(View.GONE);
                            findViewById(R.id.formularioTarea).setVisibility(View.VISIBLE);
                            findViewById(R.id.navigationMenu).setVisibility(View.VISIBLE);
                            finish();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "Debe Llenar Todos Los Campos Obligatorios (*)", Toast.LENGTH_SHORT).show();
                            saveLock = false;
                            findViewById(R.id.auxProgressBar).setVisibility(View.GONE);
                            findViewById(R.id.formularioTarea).setVisibility(View.VISIBLE);
                            findViewById(R.id.navigationMenu).setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        });
        net.start();
    }
}
