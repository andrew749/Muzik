package com.acod.play.app;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Andrew on 6/12/2014.
 */
public class Player extends Fragment implements View.OnClickListener {
    MediaPlayer player;
    private Runnable updatebar = new Runnable() {
        @Override
        public void run() {
            if (player != null) {
                int mCurrentPosition = player.getCurrentPosition();
                seek.setProgress(mCurrentPosition);
                currentTime.setText(milliSecondsToTimer(mCurrentPosition));
            }
            handler.postDelayed(this, 1000);
        }
    };
    ImageButton play, pause, stop;
    TextView songName, currentTime, totalTime;
    ImageView albumart;
    SeekBar seek;
    Bundle b;
    boolean isPrepared = false;
    private android.os.Handler handler = new android.os.Handler();

    public Player(Bundle b) {
        this.b = b;
    }

    public String milliSecondsToTimer(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.player, null);
        Bundle data = b;
        songName = (TextView) v.findViewById(R.id.nameText);
        totalTime = (TextView) v.findViewById(R.id.totalTime);
        currentTime = (TextView) v.findViewById(R.id.currentTime);
        seek = (SeekBar) v.findViewById(R.id.seekBar);
        albumart = (ImageView) v.findViewById(R.id.albumArt);

        songName.setText(data.getString("name"));
        player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        Uri uri = Uri.parse(data.getString("url"));
        try {
            player.setDataSource(getActivity().getApplicationContext(), uri);
            player.prepare();
            seek.setMax(player.getDuration());
            totalTime.setText(milliSecondsToTimer(player.getDuration()));

        } catch (IOException e) {
            e.printStackTrace();
        }
        play = (ImageButton) v.findViewById(R.id.play_button);
        play.setOnClickListener(this);
        pause = (ImageButton) v.findViewById(R.id.pause_button);
        pause.setOnClickListener(this);
        stop = (ImageButton) v.findViewById(R.id.stop_button);
        stop.setOnClickListener(this);

        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                isPrepared = true;
            }
        });

        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) player.seekTo(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        findInfo info = new findInfo();
        info.execute();
        return v;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.play_button:
                if (isPrepared) {
                    player.start();
                    handler.postDelayed(updatebar, 1000);
                }
                break;
            case R.id.stop_button:
                player.stop();
                getFragmentManager().popBackStack();

//remove fragment
                break;
            case R.id.pause_button:
                player.pause();
                break;
        }
    }

    class findInfo extends AsyncTask<Void, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Void... voids) {
            URL url = null;
            Uri imageuri = null;
            BufferedReader reader;
            Bitmap b = null;
            String urlb = null;
            try {
                url = new URL("https://ajax.googleapis.com/ajax/services/search/images?v=1.0&q=%22" + Uri.encode(songName.getText().toString()) + "%22&rsz=8");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            //TODO insert code to query google for album art and set to imageuri
            URLConnection urlConnection;
            if (url != null) {
                try {
                    urlConnection = url.openConnection();
                    InputStream io = new BufferedInputStream(urlConnection.getInputStream());
                    reader = new BufferedReader(new InputStreamReader(io));
                    StringBuilder responseStrBuilder = new StringBuilder();
                    String i;
                    while ((i = reader.readLine()) != null)
                        responseStrBuilder.append(i);

                    JSONObject json = new JSONObject(responseStrBuilder.toString());
                    JSONObject object = json.getJSONObject("responseData");
                    JSONArray subobject = object.getJSONArray("results");
                    urlb = subobject.getJSONObject(0).getString("url");
                    Log.d("Play", "album image:" + imageuri);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    URL urla = new URL(urlb);
                    b = BitmapFactory.decodeStream(urla.openConnection().getInputStream());

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return b;
        }

        @Override
        protected void onPostExecute(Bitmap bm) {
            super.onPostExecute(bm);
            albumart.setImageBitmap(bm);
        }
    }
}
