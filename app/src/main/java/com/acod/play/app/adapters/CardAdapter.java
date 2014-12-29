package com.acod.play.app.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.acod.play.app.Models.Song;
import com.acod.play.app.R;
import com.google.android.gms.common.api.BaseImplementation;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Andrew on 7/1/2014.
 */
public class CardAdapter extends BaseAdapter {
    TextView songName, artistName;
    ImageView albumArt;
    Context context;
    private ArrayList<Song> songs;

    public CardAdapter(ArrayList<Song> songs, Context context) {
        this.songs = songs;
        this.context = context;
        //inflate the cardlayout with the given information

    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int i) {
        return songs.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.songcard, null);

            TextView name = (TextView) view.findViewById(R.id.card_songname);
            TextView artist = (TextView) view.findViewById(R.id.card_artist);
            ImageView albumArt = (ImageView) view.findViewById(R.id.card_albumimage);
            name.setText(songs.get(i).getSongName());
            artist.setText(songs.get(i).getArtist());
            AlbumArtLoader l = new AlbumArtLoader(songs.get(i).getImageUrl(), albumArt);
            l.execute();
        }
            return view;
        }

 class AlbumArtLoader extends AsyncTask<Void,Void,Bitmap>{
     String imageurl;
     ImageView art;
     public AlbumArtLoader(String url,ImageView art) {
         imageurl=url;
         this.art=art;
     }

     @Override
     protected Bitmap doInBackground(Void... params) {
         Bitmap image=null;
         try {
             image = BitmapFactory.decodeStream(new URL(imageurl).openConnection().getInputStream());
         } catch (IOException e) {
             e.printStackTrace();
         }
         if (image ==null) image = BitmapFactory.decodeResource(context.getResources(), R.drawable.musicimage);

         return image;
     }

     @Override
     protected void onPostExecute(Bitmap bitmap) {
         super.onPostExecute(bitmap);
         art.setImageBitmap(bitmap);
     }
 }
}
