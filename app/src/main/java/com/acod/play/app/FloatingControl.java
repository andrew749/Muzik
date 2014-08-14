package com.acod.play.app;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.acod.play.app.Activities.HomescreenActivity;

/**
 * Created by Andrew on 8/1/2014.
 */
public class FloatingControl implements View.OnClickListener {
    ImageView albumArt;
    WindowManager manager;
    boolean openState = false;
    boolean viewExists = false;
    Context context;
    WindowManager.LayoutParams params, controlparams;
    View controlsview;

    public FloatingControl(Bitmap bm, Context context) {
        this.context = context;
        albumArt = new ImageView(context);
        albumArt.setImageBitmap(bm);
        albumArt.setOnClickListener(this);

        manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);


        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;
        params.width = 200;
        params.height = 200;
        controlparams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        controlparams.gravity = Gravity.TOP | Gravity.LEFT;
        controlparams.x = params.width;
        controlparams.y = 100;
        controlparams.height = params.height;
    }

    public void displayControl() {

        manager.addView(albumArt, params);
        viewExists = true;
    }


    public void destroyView() {
        manager.removeView(albumArt);
        if (!(controlsview == null)) {
            manager.removeView(controlsview);
        }
        viewExists = false;
    }

    public boolean viewExists() {
        return viewExists;
    }

    @Override
    public void onClick(View view) {
        //if the controls is not open

        if (!openState) {
            //open the control panel

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            controlsview = inflater.inflate(R.layout.popupcontrols, null);
            manager.addView(controlsview, controlparams);

            setupControls(controlsview);
            openState = true;
        } else {
            //remove the controls
            manager.removeView(controlsview);
            openState = false;
        }
    }

    public void changeImage(Bitmap bm) {
        albumArt.setImageBitmap(bm);
    }

    public void setupControls(View controls) {
        final PendingIntent stopIntent = PendingIntent.getBroadcast(context, 0, new Intent().setAction(HomescreenActivity.STOP_ACTION), 0);
        final PendingIntent pauseIntent = PendingIntent.getBroadcast(context, 0, new Intent().setAction(HomescreenActivity.PAUSE_ACTION), 0);
        final PendingIntent playIntent = PendingIntent.getBroadcast(context, 0, new Intent().setAction(HomescreenActivity.PLAY_ACTION), 0);
        controls.findViewById(R.id.play_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    playIntent.send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            }
        });
        controls.findViewById(R.id.stop_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    stopIntent.send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            }
        });
        controls.findViewById(R.id.pause_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    pauseIntent.send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
