package Mygymmate.gymmate;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CostoAdaptador extends RecyclerView.Adapter<CostoAdaptador.CostoViewHolder> {
    private CostoClickListener mCostoClickListener = null;
    private static final String DATE_FORMAT = "dd/MM/yyy";
    private ArrayList<CostoMensaje> mCostoEntries;
    private Context mContext;

    public CostoAdaptador(Context context, CostoClickListener clickListener) {
        mContext = context;
        mCostoClickListener = clickListener;
    }

    public ArrayList<CostoMensaje> getmCostoEntries() {
        return mCostoEntries;
    }

    public void addEntrie(CostoMensaje costo) {
        if (mCostoEntries == null)
            mCostoEntries = new ArrayList<CostoMensaje>();
        this.mCostoEntries.add(costo);
        notifyDataSetChanged();

    }

    public void setCostoEntries(ArrayList<CostoMensaje> mTaskEntries) {
        this.mCostoEntries = mTaskEntries;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CostoViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.recycler_costo, viewGroup, false);

        return new CostoViewHolder(view);
    }


    @Override
    public int getItemCount() {
        if (mCostoEntries == null)
            return 0;
        else
            return mCostoEntries.size();
    }

    @Override
    public void onBindViewHolder(@NonNull CostoViewHolder costoViewHolder, int i) {
        CostoMensaje costo = mCostoEntries.get(i);
        costoViewHolder.text_clave_costo.setText(costo.getClave());
        costoViewHolder.text_fecha_costo.setText(costo.getFecha().toString());
        costoViewHolder.text_establecimiento.setText(costo.getEstablecimiento());
        costoViewHolder.text_folio_factura.setText(costo.getFolio());
        costoViewHolder.uiid_costo.setText(costo.getUuid());

    }

    public interface CostoClickListener {
        void onItemClickListener(String itemId);
    }

    class CostoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView text_clave_costo;
        TextView text_fecha_costo;
        TextView text_establecimiento;
        TextView text_folio_factura;
        TextView uiid_costo;

        public CostoViewHolder(@NonNull View itemView) {
            super(itemView);
            text_clave_costo = (TextView) itemView.findViewById(R.id.text_clave_costo);
            text_fecha_costo = (TextView) itemView.findViewById(R.id.text_fecha_costo);
            text_establecimiento = (TextView) itemView.findViewById(R.id.text_establecimiento);
            text_folio_factura = (TextView) itemView.findViewById(R.id.text_folio_factura);
            uiid_costo = (TextView) itemView.findViewById(R.id.uiid_costo);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mCostoClickListener.onItemClickListener(""+getAdapterPosition());
        }
    }
}
