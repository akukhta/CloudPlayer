package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.lang.NullPointerException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.example.myapplication.Mdl.UploadSong;
import com.firebase.client.Firebase;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

public class SongSelectActivity extends AppCompatActivity {

    Uri audioUri;
    TextView file_path;
    EditText file_name;
    ProgressBar progressBar;
    StorageReference storageRef;
    FirebaseDatabase db;
    DatabaseReference reference;
    StorageTask uploadTask;
    Button btn1;
    Button btn2;
    String user_id;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_select);
        file_path = findViewById(R.id.filepath);
        file_name =  findViewById(R.id.songname);
        progressBar = findViewById(R.id.progressBar);
        user = mAuth.getInstance().getCurrentUser();
        user_id = user.getUid();
        db = FirebaseDatabase.getInstance();
        reference = db.getReference().child("Users").child(user.getUid()).child("songs");
        storageRef = FirebaseStorage.getInstance().getReference().child("songs");
        btn1 = findViewById(R.id.select_song);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAudio(v);
            }
        });
        btn2 = findViewById(R.id.send);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              uploadFile(v);
            }
        });
    }


    public void getAudio(View v){
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("audio/*");
        startActivityForResult(i,101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data.getData() != null) {
            audioUri = data.getData();
            String filename = getFileName(audioUri);
            file_name.setText(filename);
            file_path.setText(filename);

        }
    }

    private String getFileName(Uri audioUri) {
        String result = null;
        if(audioUri.getScheme().equals("content")){
            Cursor cursor = getContentResolver().query(audioUri,null,null,null,null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }


        if (result == null){
            result = audioUri.getPath();
            int cut = result.lastIndexOf('/');

            if (cut != -1)
                result = result.substring(cut + 1);
        }

        return result;
    };



    public void uploadFile(View v){
        if (file_path.getText().equals("Файл не выбран")){
            Toast.makeText(getApplicationContext(),"Файл для отправки не выбран!", Toast.LENGTH_LONG).show();
        }

        else{

            if (uploadTask != null && uploadTask.isInProgress())
                Toast.makeText(getApplicationContext(),"Загрузка в процессе...",Toast.LENGTH_LONG).show();
            else
                upload();
        }

    }

    private void upload() {

        if (audioUri != null){
            String duration;
            Toast.makeText(getApplicationContext(),"Загрузка файла\nПожалуйста,подождите",Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.VISIBLE);
            final StorageReference storageReference = storageRef.child(System.currentTimeMillis() + "." + getFileExtention(audioUri));
            int dur = songDuration(audioUri);
            Toast.makeText(getApplicationContext(),audioUri.toString(),Toast.LENGTH_LONG).show();
            if (dur == 0)
                duration = "N/A";
            else
                duration = msToTime(dur);
            final String finalDuration1 = duration;

            uploadTask = storageReference.putFile(audioUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            UploadSong sng = new UploadSong(file_name.getText().toString(), finalDuration1,uri.toString());
                            String key = reference.push().getKey();
                            sng.setKey(key);
                            reference.child(key).setValue(sng);
                        }
                    });
                    Toast.makeText(getApplicationContext(),"Готово!",Toast.LENGTH_SHORT).show();
                    progressBar.setProgress(0);
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                   double progress = (100.0 *  taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    progressBar.setProgress((int)progress);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(),"Ошибка отправки!",Toast.LENGTH_LONG).show();
                }
            });
        }
        else
            Toast.makeText(getApplicationContext(),"Файл для отправки не выбран!",Toast.LENGTH_LONG).show();
    }

    private String msToTime(int dur) {
        Date date = new Date(dur);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());
        String myTime = simpleDateFormat.format(date);
        return myTime;
    }

    private int songDuration(Uri audioUri) {
        int timems = 0;
        try{
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(this,audioUri);
            String _time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            timems = Integer.parseInt(_time);
            retriever.release();
            return timems;
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    private String getFileExtention(Uri audioUri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(audioUri));
    }
}
