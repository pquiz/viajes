package Mygymmate.gymmate;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.support.v7.widget.DividerItemDecoration.VERTICAL;

public class AgregaViajeActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, CostoAdaptador.CostoClickListener {
    private static final int PERMISSIONS_REQUEST_FINE_LOCATION = 111;
    private static final int PLACEPICKERREQUEST = 1;
    TextView lugar;
    EditText nombre;
    EditText motivo;
    EditText rfc;
    Button botonAgrega;
    static TextView fechaInicio;
    static Date fInicio;
    static TextView fechaFin;
    static Date fFin;
    private Context mContext;
    private Spinner spinner;
    static boolean bandera = true;
    private String userId;
    private FirebaseDatabase mDataBase;
    private DatabaseReference mreference;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mStorageReference;
    private String uuid;
    private boolean modificar = false;
    private ViajeMensaje viaje;
    private RecyclerView costo_recycler;
    private CostoAdaptador mCostoAdapter;
    private ChildEventListener mChildEventListener;

    public void agregaViaje(View view) {
        mDataBase = FirebaseDatabase.getInstance();
        if (!modificar) {
            mreference = mDataBase.getReference().child(userId);
            generaViaje();
            mreference.push().setValue(viaje);
        } else {
            mreference = mDataBase.getReference().child(userId).child(uuid);
            generaViaje();
            mreference.setValue(viaje);
        }
        finish();
    }

    private void generaViaje() {
        viaje = new ViajeMensaje();
        if (modificar) viaje.setUuid(uuid);
        viaje.setNombre(nombre.getText().toString());
        viaje.setMotivo(motivo.getText().toString());
        viaje.setFechaFin(fFin.getTime());
        viaje.setFechaInicio(fInicio.getTime());
        viaje.setCostos(new ArrayList<CostoMensaje>());
        viaje.setLugar(lugar.getText().toString());
        viaje.setRfc(rfc.getText().toString());
        viaje.setMoneda(spinner.getSelectedItem().toString());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_agrega_viaje);
        if (savedInstanceState != null) {
            userId = savedInstanceState.getString("userId");
            Toast.makeText(this, userId, Toast.LENGTH_LONG).show();
        }
        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        uuid = intent.getStringExtra("uuid");
        botonAgrega = (Button) findViewById(R.id.botonagrega);
        //firebasestorage inicializar
        mFirebaseStorage = FirebaseStorage.getInstance();

