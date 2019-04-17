package Mygymmate.gymmate;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class AgregaCosto extends AppCompatActivity {
    private static final int RC_PDF_PICKER = 2;
    private static final int RC_XML_PICKER = 3;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mStorageReference;
    private UploadTask uploadTask;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agrega_costo);
        mFirebaseStorage = FirebaseStorage.getInstance();
        context=this;
        mStorageReference = mFirebaseStorage.getReference("viajes");
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
                public void onFailure( Exception exception) {
                    Toast.makeText(context,"",Toast.LENGTH_LONG).show();
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
}
