package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import com.example.myapplication.Mdl.UploadSong;
import com.firebase.client.Firebase;
import com.firebase.client.core.Constants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.os.FileUtils;
import android.provider.SyncStateContract;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MusicListActivity extends AppCompatActivity {

    public static final String LOGGIN = "false";
    RecyclerView recyclerView;
    ProgressBar progressBar;
    List<UploadSong> SongList;
    DatabaseReference reference;
    private FirebaseAuth auth;
    private FirebaseUser user;
    MediaPlayer player;
    SongsAdapter adapter;
    ImageButton song_select;
    ImageButton play_stop;
    ImageButton log_out;
    ImageButton help;
    ValueEventListener valueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);
        song_select = findViewById(R.id.imageButton3);
        song_select.setBackgroundResource(R.drawable.add);
        log_out = findViewById(R.id.imageButton4);
        log_out.setBackgroundResource(R.drawable.logout);
        log_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                log_off();
            }
        });
        help = findViewById(R.id.imageButton5);
        help.setBackgroundResource(R.drawable.help);
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHelp();
            }
        });
        //Todo: Сделать форму и обработчик события для показа помощи
        user = auth.getInstance().getCurrentUser();;
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBarShowSongs);
        play_stop = findViewById(R.id.play_stop);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        SongList = new ArrayList<>();
        adapter = new SongsAdapter(MusicListActivity.this,SongList);
        recyclerView.setAdapter(adapter);
        play_stop.setBackgroundResource(R.drawable.stop);
        play_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play_or_stop();
            }
        });
        song_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosefile();
            }
        });
        reference = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid()).child("songs");
        valueEventListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                SongList.clear();
                for (DataSnapshot dss:dataSnapshot.getChildren()){
                    UploadSong uploadSong = dss.getValue(UploadSong.class);
                    SongList.add(uploadSong);
                }
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(),"Ошибка:" +databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        reference.removeEventListener(valueEventListener);
    }

    void choosefile(){
            startActivity(new Intent(MusicListActivity.this, SongSelectActivity.class));
        }

    public void playSong(List<UploadSong> arrayListSongs, int adapterPosition) throws IOException {
        play_stop.setBackgroundResource(R.drawable.stop);
        UploadSong song = arrayListSongs.get(adapterPosition);

        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
        player = new MediaPlayer();
        player.setDataSource(song.getSongLink());
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });
        player.prepareAsync();
    }

    public void play_or_stop(){
        if (player != null) {
            if (player.isPlaying()){
                player.pause();
                play_stop.setBackgroundResource(R.drawable.play);
                return;
            }
            if (!player.isPlaying()){
                player.start();
                play_stop.setBackgroundResource(R.drawable.stop);
                return;
            }

        }
    }
    public void log_off(){
        auth = FirebaseAuth.getInstance();
        auth.signOut();
        SharedPreferences settings = getSharedPreferences(LOGGIN,0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("login",false);
        editor.commit();
        if (player != null && player.isPlaying())
            player.pause();
        startActivity(new Intent(MusicListActivity.this, MainActivity.class));
        finish();
    }

    public long download(List<UploadSong> arrayListSongs, int adapterPosition){

        UploadSong song = arrayListSongs.get(adapterPosition);
        DownloadManager downloadManager = (DownloadManager) getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(song.getSongLink());
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,song.getTitle());
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        return downloadManager.enqueue(request);
    }

    public void del(List<UploadSong> arrayListSongs, int adapterPosition){
        final UploadSong song = arrayListSongs.get(adapterPosition);
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(song.songLink);
        storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getApplicationContext(),"Удалено",Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),"Ошибка удаления",Toast.LENGTH_SHORT).show();
            }
        });
        reference.child(song.getKey()).removeValue();
    }

    public void showHelp(){
        startActivity(new Intent(MusicListActivity.this, HelpActivity.class));
    }

}
