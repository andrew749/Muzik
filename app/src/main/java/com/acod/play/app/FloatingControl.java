package com.acod.play.app;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.acod.play.app.Activities.HomescreenActivity;
import com.acod.play.app.Interfaces.PlayerCommunication;

/**
 * Created by Andrew on 8/1/2014.
 */
public class FloatingControl implements View.OnLongClickListener, View.OnTouchListener, PlayerCommunication {
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
        metrics = new DisplayMetrics();
        manager = (WindowManager) context.getSystemService(Service.WINDOW_SERVICE);
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

        ((WindowManager) context.getSystemService(Service.WINDOW_SERVICE)).addView(albumArt, params);
        viewExists = true;
    }


    public void destroyView() {
        ((WindowManager) context.getSystemService(Service.WINDOW_SERVICE)).removeView(albumArt);
        if (openState) {
            ((WindowManager) context.getSystemService(Service.WINDOW_SERVICE)).removeView(controlsview);
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


            ((WindowManager) context.getSystemService(Service.WINDOW_SERVICE)).addView(controlsview, controlparams);

            setupControls();
            openState = true;
        } else {
            //remove the controls
            ((WindowManager) context.getSystemService(Service.WINDOW_SERVICE)).removeView(controlsview);
            openState = false;
        }
    }

    public void setState(boolean state) {
        if (state)
            ((ImageButton) controlsview.findViewById(R.id.control_button)).setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.pausebuttonblack));
        else
            ((ImageButton) controlsview.findViewById(R.id.control_button)).setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.playbuttonblack));

    }

    public void changeImage(Bitmap bm) {
        albumArt.setImageBitmap(bm);
    }

    public void setupControls() {
        final PendingIntent stopIntent = PendingIntent.getBroadcast(context, 0, new Intent().setAction(HomescreenActivity.STOP_ACTION), 0);

        final PendingIntent toggleIntent = PendingIntent.getBroadcast(context, 0, new Intent().setAction(HomescreenActivity.TOGGLE_ACTION), 0);
        controlsview.findViewById(R.id.control_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {

                    toggleIntent.send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            }
        });
        controlsview.findViewById(R.id.stop_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    stopIntent.send();
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


    @Override
    public void toggle() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void seek(int i) {

    }
}
