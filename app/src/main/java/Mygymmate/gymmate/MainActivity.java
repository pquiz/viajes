package Mygymmate.gymmate;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import static android.support.v7.widget.DividerItemDecoration.VERTICAL;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ViajesAdaptador.ItemClickListener{
    private static final int RC_SIGN_IN = 222;

    private FirebaseDatabase mDataBase;
    private DatabaseReference mreference;
    private ChildEventListener mChildEventListener;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuth;
    private Context mContext;
    private TextView textView;
    private RecyclerView mRecyclerView;
    private ViajesAdaptador mAdapter;
    private String userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Choose authentication providers
        textView = (TextView) findViewById(R.id.text_hello);
        mRecyclerView = findViewById(R.id.viajes_recycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the adapter and attach it to the RecyclerView
        mAdapter = new ViajesAdaptador(this, this);
        mAdapter.setmTaskEntries(null);
        mRecyclerView.setAdapter(mAdapter);

        DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), VERTICAL);
        mRecyclerView.addItemDecoration(decoration);
     /*  ArrayList<CostoMensaje> array = new ArrayList<CostoMensaje>();
        CostoMensaje costos = new CostoMensaje("Mi costo de ejemplo", "Mi lugar de ejemplo", new Date().getTime());
        array.add(costos);
        ViajeMensaje viaje = new ViajeMensaje("Viaje ejemplo", new Date().getTime(), new Date().getTime(), 0, array, "mi lugar", "rfc", "Mi motivo", "mi moneda");
        ArrayList<ViajeMensaje> viajeA=new ArrayList<ViajeMensaje>();
       // viajeA.add(viaje);
        mAdapter.setmTaskEntries(viajeA);
        mreference.setValue(viaje);*/
        mFirebaseAuth = FirebaseAuth.getInstance();
        mContext = this;
        try {
            readXLS();
        } catch (Exception e) {
        }
        mAuth = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    callAuth();
                } else {
                    Toast.makeText(mContext, "usuario " + user.getEmail(), Toast.LENGTH_LONG).show();
                    mDataBase = FirebaseDatabase.getInstance();
                    userId=user.getUid();
                    mreference = mDataBase.getReference().child(userId);
                    crearChild();

                }
            }
        };


// Create and launch sign-in intent

    }

    private void crearChild() {
        mChildEventListener= new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                ViajeMensaje viaje=dataSnapshot.getValue(ViajeMensaje.class);
                viaje.setUuid(dataSnapshot.getKey());
                mAdapter.addEntrie(viaje);


            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mreference.addChildEventListener(mChildEventListener);
    }

    private void readXLS() throws IOException {
        InputStream myInput;
// initialize asset manager
        AssetManager assetManager = getAssets();
//  open excel file name as myexcelsheet.xls
        myInput = assetManager.open("Gastos.xls");
// Create a POI File System object
        POIFSFileSystem myFileSystem = new POIFSFileSystem(myInput);
// Create a workbook using the File System
        HSSFWorkbook myWorkBook = new HSSFWorkbook(myFileSystem);
        FormulaEvaluator evaluator = myWorkBook.getCreationHelper().createFormulaEvaluator();
        myWorkBook.getCreationHelper().createFormulaEvaluator().evaluateAll();
// Get the first sheet from workbook
        HSSFSheet mySheet = myWorkBook.getSheetAt(0);
        Iterator<Row> rowIter = mySheet.rowIterator();

        textView.append("\n");
        while (rowIter.hasNext()) {
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
            textView.append(sno + "\n");
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_CANCELED) {
                // Successfully signed in
                finish();
                // ...
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AuthUI.getInstance()
                .signOut(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuth);
        mAdapter.setmTaskEntries(null);
    }

    private void callAuth() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build());
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder().setIsSmartLockEnabled(false)
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFirebaseAuth.removeAuthStateListener(mAuth);
    }

    public void agregaViaje(View view) {
        Intent intent = new Intent(this, AgregaViajeActivity.class);
        intent.putExtra("userId", userId);
        startActivity(intent);
    }

    @Override
    public void onItemClickListener(String itemId) {
        Intent intent = new Intent(this, AgregaViajeActivity.class);
        intent.putExtra("userId", userId);
        intent.putExtra("uuid",itemId);
        startActivity(intent);

    }
}
