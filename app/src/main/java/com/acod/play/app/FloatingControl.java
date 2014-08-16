package com.acod.play.app;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.acod.play.app.Activities.HomescreenActivity;

/**
 * Created by Andrew on 8/1/2014.
 */
public class FloatingControl implements View.OnLongClickListener, View.OnTouchListener {
    ImageView albumArt;
    WindowManager manager;

    boolean openState = false;
    boolean viewExists = false;
    Context context;
    WindowManager.LayoutParams params, controlparams;
    View controlsview;
    DisplayMetrics metrics;
    boolean editable = false;
    int x, y;

    public FloatingControl(Bitmap bm, Context context) {
        this.context = context;
        albumArt = new ImageView(context);
        albumArt.setImageBitmap(bm);
        albumArt.setOnLongClickListener(this);
        albumArt.setOnTouchListener(this);
        manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        metrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(metrics);

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;
        x = 0;
        y = 100;
        params.width = 200;
        params.height = 200;
        controlparams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        controlparams.x = (int) albumArt.getX() + params.width;

        controlparams.gravity = Gravity.TOP | Gravity.LEFT;

        controlparams.y = y;

        controlparams.height = params.height;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        controlsview = inflater.inflate(R.layout.popupcontrols, null);
    }

    public void displayControl() {

        manager.addView(albumArt, params);
        viewExists = true;
    }


    public void destroyView() {
        manager.removeView(albumArt);
        if (openState) {
            manager.removeView(controlsview);
            controlsview = null;
        }
        viewExists = false;
    }

    public boolean viewExists() {
        return viewExists;
    }

    //true is the right side and false is left
    public boolean whichSide(DisplayMetrics metrics) {
        int midway = metrics.widthPixels / 2;

        if ((this.x + albumArt.getWidth()) >= midway) {
            //more likely right but check if there is room
            if (metrics.widthPixels - (this.x + albumArt.getWidth()) < controlparams.width) {
                return true;
            }
            return false;
        } else {

            return true;
        }
    }

    public void handleClick() {
        //if the controls is not open

        if (!openState) {
            //open the control panel


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

    //method to move the specified view
    public void move(float x, float y) {
        this.x = (int) x - (albumArt.getWidth() / 2);
        this.y = (int) y - (albumArt.getHeight() / 2);
        params.x = this.x;
        params.y = this.y;
        albumArt.setLayoutParams(params);
        if (openState) {
            manager.removeView(controlsview);
            openState = false;
        }
        manager.updateViewLayout(albumArt, params);

    }

    @Override
    public boolean onLongClick(View view) {
        editable = true;
        return false;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if ((motionEvent.getAction() == MotionEvent.ACTION_MOVE) && editable) {
            //state when the long click
            move(motionEvent.getRawX(), motionEvent.getRawY());
        } else if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            handleClick();
        } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            editable = false;
            if (whichSide(metrics)) {
                controlparams.x = this.x + params.width;
            } else {
                controlparams.x = this.x - controlsview.getWidth();
            }
            controlparams.y = this.y;
        }

        return false;
    }
}
