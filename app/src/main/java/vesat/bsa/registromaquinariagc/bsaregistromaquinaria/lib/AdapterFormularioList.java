package vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.R;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.activity.FormularioFillActivity;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.activity.FormularioHistoryActivity;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.obj.Formulario;

public class AdapterFormularioList extends RecyclerView.Adapter<AdapterFormularioList.CustomViewHolder> {
    private ArrayList<Formulario> arrFormularios;
    private Context context;

    public AdapterFormularioList(Context context, ArrayList<Formulario> arrFormularios) {
        this.arrFormularios = arrFormularios;
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
        customViewHolder.formButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.saveToSP(context,formulario,Cons.Current_Form);
                Intent i = new Intent(context,FormularioFillActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
        });
        customViewHolder.formButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.saveToSP(context,formulario,Cons.Current_Form);
                Intent i = new Intent(context,FormularioHistoryActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
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
        ImageView formButton2;

        CustomViewHolder(View view) {
            super(view);
            this.formName = view.findViewById(R.id.formName);
            this.formButton = view.findViewById(R.id.formButton);
            this.formButton2 = view.findViewById(R.id.formButton2);
        }
    }
}
