package vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.R;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.obj.RondaElem;

public class AdapterFormularioHistoryRonda extends RecyclerView.Adapter<AdapterFormularioHistoryRonda.CustomViewHolder> {
    private ArrayList<RondaElem> arrRondas;
    private Context context;

    public AdapterFormularioHistoryRonda(Context context, ArrayList<RondaElem> arrRondas) {
        this.arrRondas = arrRondas;
        this.context = context;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_ronda, viewGroup,
                false);
        return new CustomViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder customViewHolder, int i) {
        RondaElem rondaElem = arrRondas.get(i);
        customViewHolder.rondaIndex.setText((rondaElem.index != null ? ""+rondaElem.index : "---"));
        customViewHolder.rondaHora.setText(rondaElem.fecha);
        customViewHolder.turnoHora.setText((rondaElem.turno != null ? rondaElem.turno : "---"));
    }

    @Override
    public int getItemCount() {
        return (null != arrRondas ? arrRondas.size() : 0);
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        TextView rondaIndex;
        TextView rondaHora;
        TextView turnoHora;

        CustomViewHolder(View view) {
            super(view);
            this.rondaIndex = view.findViewById(R.id.rondaIndex);
            this.rondaHora = view.findViewById(R.id.rondaHora);
            this.turnoHora = view.findViewById(R.id.turnoHora);
        }
    }
}
