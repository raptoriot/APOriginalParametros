package vesat.bsa.registromaquinariagc.bsaregistromaquinaria.activity;

import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

public class FormularioViewActivity extends AppCompatActivity {

    private Formulario current_form = null;
    private Integer current_form_elem = null;
    private ArrayList<FormSection> sections = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario_view);
        current_form = (Formulario) Util.loadFromSP(this,Formulario.class, Cons.Current_Form);
        current_form_elem = (Integer) Util.loadFromSP(this,Integer.class, Cons.Current_FormElem);
        if(current_form != null && current_form_elem != null && current_form_elem > 0)
        {
            loadFormulario();
        }
        else
        {
            Toast.makeText(this,"Formulario y/o Datos Invalidos",Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadFormulario()
    {
        LinearLayout variableContent = findViewById(R.id.variableContentLayout);
        findViewById(R.id.auxProgressBar).setVisibility(View.VISIBLE);
        findViewById(R.id.variableContentLayout).setVisibility(View.GONE);
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
                    field.createView(this,false);
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
        loadFormularioData();
        findViewById(R.id.auxProgressBar).setVisibility(View.GONE);
        findViewById(R.id.variableContentLayout).setVisibility(View.VISIBLE);
    }

    private void loadFormularioData()
    {
        DBHelper db = new DBHelper(this);
        Cursor cur = db.getRegistroById(current_form_elem);
        if(cur.moveToNext())
        {
            try {
                JSONArray datos = new JSONArray(
                        new String(Base64.decode(cur.getString(cur.getColumnIndex("datos")),
                                Base64.NO_WRAP | Base64.URL_SAFE | Base64.NO_PADDING)));
                for(FormSection section : sections) {
                    for(FormField field : section.fields)
                    {
                        for(int x = 0;x < datos.length();x++)
                        {
                            if(datos.getJSONObject(x).has("id") && datos.getJSONObject(x).getString("id").
                                    trim().equalsIgnoreCase(field.id.trim()))
                            {
                                if(datos.getJSONObject(x).has("value")
                                    && datos.getJSONObject(x).has("type")) {
                                    String value = datos.getJSONObject(x).getString("value");
                                    String type = datos.getJSONObject(x).getString("type");
                                    if(type.equalsIgnoreCase("Label") || type.equalsIgnoreCase("Image"))
                                    {
                                        // No Hacer Nada
                                    }
                                    else if(type.equalsIgnoreCase("Photo") || type.equalsIgnoreCase("Signature"))
                                    {
                                        Util.loadBase64Img(value,(ImageView) field.view_value,this);
                                    }
                                    else if(type.equalsIgnoreCase("Boolean"))
                                    {
                                        ((CheckBox) field.view_value).setChecked(value.equalsIgnoreCase("1"));
                                    }
                                    else if(type.equalsIgnoreCase("Combo"))
                                    {
                                        ((TextView) field.view_value).setText(value);
                                    }
                                    else
                                    {
                                        ((EditText) field.view_value).setText(value);
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }
            catch (JSONException ignored){}
        }
        db.close();
    }
}
