package xyz.cumaki.mlkit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.Manifest;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity  {
    public static int REQUEST_CAMERA = 111;
    public static int REQUEST_GALLERY = 222;
    Bitmap mSelectedImage;
    ImageView mImageView;
    ArrayList<String> permisosNoAprobados;
    Button btnCamara;
    Button btnGaleria;
    TextView txtResults;
    private BarcodeScanner scanner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArrayList<String> permisos_requeridos = new ArrayList<String>();
        permisos_requeridos.add(Manifest.permission.CAMERA);
        permisos_requeridos.add(Manifest.permission.MANAGE_EXTERNAL_STORAGE);
        permisos_requeridos.add(Manifest.permission.READ_EXTERNAL_STORAGE);


        txtResults = findViewById(R.id.txtresults);
        mImageView = findViewById(R.id.image_view);
        btnCamara = findViewById(R.id.btCamera);
        btnGaleria=  findViewById(R.id.btGallery);


        permisosNoAprobados  = getPermisosNoAprobados(permisos_requeridos);
        requestPermissions(permisosNoAprobados.toArray(new String[permisosNoAprobados.size()]),
                100);

        BarcodeScannerOptions options =
                new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_QR_CODE, Barcode.FORMAT_AZTEC).build();
      scanner = BarcodeScanning.getClient(options);

    }

    public void abrirGaleria (View view){
        Intent i = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, REQUEST_GALLERY);
    }
    public void abrirCamera (View view){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {



        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && null != data) {
            try {

                    mSelectedImage = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                    mImageView.setImageBitmap(mSelectedImage);


                    InputImage image = InputImage.fromBitmap(mSelectedImage, 0);


                    Task<List<Barcode>> result = scanner.process(image)
                            .addOnSuccessListener(barcodes -> {
                                for (Barcode barcode : barcodes) {

                                    String barcodeValue = barcode.getRawValue();
                                    txtResults.setText("Código detectado : " + barcodeValue);
                                }
                            })
                            .addOnFailureListener(e -> {

                                txtResults.setText("Error al detectar código: " + e.getMessage());
                            });

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void iniciarEscaneo(View view) {
        // Asegúrate de que la cámara esté disponible y los permisos sean otorgados
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // Inicia el escaneo de códigos
            if (mImageView != null) {
                InputImage image = InputImage.fromBitmap(mSelectedImage, 0);
                BarcodeScanner scanner = BarcodeScanning.getClient();
                Task<List<Barcode>> result = scanner.process(image)
                        .addOnSuccessListener(barcodes -> {
                            for (Barcode barcode : barcodes) {
                                // Aquí puedes manejar los códigos de barras detectados
                                String barcodeValue = barcode.getRawValue();
                                txtResults.setText("Código detectado: " + barcodeValue);
                            }
                        })
                        .addOnFailureListener(e -> {
                            // Manejar errores de detección aquí
                            txtResults.setText("Error al detectar código: " + e.getMessage());
                        });
            }
        }
    }

    public ArrayList<String> getPermisosNoAprobados(ArrayList<String>  listaPermisos) {
        ArrayList<String> list = new ArrayList<String>();
        Boolean habilitado;


        if (Build.VERSION.SDK_INT >= 23)
            for(String permiso: listaPermisos) {
                if (checkSelfPermission(permiso) != PackageManager.PERMISSION_GRANTED) {
                    list.add(permiso);
                    habilitado = true;
                }else
                    habilitado=true;

                if(permiso.equals(Manifest.permission.CAMERA))
                    btnCamara.setEnabled(habilitado);
                else if (permiso.equals(Manifest.permission.MANAGE_EXTERNAL_STORAGE)  ||
                        permiso.equals(Manifest.permission.READ_EXTERNAL_STORAGE))
                    btnGaleria.setEnabled(habilitado);
            }


        return list;
    }

}