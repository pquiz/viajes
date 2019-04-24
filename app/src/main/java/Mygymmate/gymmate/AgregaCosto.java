package Mygymmate.gymmate;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AgregaCosto extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final int RC_PDF_PICKER = 2;
    private static final int RC_XML_PICKER = 3;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mStorageReference;
    private FirebaseDatabase mDataBase;
    private DatabaseReference mreference;
    private UploadTask uploadTask;
    private Context context;
    private String userId;
    private String costo_uuid;
    private String viaje_uuid;
    private boolean modificar = false;
    private static TextView agregafechacosto;
    static Date fecha;
    private Spinner tipo_gasto;
    private EditText establecimiento_gasto;
    private EditText concepto_gasto;
    private EditText folio_factura;
    private String pdf;
    private String xml;
    private Spinner tipo_moneda_gasto;
    private EditText monto_costo;
    private EditText adicional_costo;
    private EditText iva_costo;
    private Button agregar_costo;
    private CostoMensaje costoMensaje;

    public void agregaCosto(View view) {
        mDataBase = FirebaseDatabase.getInstance();
        if (!modificar) {
            mreference = mDataBase.getReference().child(userId).child(viaje_uuid);
            generaCosto();
            mreference.push().setValue(costoMensaje);
        } else {
            mreference = mDataBase.getReference().child(userId).child(viaje_uuid).child(costo_uuid);
            generaCosto();
            mreference.setValue(costoMensaje);
        }
        finish();
    }

    private void generaCosto() {
        costoMensaje = new CostoMensaje();
        if (modificar) costoMensaje.setUuid(costo_uuid);
        costoMensaje.setFecha(fecha.getTime());
        costoMensaje.setClave(tipo_gasto.getSelectedItem().toString());
        costoMensaje.setEstablecimiento(establecimiento_gasto.getText().toString());
        costoMensaje.setConcepto(concepto_gasto.getText().toString());
        costoMensaje.setFolio(folio_factura.getText().toString());
        costoMensaje.setMoneda(tipo_moneda_gasto.getSelectedItem().toString());
        try {
            costoMensaje.setMonto(new Double(monto_costo.getText().toString()));
            costoMensaje.setAdicional(new Double(adicional_costo.getText().toString()));
            costoMensaje.setIva(new Double(iva_costo.getText().toString()));
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agrega_costo);
        context = this;
        //datos de la ruta de firebase
        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        viaje_uuid = intent.getStringExtra("viaje_uuid");
        costo_uuid = intent.getStringExtra("costo_uuid");
        //firebase
        mFirebaseStorage = FirebaseStorage.getInstance();
        mStorageReference = mFirebaseStorage.getReference("viajes");

        agregafechacosto = (TextView) findViewById(R.id.agregafechacosto);
        tipo_gasto = (Spinner) findViewById(R.id.tipo_gasto);
        establecimiento_gasto = (EditText) findViewById(R.id.establecimiento_gasto);
        concepto_gasto = (EditText) findViewById(R.id.concepto_gasto);
        folio_factura = (EditText) findViewById(R.id.folio_factura);
        tipo_moneda_gasto = (Spinner) findViewById(R.id.tipo_moneda_gasto);
        monto_costo = (EditText) findViewById(R.id.monto_costo);
        adicional_costo = (EditText) findViewById(R.id.adicional_costo);
        iva_costo = (EditText) findViewById(R.id.iva_costo);

        agregar_costo = (Button) findViewById(R.id.agregar_costo);

// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.monedas_array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        tipo_moneda_gasto.setAdapter(adapter);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,
                R.array.tipo_gastos_array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        tipo_gasto.setAdapter(adapter2);
        //inicializar el gasto
        if (costo_uuid != null) {
            agregar_costo.setText("Modificar gasto");
            modificar = true;
            mDataBase = FirebaseDatabase.getInstance();
            mreference = mDataBase.getReference().child(userId).child(viaje_uuid).child(costo_uuid);
            mreference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    GenericTypeIndicator<CostoMensaje> t = new GenericTypeIndicator<CostoMensaje>() {
                    };
                    CostoMensaje costoConsulta = dataSnapshot.getValue(t);

                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/mm/yyyy");
                    String strDate = dateFormat.format(costoConsulta.getFecha());
                    agregafechacosto.setText(strDate);
                    fecha = new Date(costoConsulta.getFecha());
                    establecimiento_gasto.setText(costoConsulta.getEstablecimiento());
                    concepto_gasto.setText(costoConsulta.getConcepto());
                    folio_factura.setText(costoConsulta.getFolio());
                    monto_costo.setText(costoConsulta.getMonto().toString());
                    adicional_costo.setText(costoConsulta.getAdicional().toString());
                    iva_costo.setText(costoConsulta.getIva().toString());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }
    public void agregaFecha(View view) {

        DialogFragment newFragment = new CostoTimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");

    }
    public void agregaPDF(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(Intent.createChooser(intent, "Agrega pdf"), RC_PDF_PICKER);
    }

    public void agregaXML(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/xml");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(Intent.createChooser(intent, "Agrega XML"), RC_XML_PICKER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Uri ruta = data.getData();
            StorageReference archivo = mStorageReference.child(ruta.getLastPathSegment());
            InputStream stream = null;

            uploadTask = archivo.putFile(ruta);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception exception) {
                    Toast.makeText(context, "", Toast.LENGTH_LONG).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    // ...

                }
            });


        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public static class CostoTimePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of TimePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            try {

                agregafechacosto.setText(day + "/" + (month + 1) + "/" + year);
                fecha = new SimpleDateFormat("dd/MM/yyyy").parse(day + "/" + (month + 1) + "/" + year);

            } catch (Exception e) {

            }
        }
    }
}
