package Mygymmate.gymmate;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
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

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
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
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    SimpleDateFormat dateDM = new SimpleDateFormat("dd/MM");

    public void agregaViaje(View view) {
        mDataBase = FirebaseDatabase.getInstance();
        if (!modificar) {
            mreference = mDataBase.getReference().child(userId);
            generaViaje();
            mreference.push().setValue(viaje);
        } else {
            mreference = mDataBase.getReference().child(userId).child(uuid);
            generaViaje();
            viaje.setCostos(mCostoAdapter.getmCostoEntries());
            mreference.setValue(viaje);
            enviaCorreo(null);
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
                    mCostoAdapter.setCostoEntries(viejeConsulta.getCostos());
                    lugar.setText(viejeConsulta.getLugar());

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
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    public void descargaArchivos(String url) throws IOException {
        mStorageReference = mFirebaseStorage.getReferenceFromUrl(url);
        File imagePath = new File(getFilesDir(), "images");
        Uri uri = Uri.parse(url);


        File localFile = new File(imagePath, uri.getLastPathSegment());
        Log.d("AgregaviajeActivity", localFile.getAbsolutePath());
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

    public static void copy(AssetManager assetManager, File dst) throws IOException {

        InputStream in = assetManager.open("Gastos.xls");
        try {
            OutputStream out = new FileOutputStream(dst);
            try {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }

    public Uri creaExcel() throws IOException {
        InputStream myInput;
// initialize asset manager
        AssetManager assetManager = getAssets();
        File imagePath = new File(getFilesDir(), "images");
        File file = new File(imagePath,
                nombre.getText().toString().replace(" ", "_")
                        + fechaFin.getText().toString().replace("/", "") + ".xls");

        // copy(assetManager, file);
//  open excel file name as myexcelsheet.xls
        // myInput = new FileInputStream(assetManager.open("Gastos.xls"));

// Create a workbook using the File System
        HSSFWorkbook myWorkBook = new HSSFWorkbook(assetManager.open("Gastos.xls"));
        myWorkBook.setForceFormulaRecalculation(true);
        FormulaEvaluator evaluator = myWorkBook.getCreationHelper().createFormulaEvaluator();

// Get the first sheet from workbook
        HSSFSheet mySheet = myWorkBook.getSheetAt(0);
        //Iterator<Row> rowIter = mySheet.rowIterator();
        Row currentRow = mySheet.getRow(0);//empresa
        currentRow = mySheet.getRow(1);//moneda
        currentRow.getCell(6)
                .setCellValue(spinner.getSelectedItem().toString());
        currentRow = mySheet.getRow(6);//nombre y rfc
        currentRow.getCell(2)
                .setCellValue(nombre.getText().toString());//nombre
        currentRow.getCell(6)
                .setCellValue(rfc.getText().toString());//rfc
        currentRow = mySheet.getRow(7);//lugar, fecha inicio fecha fin
        currentRow.getCell(2)
                .setCellValue("A " + lugar.getText().toString() + ", del " +
                        dateFormat.format(fInicio.getTime()) +
                        " al " + dateFormat.format(fFin.getTime()));
        currentRow = mySheet.getRow(8);//motivo
        currentRow.getCell(2)
                .setCellValue(motivo.getText().toString());
        currentRow = mySheet.getRow(23);//nombre
        currentRow.getCell(5)
                .setCellValue(nombre.getText().toString());
        mySheet = myWorkBook.getSheetAt(1);
        int i=3;
        for (CostoMensaje costo :mCostoAdapter.getmCostoEntries()){
            currentRow= mySheet.getRow(i);
            currentRow.getCell(0).setCellValue(costo.getClave().substring(0,1));//tipo de costo
            currentRow.getCell(1).setCellValue(dateDM.format(costo.getFecha()));
            currentRow.getCell(2).setCellValue(costo.getEstablecimiento());
            currentRow.getCell(3).setCellValue(costo.getConcepto());
            currentRow.getCell(4).setCellValue(costo.getFolio());
            currentRow.getCell(5).setCellValue(costo.getMoneda());
            currentRow.getCell(6).setCellValue(costo.getMonto());
            currentRow.getCell(7).setCellValue(costo.getAdicional());
            currentRow.getCell(8).setCellValue(costo.getIva());
            i++;
        }
        HSSFFormulaEvaluator formulas=myWorkBook.getCreationHelper().createFormulaEvaluator();
        formulas.setIgnoreMissingWorkbooks(true);
        formulas.evaluateAll();


        formulas.evaluateAll();
        FileOutputStream fileOut = new FileOutputStream(file);
        myWorkBook.write(fileOut);
        fileOut.close();
        myWorkBook.close();

        //textView.append("\n");)
        /*while (rowIter.hasNext()) {
            HSSFRow myRow = (HSSFRow) rowIter.next();

            Iterator<Cell> cellIter = myRow.cellIterator();

            String sno = "", date = "", det = "";
            while (cellIter.hasNext()) {
                HSSFCell myCell = (HSSFCell) cellIter.next();

                switch (myCell.getCellType()) {
                    case Cell.CELL_TYPE_BOOLEAN:


                    case Cell.CELL_TYPE_NUMERIC:

                    case Cell.CELL_TYPE_STRING:
                        sno += " " + myCell.toString();
                        break;
                    case Cell.CELL_TYPE_BLANK:
                        break;
                    case Cell.CELL_TYPE_ERROR:
                        break;

                    // CELL_TYPE_FORMULA will never happen
                    case Cell.CELL_TYPE_FORMULA:
                        CellValue cellValue = evaluator.evaluate(myCell);
                        switch (cellValue.getCellType()) {
                            case Cell.CELL_TYPE_BOOLEAN:


                            case Cell.CELL_TYPE_NUMERIC:
                                sno += " " + cellValue.getNumberValue();

                            case Cell.CELL_TYPE_STRING:
                                sno += " " + cellValue.getStringValue();
                                break;
                            case Cell.CELL_TYPE_BLANK:
                                break;
                            case Cell.CELL_TYPE_ERROR:
                                break;
                        }

                        break;
                }
            }
           // textView.append(sno + "\n");
        }*/

        return FileProvider.getUriForFile(this, "Mygymmate.gymmate.fileprovider", file);

    }


    public void enviaCorreo(File file) {
        try {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND_MULTIPLE);
            // Uri contentUri = FileProvider.getUriForFile(mContext, "Mygymmate.gymmate.fileprovider", file);
            ArrayList<Uri> files = new ArrayList<Uri>();
            //  files.add(contentUri);
            files.add(creaExcel());
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
            intent.setType("*/*");
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
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
