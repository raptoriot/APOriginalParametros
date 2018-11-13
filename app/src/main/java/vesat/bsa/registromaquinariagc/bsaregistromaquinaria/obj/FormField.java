package vesat.bsa.registromaquinariagc.bsaregistromaquinaria.obj;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.R;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.activity.ActivityCamera;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.activity.ActivityCanvas;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib.Cons;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib.CustomDateTimePicker;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib.DownloadImageTask;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib.Util;

public class FormField {
    public String id;
    public String type;
    public String title;
    public String defaultvalue;
    public String placeholder;
    public ArrayList<String> options;
    public String required;

    public TextView view_name;
    public View value_view;
    public String value_aux;

    public void loadFromJSON(JSONObject jsonObject)
    {
        id = Util.getJSONStringOrNull(jsonObject,"id");
        type = Util.getJSONStringOrNull(jsonObject,"type");
        title = Util.getJSONStringOrNull(jsonObject,"title");
        defaultvalue = Util.getJSONStringOrNull(jsonObject,"defaultvalue");
        placeholder = Util.getJSONStringOrNull(jsonObject,"placeholder");
        required = Util.getJSONStringOrNull(jsonObject,"required");
        options = new ArrayList<>();
        try {
            if(!jsonObject.isNull("options")) {
                JSONArray jr_field_options = jsonObject.getJSONArray("options");
                if (jr_field_options.length() > 0) {
                    options.add(null);
                }
                for (int x = 0; x < jr_field_options.length(); x++) {
                    JSONObject option = !jr_field_options.isNull(x) ? jr_field_options.getJSONObject(x) : null;
                    if (option != null) {
                        options.add(jr_field_options.getString(x));
                    }
                }
            }
        }
        catch (JSONException e){}
    }

