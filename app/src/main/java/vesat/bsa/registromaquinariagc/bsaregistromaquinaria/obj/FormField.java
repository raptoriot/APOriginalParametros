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
    public View view_value;
    public String view_aux;

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
                    options.add(Cons.Combo_Not_Selected);
                }
                for (int x = 0; x < jr_field_options.length(); x++) {
                    JSONObject option = !jr_field_options.isNull(x) ? jr_field_options.getJSONObject(x) : null;
                    if (option != null) {
                        options.add(option.getString("value"));
                    }
                }
            }
        }
        catch (JSONException e){}
    }

    public void createView(final Context context,boolean active)
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
        view_value = null;
        if(type != null)
        {
            if(type.equalsIgnoreCase("Number"))
            {
                view_value = new EditText(context);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0,0,0,16);
                view_value.setLayoutParams(params);
                if(placeholder != null) {
                    ((EditText) view_value).setHint(placeholder);
                }
                ((EditText) view_value).setTextSize(TypedValue.COMPLEX_UNIT_SP,13);
                ((EditText) view_value).setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                ((EditText) view_value).setBackgroundColor(Color.parseColor("#dddddd"));
            }
            else if(type.equalsIgnoreCase("TextArea"))
            {
                view_value = new EditText(context);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        200);
                params.setMargins(0,0,0,16);
                view_value.setLayoutParams(params);
                if(placeholder != null) {
                    ((EditText) view_value).setHint(placeholder);
                }
                ((EditText) view_value).setTextSize(TypedValue.COMPLEX_UNIT_SP,13);
                ((EditText) view_value).setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                ((EditText) view_value).setGravity(Gravity.TOP);
                ((EditText) view_value).setBackgroundColor(Color.parseColor("#dddddd"));
            }
            else if(type.equalsIgnoreCase("Label"))
            {
                view_name.setVisibility(View.GONE);
                view_value = new TextView(context);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0,0,0,16);
                view_value.setLayoutParams(params);
                ((TextView) view_value).setText(defaultvalue);
                ((TextView) view_value).setTextSize(TypedValue.COMPLEX_UNIT_SP,13);
            }
            else if(type.equalsIgnoreCase("Image"))
            {
                view_name.setVisibility(View.GONE);
                view_value = new ImageView(context);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        400);
                params.setMargins(0,0,0,16);
                view_value.setLayoutParams(params);
                ((ImageView) view_value).setScaleType(ImageView.ScaleType.FIT_CENTER);
                (new DownloadImageTask((ImageView) view_value))
                        .execute(defaultvalue);
            }
            else if(type.equalsIgnoreCase("Photo"))
            {
                Util.saveToSP(context,null,(Cons.CAMERA_FormFieldViewHolderPrefix + id));
                // Borrar cache foto que pueda existir de un formulario anterior;
                view_value = new ImageView(context);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        400);
                params.setMargins(0,0,0,16);
                view_value.setLayoutParams(params);
                ((ImageView) view_value).setImageResource(R.drawable.ic_photo_camera_black_24dp);
                ((ImageView) view_value).setScaleType(ImageView.ScaleType.FIT_CENTER);
                if(active) {
                    view_value.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Util.saveToSP(context, (Cons.CAMERA_FormFieldViewHolderPrefix + id),
                                    Cons.CAMERA_CacheCurrentID); // Ruta para cache de foto
                            Util.saveToSP(context, "true", Cons.CAMERA_PictureLoad);
                            Util.saveToSP(context, id, Cons.CAMERA_PictureLoadID);
                            // No olvidar leer luego los datos desde aca
                            Intent intent = new Intent(context, ActivityCamera.class);
                            context.startActivity(intent);
                        }
                    });
                }
            }
            else if(type.equalsIgnoreCase("Signature"))
            {
                Util.saveToSP(context,null,(Cons.CAMERA_FormFieldViewHolderPrefix + id));
                // Borrar cache foto que pueda existir de un formulario anterior;
                view_value = new ImageView(context);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        400);
                params.setMargins(0,0,0,16);
                view_value.setLayoutParams(params);
                ((ImageView) view_value).setImageResource(R.drawable.ic_border_color_black_24dp);
                ((ImageView) view_value).setScaleType(ImageView.ScaleType.FIT_CENTER);
                if(active) {
                    view_value.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Util.saveToSP(context, (Cons.CAMERA_FormFieldViewHolderPrefix + id),
                                    Cons.CAMERA_CacheCurrentID); // Ruta para cache de foto
                            Util.saveToSP(context, "true", Cons.CAMERA_PictureLoad);
                            Util.saveToSP(context, id, Cons.CAMERA_PictureLoadID);
                            // No olvidar leer luego los datos desde aca
                            Intent intent = new Intent(context, ActivityCanvas.class);
                            context.startActivity(intent);
                        }
                    });
                }
            }
            else if(type.equalsIgnoreCase("Combo") && options.size() > 0)
            {
                view_value = new TextView(context);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0,12,0,12+16);
                view_value.setLayoutParams(params);
                ((TextView) view_value).setTypeface(((TextView) view_value).getTypeface(), Typeface.BOLD);
                ((TextView) view_value).setText((options.get(0)));
                view_aux = options.get(0);
                ((TextView) view_value).setTextSize(TypedValue.COMPLEX_UNIT_SP,13);
                if(active) {
                    view_value.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ArrayList<String> temp = new ArrayList<>(options);
                            final CharSequence options_cs[] = temp.toArray(
                                    new CharSequence[temp.size()]);
                            if (temp.size() == options.size() &&
                                    temp.size() == options_cs.length) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setTitle(title);
                                builder.setItems(options_cs, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ((TextView) view_value).setText(options.get(which));
                                        view_aux = options.get(which);
                                    }
                                });
                                builder.show();
                            }
                        }
                    });
                }
            }
            else if(type.equalsIgnoreCase("Boolean"))
            {
                view_name.setVisibility(View.GONE); // Ocultar titulo en este caso
                view_value = new CheckBox(context);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0,0,0,16);
                view_value.setLayoutParams(params);
                ((CheckBox) view_value).setText(title);
                view_aux = "0";
                ((CheckBox) view_value).setTextSize(TypedValue.COMPLEX_UNIT_SP,13);
                if(active) {
                    ((CheckBox) view_value).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {
                                view_aux = "1";
                            } else {
                                view_aux = "0";
                            }
                        }
                    });
                }
                else
                {
                    view_value.setClickable(false);
                }
            }
            else if(type.equalsIgnoreCase("Date"))// DateTime
            {
                view_value = new EditText(context);
                view_value.setFocusable(false);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0,0,0,16);
                view_value.setLayoutParams(params);
                if(placeholder != null) {
                    ((EditText) view_value).setHint(placeholder);
                }
                ((EditText) view_value).setTextSize(TypedValue.COMPLEX_UNIT_SP,13);
                ((EditText) view_value).setInputType(InputType.TYPE_CLASS_TEXT);
                if(active) {
                    view_value.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                DatePickerDialog recogerFecha = new DatePickerDialog(context,
                                        new DatePickerDialog.OnDateSetListener() {
                                            @Override
                                            public void onDateSet(DatePicker view, int year, int monthNumber, int date) {
                                                String fecha = year + "-" + Util.leftZeros(1 + monthNumber, 2) + "-" +
                                                        Util.leftZeros(date, 2);
                                                ((TextView) view_value).setText(fecha);
                                            }
                                        }, Integer.parseInt(Util.getFechaCustom("yyyy")),
                                        Integer.parseInt(Util.getFechaCustom("MM")) - 1,
                                        Integer.parseInt(Util.getFechaCustom("dd")));
                                //Muestro el widget
                                recogerFecha.show();
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    });
                }
                ((EditText) view_value).setBackgroundColor(Color.parseColor("#dddddd"));
            }
            else if(type.equalsIgnoreCase("Time"))// DateTime
            {
                view_value = new EditText(context);
                view_value.setFocusable(false);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0,0,0,16);
                view_value.setLayoutParams(params);
                if(placeholder != null) {
                    ((EditText) view_value).setHint(placeholder);
                }
                ((EditText) view_value).setTextSize(TypedValue.COMPLEX_UNIT_SP,13);
                ((EditText) view_value).setInputType(InputType.TYPE_CLASS_TEXT);
                if(active) {
                    view_value.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                TimePickerDialog recogerHora = new TimePickerDialog(context,
                                        new TimePickerDialog.OnTimeSetListener() {
                                            @Override
                                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                                String fecha = Util.leftZeros(hourOfDay, 2) + ":" +
                                                        Util.leftZeros(minute, 2);
                                                ((TextView) view_value).setText(fecha);
                                            }

                                        }, Integer.parseInt(Util.getFechaCustom("HH")),
                                        Integer.parseInt(Util.getFechaCustom("mm")), true);

                                recogerHora.show();
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    });
                }
                ((EditText) view_value).setBackgroundColor(Color.parseColor("#dddddd"));
            }
            else if(type.equalsIgnoreCase("DateTime"))// DateTime
            {
                view_value = new EditText(context);
                view_value.setFocusable(false);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0,0,0,16);
                view_value.setLayoutParams(params);
                if(placeholder != null) {
                    ((EditText) view_value).setHint(placeholder);
                }
                ((EditText) view_value).setTextSize(TypedValue.COMPLEX_UNIT_SP,13);
                ((EditText) view_value).setInputType(InputType.TYPE_CLASS_TEXT);
                if(active) {
                    view_value.setOnClickListener(new View.OnClickListener() {
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
                                            String fecha = year + "-" + Util.leftZeros(1 + monthNumber, 2) + "-" +
                                                    Util.leftZeros(date, 2) + " " + Util.leftZeros(hour24, 2) + ":" +
                                                    Util.leftZeros(min, 2);
                                            ((TextView) view_value).setText(fecha);
                                        }

                                        @Override
                                        public void onCancel() {

                                        }
                                    }).set24HourFormat(true).setDate(Calendar.getInstance());
                            cus.showDialog();
                        }
                    });
                }
                ((EditText) view_value).setBackgroundColor(Color.parseColor("#dddddd"));
            }
            else // Text, Fallback
            {
                view_value = new EditText(context);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0,0,0,16);
                view_value.setLayoutParams(params);
                if(placeholder != null) {
                    ((EditText) view_value).setHint(placeholder);
                }
                ((EditText) view_value).setTextSize(TypedValue.COMPLEX_UNIT_SP,13);
                ((EditText) view_value).setInputType(InputType.TYPE_CLASS_TEXT);
                ((EditText) view_value).setBackgroundColor(Color.parseColor("#dddddd"));
            }
        }
        if(!active)
        {
            view_value.setFocusable(false);
        }
    }


}
