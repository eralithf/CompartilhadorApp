package com.example.compartilhadorapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    EditText editTexto;
    Button btnCompartilharTexto, btnEnviarArquivos, btnCamera;
    ImageView imageView;

    static final int REQUEST_CAMERA = 1;
    static final int REQUEST_PERMISSOES = 100;
    Uri photoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTexto = findViewById(R.id.editTexto);
        btnCompartilharTexto = findViewById(R.id.btnCompartilharTexto);
        btnEnviarArquivos = findViewById(R.id.btnEnviarArquivos);
        btnCamera = findViewById(R.id.btnCamera);
        imageView = findViewById(R.id.imageView);


        solicitarPermissoes();


        btnCompartilharTexto.setOnClickListener(v -> {
            String texto = editTexto.getText().toString();
            if (!texto.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, texto);
                startActivity(Intent.createChooser(intent, "Compartilhar com:"));
            } else {
                Toast.makeText(this, "Digite algo para compartilhar.", Toast.LENGTH_SHORT).show();
            }
        });


        btnEnviarArquivos.setOnClickListener(v -> {
            ArrayList<Uri> uris = new ArrayList<>();
            File dir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "");
            File[] files = dir.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    Uri uri = FileProvider.getUriForFile(
                            this,
                            getPackageName() + ".fileprovider",
                            file
                    );
                    uris.add(uri);
                }

                Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                intent.setType("image/*");
                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                startActivity(Intent.createChooser(intent, "Compartilhar imagens:"));
            } else {
                Toast.makeText(this, "Nenhuma imagem encontrada.", Toast.LENGTH_SHORT).show();
            }
        });


        btnCamera.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(getPackageManager()) != null) {
                File photoFile = createImageFile();
                if (photoFile != null) {
                    photoUri = FileProvider.getUriForFile(
                            this,
                            getPackageName() + ".fileprovider",
                            photoFile
                    );
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else {
                    Toast.makeText(this, "Erro ao criar arquivo da imagem", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private File createImageFile() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "IMG_" + timeStamp;
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            return File.createTempFile(fileName, ".jpg", storageDir);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Exibir imagem capturada
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK && photoUri != null) {
            imageView.setImageBitmap(BitmapFactory.decodeFile(photoUri.getPath()));
        }
    }


    private void solicitarPermissoes() {
        String[] permissoes = {
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };

        ArrayList<String> permissoesNegadas = new ArrayList<>();
        for (String permissao : permissoes) {
            if (ContextCompat.checkSelfPermission(this, permissao) != PackageManager.PERMISSION_GRANTED) {
                permissoesNegadas.add(permissao);
            }
        }

        if (!permissoesNegadas.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissoesNegadas.toArray(new String[0]),
                    REQUEST_PERMISSOES);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }
}
