package vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.R;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.activity.FormularioFillActivity;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.obj.Formulario;

public class AdapterFormularioListRonda extends RecyclerView.Adapter<AdapterFormularioListRonda.CustomViewHolder> {
    private ArrayList<Formulario> arrFormularios;
    private Context context;
    private String currentRondaID;

    public AdapterFormularioListRonda(Context context, ArrayList<Formulario> arrFormularios, String currentRondaID) {
        this.arrFormularios = arrFormularios;
        this.currentRondaID = currentRondaID;
        this.context = context;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_formulario, viewGroup,
                false);
        return new CustomViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder customViewHolder, int i) {
        final Formulario formulario = arrFormularios.get(i);
        customViewHolder.formName.setText(formulario.nombre);
        DBHelper db = new DBHelper(context);
        try {
            if (db.isFormSubmittedForRonda(currentRondaID
                    , Long.parseLong(formulario.id)))
            {
                customViewHolder.llayoutback.setBackgroundColor(Color.parseColor("#4fff51"));
            }
            else
            {
                customViewHolder.llayoutback.setBackgroundColor(Color.parseColor("#ff7777"));
            }
        }
        catch (NumberFormatException ignored){}
        db.close();
        customViewHolder.formButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DBHelper db = new DBHelper(context);
                try {
                    if(!db.isFormSubmittedForRonda(currentRondaID
                            , Long.parseLong(formulario.id))) {
                        db.close();
                        Util.saveToSP(context, formulario, Cons.Current_Form);
                        Util.saveToSP(context, currentRondaID, Cons.Current_Ronda_ID);
                        Intent i = new Intent(context, FormularioFillActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(i);
                    } else {
                        Toast.makeText(context,"Ya ingresado a la ronda.",Toast.LENGTH_SHORT).show();
                        db.close();
                    }
                }
                catch (NumberFormatException ignored)
                {
                    db.close();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return (null != arrFormularios ? arrFormularios.size() : 0);
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        TextView formName;
        ImageView formButton;
        LinearLayout llayoutback;

        CustomViewHolder(View view) {
            super(view);
            this.formName = view.findViewById(R.id.formName);
            this.formButton = view.findViewById(R.id.formButton);
            this.llayoutback = view.findViewById(R.id.llayoutback);
        }
    }
}