    public void createView(final Context context)
    {
        view_name = new TextView(context);
        view_name.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        if(required.equalsIgnoreCase("true")) {
            view_name.setText((title + " *"));
        }
        else
        {
            view_name.setText(title);
        }
        view_name.setTypeface(view_name.getTypeface(), Typeface.ITALIC);
        view_name.setTextSize(TypedValue.COMPLEX_UNIT_SP,13);
        value_view = null;
        if(type != null)
        {
            if(type.equalsIgnoreCase("Number"))
            {
                value_view = new EditText(context);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0,0,0,16);
                value_view.setLayoutParams(params);
                ((EditText) value_view).setHint(placeholder);
                ((EditText) value_view).setTextSize(TypedValue.COMPLEX_UNIT_SP,13);
                ((EditText) value_view).setInputType(InputType.TYPE_CLASS_NUMBER);
                ((EditText) value_view).setBackgroundColor(Color.parseColor("#dddddd"));
            }
            else if(type.equalsIgnoreCase("TextArea"))
            {
                value_view = new EditText(context);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        200);
                params.setMargins(0,0,0,16);
                value_view.setLayoutParams(params);
                ((EditText) value_view).setHint(placeholder);
                ((EditText) value_view).setTextSize(TypedValue.COMPLEX_UNIT_SP,13);
                ((EditText) value_view).setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                ((EditText) value_view).setGravity(Gravity.TOP);
                ((EditText) value_view).setBackgroundColor(Color.parseColor("#dddddd"));
            }
            else if(type.equalsIgnoreCase("Label"))
            {
                value_view = new TextView(context);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0,0,0,16);
                value_view.setLayoutParams(params);
                ((TextView) value_view).setText(defaultvalue);
                ((TextView) value_view).setTextSize(TypedValue.COMPLEX_UNIT_SP,13);
            }
            else if(type.equalsIgnoreCase("Image"))
            {
                value_view = new ImageView(context);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        400);
                params.setMargins(0,0,0,16);
                value_view.setLayoutParams(params);
                ((ImageView) value_view).setScaleType(ImageView.ScaleType.FIT_CENTER);
                (new DownloadImageTask((ImageView) value_view))
                        .execute(defaultvalue);
            }
            else if(type.equalsIgnoreCase("Photo"))
            {
                Util.saveToSP(context,null,(Cons.CAMERA_FormFieldViewHolderPrefix + id));
                // Borrar cache foto que pueda existir de un formulario anterior;
                value_view = new ImageView(context);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        400);
                params.setMargins(0,0,0,16);
                value_view.setLayoutParams(params);
                ((ImageView) value_view).setImageResource(R.drawable.ic_photo_camera_black_24dp);
                ((ImageView) value_view).setScaleType(ImageView.ScaleType.FIT_CENTER);
                value_view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Util.saveToSP(context,(Cons.CAMERA_FormFieldViewHolderPrefix + id),
                                Cons.CAMERA_CacheCurrentID); // Ruta para cache de foto
                        Util.saveToSP(context, "true",Cons.CAMERA_PictureLoad);
                        Util.saveToSP(context, id,Cons.CAMERA_PictureLoadID);
                        // No olvidar leer luego los datos desde aca
                        Intent intent = new Intent(context, ActivityCamera.class);
                        context.startActivity(intent);
                    }
                });
            }
            else if(type.equalsIgnoreCase("Signature"))
            {
                Util.saveToSP(context,null,(Cons.CAMERA_FormFieldViewHolderPrefix + id));
                // Borrar cache foto que pueda existir de un formulario anterior;
                value_view = new ImageView(context);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        400);
                params.setMargins(0,0,0,16);
                value_view.setLayoutParams(params);
                ((ImageView) value_view).setImageResource(R.drawable.ic_border_color_black_24dp);
                ((ImageView) value_view).setScaleType(ImageView.ScaleType.FIT_CENTER);
                value_view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Util.saveToSP(context,(Cons.CAMERA_FormFieldViewHolderPrefix + id),
                                Cons.CAMERA_CacheCurrentID); // Ruta para cache de foto
                        Util.saveToSP(context, "true",Cons.CAMERA_PictureLoad);
                        Util.saveToSP(context, id,Cons.CAMERA_PictureLoadID);
                        // No olvidar leer luego los datos desde aca
                        Intent intent = new Intent(context, ActivityCanvas.class);
                        context.startActivity(intent);
                    }
                });
            }
            else if(type.equalsIgnoreCase("Combo") && options.size() > 0)
            {
                value_view = new TextView(context);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0,12,0,12+16);
                value_view.setLayoutParams(params);
                ((TextView) value_view).setTypeface(((TextView) value_view).getTypeface(), Typeface.BOLD);
                ((TextView) value_view).setText((options.get(0)));
                value_aux = options.get(0);
                ((TextView) value_view).setTextSize(TypedValue.COMPLEX_UNIT_SP,13);
                value_view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ArrayList<String> temp = new ArrayList<>(options);
                        final CharSequence options_cs[] = temp.toArray(
                                new CharSequence[temp.size()]);
                        if(temp.size() == options.size() &&
                                temp.size() == options_cs.length) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle(title);
                            builder.setItems(options_cs, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ((TextView) value_view).setText(options.get(which));
                                    value_aux = options.get(which);
                                }
                            });
                            builder.show();
                        }
                    }
                });
            }
            else if(type.equalsIgnoreCase("Boolean"))
            {
                view_name.setVisibility(View.GONE); // Ocultar titulo en este caso
                value_view = new CheckBox(context);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0,0,0,16);
                value_view.setLayoutParams(params);
                ((CheckBox) value_view).setText(title);
                value_aux = "0";
                ((CheckBox) value_view).setTextSize(TypedValue.COMPLEX_UNIT_SP,13);
                ((CheckBox) value_view).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked)
                        {
                            value_aux = "1";
                        }
                        else
                        {
                            value_aux = "0";
                        }
                    }
                });
            }
            else if(type.equalsIgnoreCase("Date"))// DateTime
            {
                value_view = new EditText(context);
                value_view.setFocusable(false);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0,0,0,16);
                value_view.setLayoutParams(params);
                ((EditText) value_view).setHint(placeholder);
                ((EditText) value_view).setTextSize(TypedValue.COMPLEX_UNIT_SP,13);
                ((EditText) value_view).setInputType(InputType.TYPE_CLASS_TEXT);
                value_view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            DatePickerDialog recogerFecha = new DatePickerDialog(context,
                                    new DatePickerDialog.OnDateSetListener() {
                                        @Override
                                        public void onDateSet(DatePicker view, int year, int monthNumber, int date) {
                                            String fecha = year + "-" + Util.leftZeros(1 + monthNumber, 2) + "-" +
                                                    Util.leftZeros(date, 2);
                                            ((TextView) value_view).setText(fecha);
                                        }
                                    }, Integer.parseInt(Util.getFechaCustom("yyyy")),
                                    Integer.parseInt(Util.getFechaCustom("MM"))-1,
                                    Integer.parseInt(Util.getFechaCustom("dd")));
                            //Muestro el widget
                            recogerFecha.show();
                        }
                        catch (NumberFormatException ignored){}
                    }
                });
                ((EditText) value_view).setBackgroundColor(Color.parseColor("#dddddd"));
            }
            else if(type.equalsIgnoreCase("Time"))// DateTime
            {
                value_view = new EditText(context);
                value_view.setFocusable(false);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0,0,0,16);
                value_view.setLayoutParams(params);
                ((EditText) value_view).setHint(placeholder);
                ((EditText) value_view).setTextSize(TypedValue.COMPLEX_UNIT_SP,13);
                ((EditText) value_view).setInputType(InputType.TYPE_CLASS_TEXT);
                value_view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            TimePickerDialog recogerHora = new TimePickerDialog(context,
                                    new TimePickerDialog.OnTimeSetListener() {
                                        @Override
                                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                            String fecha = Util.leftZeros(hourOfDay, 2) + ":" +
                                                    Util.leftZeros(minute, 2);
                                            ((TextView) value_view).setText(fecha);
                                        }

                                    }, Integer.parseInt(Util.getFechaCustom("HH")),
                                    Integer.parseInt(Util.getFechaCustom("mm")), true);

                            recogerHora.show();
                        }
                        catch (NumberFormatException ignored){}
                    }
                });
                ((EditText) value_view).setBackgroundColor(Color.parseColor("#dddddd"));
            }
            else if(type.equalsIgnoreCase("DateTime"))// DateTime
            {
                value_view = new EditText(context);
                value_view.setFocusable(false);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0,0,0,16);
                value_view.setLayoutParams(params);
                ((EditText) value_view).setHint(placeholder);
                ((EditText) value_view).setTextSize(TypedValue.COMPLEX_UNIT_SP,13);
                ((EditText) value_view).setInputType(InputType.TYPE_CLASS_TEXT);
                value_view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CustomDateTimePicker cus = new CustomDateTimePicker(context,
                                new CustomDateTimePicker.ICustomDateTimeListener() {
                                    @Override
                                    public void onSet(Dialog dialog, Calendar calendarSelected,
                                                      Date dateSelected, int year,
                                                      String monthFullName,
                                                      String monthShortName,
                                                      int monthNumber, int date,
                                                      String weekDayFullName,
                                                      String weekDayShortName, int hour24,
                                                      int hour12,
                                                      int min, int sec, String AM_PM) {
                                        String fecha = year + "-" + Util.leftZeros(1+monthNumber,2) + "-" +
                                                Util.leftZeros(date,2) + " " + Util.leftZeros(hour24,2) + ":" +
                                                Util.leftZeros(min,2);
                                        ((TextView) value_view).setText(fecha);
                                    }

                                    @Override
                                    public void onCancel() {

                                    }
                                }).set24HourFormat(true).setDate(Calendar.getInstance());
                        cus.showDialog();
                    }
                });
                ((EditText) value_view).setBackgroundColor(Color.parseColor("#dddddd"));
            }
            else // Text, Fallback
            {
                value_view = new EditText(context);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0,0,0,16);
                value_view.setLayoutParams(params);
                ((EditText) value_view).setHint(placeholder);
                ((EditText) value_view).setTextSize(TypedValue.COMPLEX_UNIT_SP,13);
                ((EditText) value_view).setInputType(InputType.TYPE_CLASS_TEXT);
                ((EditText) value_view).setBackgroundColor(Color.parseColor("#dddddd"));
            }
        }
    }


}
