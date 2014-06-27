package com.acod.play.app;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;

/**
 * @author Andrew Codispoti
 *         This is the main activtiy that will contain the vairous fragments and also do all of the searching system wide.
 */
public class HomescreenActivity extends ActionBarActivity implements ResultsFragment.DataTransmission, PlayerCommunication {
    protected static final String PLAY_ACTION = "com.acod.play.playmusic";
    protected static final String PAUSE_ACTION = "com.acod.play.pausemusic";
    protected static final String STOP_ACTION = "com.acod.play.stopmusic";
    FragmentManager manager;
    FragmentTransaction fragmentTransaction;
    Bundle b;
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
    Intent sintent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homescreen);
        manager = getFragmentManager();
        fragmentTransaction = manager.beginTransaction();
        ResultsFragment fragment = new ResultsFragment();
        fragmentTransaction.add(R.id.content_frame, fragment);
        fragmentTransaction.commit();
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                manager.popBackStack();
            }
        }, new IntentFilter(HomescreenActivity.STOP_ACTION));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void openPlayer(Bundle data) {
        Log.d("PLAY", "Opening player");
        sintent = new Intent(this, MediaService.class);
        sintent.putExtra("data", data);
        bindService(sintent, mConnection, Context.BIND_AUTO_CREATE);
        startService(sintent);
        Player p = new Player();
        p.setArguments(data);
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.content_frame, p).addToBackStack(null);
        transaction.commit();

    }

    @Override
    public void play() {
        service.play();
    }

    @Override
    public void pause() {
        service.pause();
    }

    @Override
    public void stop() {
        service.stop();
    }


}