        spinner = (Spinner) findViewById(R.id.spinner);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.monedas_array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        String permission = Manifest.permission.ACCESS_FINE_LOCATION;
        int res = checkCallingOrSelfPermission(permission);
        if (!(res == PackageManager.PERMISSION_GRANTED))
            ActivityCompat.requestPermissions(AgregaViajeActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_FINE_LOCATION);
        else {
            Places.initialize(getApplicationContext(), "AIzaSyDI5WuUO1csVhGtUMFk8PVrEF2nbPdfQk0");

// Create a new Places client instance.
            PlacesClient placesClient = Places.createClient(this);
        }
        lugar = (TextView) findViewById(R.id.lugar);
        fechaInicio = (TextView) findViewById(R.id.fecha_inicio);
        fechaFin = (TextView) findViewById(R.id.fecha_fin);
        nombre = (EditText) findViewById(R.id.camponombre);
        motivo = (EditText) findViewById(R.id.motivo);
        rfc = (EditText) findViewById(R.id.rfc);
        if (uuid != null) {
            costo_recycler = findViewById(R.id.costo_recycler);
            costo_recycler.setLayoutManager(new LinearLayoutManager(this));

            // Initialize the adapter and attach it to the RecyclerView
            mCostoAdapter = new CostoAdaptador(this, this);
            mCostoAdapter.setCostoEntries(null);
            costo_recycler.setAdapter(mCostoAdapter);

            DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), VERTICAL);
            costo_recycler.addItemDecoration(decoration);
            botonAgrega.setText("Modificar viaje");
            modificar = true;
            mDataBase = FirebaseDatabase.getInstance();
            mreference = mDataBase.getReference().child(userId).child(uuid);
            mreference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    GenericTypeIndicator<ViajeMensaje> t = new GenericTypeIndicator<ViajeMensaje>() {
                    };

                    ViajeMensaje viejeConsulta = dataSnapshot.getValue(t);
                    mCostoAdapter.setCostoEntries(null);
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        try {
                            CostoMensaje commandObject = ds.getValue(CostoMensaje.class);
                            commandObject.setUuid(ds.getKey());
                            validaArchivos(commandObject);
                            mCostoAdapter.addEntrie(commandObject);
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println(e.getMessage());
                        }

                    }
                    lugar.setText(viejeConsulta.getLugar());
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/mm/yyyy");
                    String strDate = dateFormat.format(viejeConsulta.getFechaInicio());
                    fechaInicio.setText(strDate);
                    fInicio = new Date(viejeConsulta.getFechaInicio());
                    fFin = new Date(viejeConsulta.getFechaFin());
                    strDate = dateFormat.format(viejeConsulta.getFechaFin());
                    fechaFin.setText(strDate);
                    nombre.setText(viejeConsulta.getNombre());
                    motivo.setText(viejeConsulta.getMotivo());
                    rfc.setText(viejeConsulta.getRfc());
                    spinner.setSelection(((ArrayAdapter) spinner.getAdapter()).getPosition(viejeConsulta.getMoneda()));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
    }

    public void validaArchivos(CostoMensaje costo_temp) {
        try {
            if (costo_temp.getPdf() != null) {
                descargaArchivos(costo_temp.getPdf());
            }
            if (costo_temp.getXml() != null) {
                descargaArchivos(costo_temp.getXml());
            }
            if (costo_temp.getTicket() != null) {
                descargaArchivos(costo_temp.getTicket());
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    public void descargaArchivos(String url) throws IOException {
        mStorageReference=mFirebaseStorage.getReferenceFromUrl(url);
        File imagePath = new File(getFilesDir(), "images");
        Uri uri=Uri.parse(url);


        File localFile = new File(imagePath, uri.getLastPathSegment());
Log.d("AgregaviajeActivity",localFile.getAbsolutePath());
        mStorageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                // Local temp file has been created
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }

    public void enviaCorreo(File file) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        Uri contentUri = FileProvider.getUriForFile(mContext, "Mygymmate.gymmate.fileprovider", file);
        ArrayList<Uri> files = new ArrayList<Uri>();
        files.add(contentUri);
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
        intent.setType("application/pdf");
        startActivity(intent);
    }

    public void agregaFechaInicio(View view) {
        bandera = true;
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");

    }

    public void agregaFechaFin(View view) {
        bandera = false;
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");

    }

    public void addPlace(View view) {
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME);

        // Start the autocomplete intent.
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields)
                .build(this);
        startActivityForResult(intent, PLACEPICKERREQUEST);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == PLACEPICKERREQUEST && resultCode == RESULT_OK) {
            Place place = Autocomplete.getPlaceFromIntent(data);
            if (place == null)
                Toast.makeText(this, "No place selected", Toast.LENGTH_LONG).show();
            else {

                lugar.setText(place.getName());
            }

        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void agregaCosto(View view) {
        Intent intent = new Intent(this, AgregaCosto.class);
        intent.putExtra("userId", userId);
        intent.putExtra("viaje_uuid", uuid);
        intent.putExtra("finicio", fInicio.getTime());
        intent.putExtra("ffin", fFin.getTime());
        startActivity(intent);
    }

    @Override
    public void onItemClickListener(String itemId) {
        Intent intent = new Intent(this, AgregaCosto.class);
        intent.putExtra("userId", userId);
        intent.putExtra("viaje_uuid", uuid);
        intent.putExtra("finicio", fInicio.getTime());
        intent.putExtra("ffin", fFin.getTime());
        intent.putExtra("costo_uuid", itemId);

        //nodocosto
        startActivity(intent);
    }

    public static class TimePickerFragment extends DialogFragment
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
                if (bandera) {
                    fechaInicio.setText(day + "/" + (month + 1) + "/" + year);
                    fInicio = new SimpleDateFormat("dd/MM/yyyy").parse(day + "/" + (month + 1) + "/" + year);
                } else {
                    fechaFin.setText(day + "/" + (month + 1) + "/" + year);
                    fFin = new SimpleDateFormat("dd/MM/yyyy").parse(day + "/" + (month + 1) + "/" + year);
                }
            } catch (Exception e) {

            }
        }
    }
}
