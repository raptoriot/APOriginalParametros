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
        customViewHolder.formAlertStatus.setVisibility(View.VISIBLE);
        try {
            DBHelper db = new DBHelper(context);
            if (db.isFormSubmittedForRonda(currentRondaID
                    , Long.parseLong(formulario.id)))
            {
                customViewHolder.parentLayout.setBackgroundColor(Color.parseColor("#4fff51"));
            }
            else
            {
                customViewHolder.parentLayout.setBackgroundColor(Color.parseColor("#ff7777"));
            }
            switch(db.getFormNivelAlertaForRonda(currentRondaID,Long.parseLong(formulario.id)))
            {
                case 1:
                    customViewHolder.formAlertStatus.setImageDrawable(context.getResources().getDrawable(R.drawable.amarillo));
                break;
                case 2:
                    customViewHolder.formAlertStatus.setImageDrawable(context.getResources().getDrawable(R.drawable.naranjo));
                break;
                case 3:
                    customViewHolder.formAlertStatus.setImageDrawable(context.getResources().getDrawable(R.drawable.rojo));
                break;
                default:
                    customViewHolder.formAlertStatus.setImageDrawable(context.getResources().getDrawable(R.drawable.gris));
                break;
            }
            db.close();
        }
        catch (Exception e) {
            Util.serverLogException(e,context);
        }
        customViewHolder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    DBHelper db = new DBHelper(context);
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
                catch (Exception e) {
                    Util.serverLogException(e,context);
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
        ImageView formAlertStatus;
        LinearLayout parentLayout;

        CustomViewHolder(View view) {
            super(view);
            this.formName = view.findViewById(R.id.formName);
            this.formAlertStatus = view.findViewById(R.id.formAlertStatus);
            this.parentLayout = view.findViewById(R.id.parentLayout);
        }
    }
}
