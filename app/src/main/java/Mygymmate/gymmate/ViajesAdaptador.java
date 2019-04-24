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

public class ViajesAdaptador extends RecyclerView.Adapter<ViajesAdaptador.TaskViewHolder> {
    private static final String DATE_FORMAT = "dd/MM/yyy";


    // Member variable to handle item clicks
    private ItemClickListener mItemClickListener = null;

    public List<ViajeMensaje> getmTaskEntries() {
        return mTaskEntries;
    }

    public void addEntrie(ViajeMensaje viaje) {
        if (mTaskEntries == null)
            mTaskEntries = new ArrayList<ViajeMensaje>();
        this.mTaskEntries.add(viaje);
        notifyDataSetChanged();

    }

    public void setmTaskEntries(List<ViajeMensaje> mTaskEntries) {
        this.mTaskEntries = mTaskEntries;
        notifyDataSetChanged();
    }

    // Class variables for the List that holds task data and the Context
    private List<ViajeMensaje> mTaskEntries;
    private Context mContext;

    public ViajesAdaptador(Context context, ItemClickListener clickListener) {
        mContext = context;
        mItemClickListener = clickListener;

    }

    @NonNull
    @Override
    public ViajesAdaptador.TaskViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.recycler_view_item, viewGroup, false);

        return new TaskViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViajesAdaptador.TaskViewHolder taskViewHolder, int i) {
        ViajeMensaje viaje = mTaskEntries.get(i);
        taskViewHolder.rfc.setText(viaje.getRfc());
        taskViewHolder.lugar.setText(viaje.getLugar());
        taskViewHolder.nombre.setText(viaje.getNombre());
        taskViewHolder.uid.setText(viaje.getUuid());

    }

    @Override
    public int getItemCount() {
        if (mTaskEntries == null)
            return 0;
        else
            return mTaskEntries.size();
    }

    public interface ItemClickListener {
        void onItemClickListener(String itemId);
    }

    class TaskViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView rfc;
        TextView nombre;
        TextView lugar;
        TextView uid;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            rfc = (TextView) itemView.findViewById(R.id.rfc);
            nombre = (TextView) itemView.findViewById(R.id.nombre_empleado);
            lugar = (TextView) itemView.findViewById(R.id.nombre_viaje);
            uid = (TextView) itemView.findViewById(R.id.uiid);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mItemClickListener.onItemClickListener(uid.getText().toString());
        }
    }
}
