package vesat.bsa.registromaquinariagc.bsaregistromaquinaria.activity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
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

public class FormularioHistoryActivity extends AppCompatActivity {

    private Formulario current_form = null;
    private ArrayList<FormSection> sections = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario_history);
        current_form = (Formulario) Util.loadFromSP(this,Formulario.class, Cons.Current_Form);
        if(current_form != null)
        {
            ((TextView) findViewById(R.id.formularioTitle)).setText(("Historico Local: " + current_form.nombre));
            loadHistory();
        }
        else
        {
            Toast.makeText(this,"Formulario Invalido",Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadHistory()
    {
        DBHelper db = new DBHelper(this);
        try {
            String[] primary_fields = current_form.primary_fields.trim().split(",");
            JSONObject form = new JSONObject(current_form.definicion);
            JSONArray j_sections = form.getJSONArray("section");
            for(int x = 0;x < j_sections.length();x++)
            {
                FormSection section = new FormSection();
                section.loadFromJSON(j_sections.getJSONObject(x));
                sections.add(section);
            }
            TableLayout table = findViewById(R.id.tableLayout);
            TableRow header = new TableRow(this);
            header.setBackgroundColor(Color.parseColor("#dddddd"));
            header.setPadding(1,1,1,1);
            header.setGravity(Gravity.CENTER_HORIZONTAL);
            TextView header_id = new TextView(this);
            header_id.setTypeface(null,Typeface.BOLD);
            header_id.setPadding(1,1,1,1);
            header_id.setBackgroundColor(Color.parseColor("#ffffff"));
            header_id.setText(("   ID   "));
            header_id.setGravity(Gravity.CENTER_HORIZONTAL);
            TextView header_fecha = new TextView(this);
            header_fecha.setTypeface(null,Typeface.BOLD);
            header_fecha.setPadding(1,1,1,1);
            header_fecha.setText(("   Fecha   "));
            header_fecha.setBackgroundColor(Color.parseColor("#ffffff"));
            header_fecha.setGravity(Gravity.CENTER_HORIZONTAL);
            TextView header_alerta = new TextView(this);
            header_alerta.setTypeface(null,Typeface.BOLD);
            header_alerta.setPadding(1,1,1,1);
            header_alerta.setText(("   Alerta   "));
            header_alerta.setBackgroundColor(Color.parseColor("#ffffff"));
            header_alerta.setGravity(Gravity.CENTER_HORIZONTAL);
            TextView h_button_detail = new TextView(this);
            h_button_detail.setTypeface(null,Typeface.BOLD);
            h_button_detail.setPadding(1,1,1,1);
            h_button_detail.setText(("   Detalle   "));
            h_button_detail.setBackgroundColor(Color.parseColor("#ffffff"));
            h_button_detail.setGravity(Gravity.CENTER_HORIZONTAL);
            header.addView(header_id);
            header.addView(header_fecha);
            header.addView(header_alerta);
            for(String p_field : primary_fields)
            {
                keep_loop: for(FormSection section : sections) {
                    for(FormField field : section.fields)
                    {
                        if(field.id.trim().contentEquals(p_field.trim()))
                        {
                            TextView p_header = new TextView(this);
                            p_header.setTypeface(null,Typeface.BOLD);
                            p_header.setPadding(1,1,1,1);
                            p_header.setText(("   " + field.title + " "));
                            p_header.setBackgroundColor(Color.parseColor("#ffffff"));
                            p_header.setGravity(Gravity.CENTER_HORIZONTAL);
                            header.addView(p_header);
                            break keep_loop;
                        }
                    }
                }
            }
            header.addView(h_button_detail);
            table.addView(header);
            Cursor cur = db.getAllRegistrosOfFormulario(Integer.parseInt(current_form.id));
            while (cur.moveToNext()) {
                JSONArray datos = new JSONArray(
                        new String(Base64.decode(cur.getString(cur.getColumnIndex("datos")),
                                Base64.NO_WRAP | Base64.URL_SAFE | Base64.NO_PADDING)));
                TableRow t_header = new TableRow(this);
                t_header.setBackgroundColor(Color.parseColor("#dddddd"));
                t_header.setPadding(1,1,1,1);
                t_header.setGravity(Gravity.CENTER_HORIZONTAL);
                TextView t_header_id = new TextView(this);
                t_header_id.setTypeface(null,Typeface.BOLD);
                t_header_id.setPadding(1,1,1,1);
                t_header_id.setBackgroundColor(Color.parseColor("#ffffff"));
                t_header_id.setText((" " + cur.getString(cur.getColumnIndex("_ID"))+ " "));
                t_header_id.setGravity(Gravity.CENTER_HORIZONTAL);
                TextView t_header_fecha = new TextView(this);
                t_header_fecha.setTypeface(null,Typeface.BOLD);
                t_header_fecha.setPadding(1,1,1,1);
                t_header_fecha.setText((" " + cur.getString(cur.getColumnIndex("fecha")) + " "));
                t_header_fecha.setBackgroundColor(Color.parseColor("#ffffff"));
                t_header_fecha.setGravity(Gravity.CENTER_HORIZONTAL);
                ImageView t_header_alerta = new ImageView(this);
                t_header_alerta.setBackgroundColor(Color.parseColor("#ffffff"));
                switch (cur.getInt(cur.getColumnIndex("alerta_nivel")))
                {
                    case 1: t_header_alerta.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.amarillo_2)); break;
                    case 2: t_header_alerta.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.naranjo_2)); break;
                    case 3: t_header_alerta.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.rojo_2)); break;
                    default: t_header_alerta.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.gris_2)); break;
                }
                ImageView button_detail = new ImageView(this);
                button_detail.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_detail_check));
                button_detail.setBackgroundColor(Color.parseColor("#ffffff"));
                final Integer cur_id = cur.getInt(cur.getColumnIndex("_ID"));
                button_detail.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v) {
                        Util.saveToSP(getApplicationContext(),cur_id,Cons.Current_FormElem);
                        Intent i = new Intent(getApplicationContext(),FormularioViewActivity.class);
                        startActivity(i);
                    }
                });
                t_header.addView(t_header_id);
                t_header.addView(t_header_fecha);
                t_header.addView(t_header_alerta);
                for(String p_field : primary_fields)
                {
                    boolean found = false;
                    for(int x = 0;x < datos.length();x++)
                    {
                        if(datos.getJSONObject(x).has("id") && datos.getJSONObject(x).getString("id").
                                trim().contentEquals(p_field.trim())
                                && datos.getJSONObject(x).has("value")
                                && datos.getJSONObject(x).has("type"))
                        {
                            found = true;
                            String value = datos.getJSONObject(x).getString("value");
                            String type = datos.getJSONObject(x).getString("type");
                            if(type.contentEquals("Photo") || type.contentEquals("Signature"))
                            {
                                ImageView t_view = new ImageView(this);
                                t_view.setPadding(1,1,1,1);
                                Util.loadBase64Img(value,t_view,this);
                                t_header.addView(t_view);
                            }
                            else if(type.contentEquals("Boolean"))
                            {
                                TextView t_view = new TextView(this);
                                t_view.setTypeface(null,Typeface.BOLD);
                                t_view.setPadding(1,1,1,1);
                                t_view.setBackgroundColor(Color.parseColor("#ffffff"));
                                t_view.setText(value.contentEquals("1") ? "  Si  " : "  No  ");
                                t_view.setGravity(Gravity.CENTER_HORIZONTAL);
                                t_header.addView(t_view);
                            }
                            else
                            {
                                TextView t_view = new TextView(this);
                                t_view.setTypeface(null,Typeface.BOLD);
                                t_view.setPadding(1,1,1,1);
                                t_view.setBackgroundColor(Color.parseColor("#ffffff"));
                                if(value.length() > 0) {
                                    t_view.setText(("   " + value + "   "));
                                }
                                else
                                {
                                    t_view.setText("   ---   ");
                                }
                                t_view.setGravity(Gravity.CENTER_HORIZONTAL);
                                t_header.addView(t_view);
                            }
                            break;
                        }
                    }
                    if(!found)
                    {
                        TextView t_view = new TextView(this);
                        t_view.setTypeface(null,Typeface.BOLD);
                        t_view.setPadding(1,1,1,1);
                        t_view.setBackgroundColor(Color.parseColor("#ffffff"));
                        t_view.setText("   ---   ");
                        t_view.setGravity(Gravity.CENTER_HORIZONTAL);
                        t_header.addView(t_view);
                    }
                }
                t_header.addView(button_detail);
                table.addView(t_header);
            }
            cur.close();
        }
        catch (NumberFormatException | JSONException ignored){ignored.printStackTrace();}
        db.close();
    }

}
