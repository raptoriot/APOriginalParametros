package vesat.bsa.registromaquinariagc.bsaregistromaquinaria.obj;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib.Util;

public class FormSection {

    public String name;
    public ArrayList<FormField> fields;

    public TextView view_name;
    public View view_top_space;
    public View view_bottom_space;

    public void loadFromJSON(JSONObject jsonObject)
    {
        name = Util.getJSONStringOrNull(jsonObject,"name");
        fields = new ArrayList<>();
        try {
            JSONArray jr_fields = jsonObject.getJSONArray("fields");
            for(int x = 0;x < jr_fields.length();x++)
            {
                FormField field = new FormField();
                field.loadFromJSON(jr_fields.getJSONObject(x));
                fields.add(field);
            }
        }
        catch (JSONException e){}
    }

    public void createView(Context context)
    {
        //this.section = section;
        view_name = new TextView(context);
        view_top_space = new View(context);
        view_bottom_space = new View(context);
        view_name.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        view_top_space.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                12));
        view_bottom_space.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                10));
        view_name.setText(name);
        view_name.setTypeface(view_name.getTypeface(),Typeface.BOLD);
        view_name.setTextSize(TypedValue.COMPLEX_UNIT_SP,14);
        //view_bottom_space.setBackgroundColor(Color.parseColor("#dddddd"));
    }
}
