package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Mdl.UploadSong;

import java.io.IOException;
import java.util.List;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.SongsAdapterViewHolder> {

    ImageButton download, del;
    //Todo:Добавить кнопки удаления скачивания для песни
    Context context;
    List<UploadSong> arrayListSongs;

    public SongsAdapter(Context context,List<UploadSong> arrayListSongs){
        this.context = context;
        this.arrayListSongs = arrayListSongs;

    }

    @NonNull
    @Override
    public SongsAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.song_item, parent,false);
        return new SongsAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongsAdapterViewHolder holder, int position) {
        UploadSong song = arrayListSongs.get(position);
        holder.title.setText(song.getTitle());
        holder.duration.setText(song.getSongDuration());
    }

    @Override
    public int getItemCount() {
        return arrayListSongs.size();
    }

    public class SongsAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView title, duration;
        public SongsAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.song_title);
            duration = itemView.findViewById(R.id.song_duration);
            download = itemView.findViewById(R.id.download);
            download.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MusicListActivity) context).download(arrayListSongs,getAdapterPosition());
                }
            });
            del = itemView.findViewById(R.id.delete);
            del.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MusicListActivity) context).del(arrayListSongs,getAdapterPosition());
                }
            });
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            try {
                ((MusicListActivity) context).playSong(arrayListSongs, getAdapterPosition());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
