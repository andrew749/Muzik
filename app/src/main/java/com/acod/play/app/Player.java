package com.acod.play.app;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
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
public class Player extends Fragment implements View.OnClickListener, MediaService.ready {
    ImageButton play, pause, stop;
    TextView songName, currentTime, totalTime;
    ImageView albumart;
    SeekBar seek;
    Bundle b, data;
    Intent sintent;
    MediaService service;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MediaService.LocalBinder binder = (MediaService.LocalBinder) iBinder;
            service = binder.getService();


        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };
    private Runnable updatebar = new Runnable() {
        @Override
        public void run() {
            int mCurrentPosition = (int) service.getCurrentTime();
            seek.setProgress(mCurrentPosition);
            currentTime.setText(milliSecondsToTimer(mCurrentPosition));

            handler.postDelayed(this, 1000);
        }
    };
    private android.os.Handler handler = new android.os.Handler();

    //gets data from calling class with song information
    public Player(Bundle b) {
        this.b = b;
    }

    //convert the given song time in milleseconds to a readable string.
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
        data = b;
        createUI(v);

        Uri uri = Uri.parse(data.getString("url"));


        sintent = new Intent(getActivity(), MediaService.class);
        sintent.putExtra("data", data);
        getActivity().bindService(sintent, mConnection, Context.BIND_AUTO_CREATE);
        getActivity().startService(sintent);


        //find album art
        findInfo info = new findInfo();
        info.execute();
        return v;
    }

    public void createUI(View v) {
        //ui elements
        songName = (TextView) v.findViewById(R.id.nameText);
        totalTime = (TextView) v.findViewById(R.id.totalTime);
        currentTime = (TextView) v.findViewById(R.id.currentTime);
        seek = (SeekBar) v.findViewById(R.id.seekBar);
        albumart = (ImageView) v.findViewById(R.id.albumArt);
        songName.setText(data.getString("name"));
        play = (ImageButton) v.findViewById(R.id.play_button);
        play.setOnClickListener(this);
        pause = (ImageButton) v.findViewById(R.id.pause_button);
        pause.setOnClickListener(this);
        stop = (ImageButton) v.findViewById(R.id.stop_button);
        stop.setOnClickListener(this);
    }

    /*Handle ui button clicks*/
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.play_button:
                if (service.ready) {
                    service.play();
                    mediaReady();
                    handler.postDelayed(updatebar, 1000);
                    //call method on service
                }
                break;
            case R.id.stop_button:
                service.stop();
                getActivity().stopService(sintent);
                handler.removeCallbacks(updatebar);
                getFragmentManager().popBackStack();
//                call method on service

//remove fragment
                break;
            case R.id.pause_button:
                service.pause();
                break;
        }
    }

    @Override
    public void mediaReady() {

        totalTime.setText(milliSecondsToTimer(service.getMaxTime()));
        seek.setMax(service.getMaxTime());
        //handle changing the position of a song.
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) service.seekPlayer(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    /*
//    * Get album art for an album be searching Google.
    **/
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
