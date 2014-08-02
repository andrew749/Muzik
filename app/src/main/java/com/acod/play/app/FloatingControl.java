package com.acod.play.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.ImageView;

/**
 * Created by Andrew on 8/1/2014.
 */
public class FloatingControl {
    ImageView albumArt;
    WindowManager manager;

    public FloatingControl(Bitmap bm, Context context) {
        albumArt = new ImageView(context);
        albumArt.setImageBitmap(bm);
        manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    public void displayControl() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;
        manager.addView(albumArt, params);
    }

    public void destroyView() {
        manager.removeViewImmediate(albumArt);
    }
}
