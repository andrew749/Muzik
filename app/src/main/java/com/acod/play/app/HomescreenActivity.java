package com.acod.play.app;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.SearchRecentSuggestions;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;

import com.acod.play.app.fragments.HomeFragment;
import com.google.analytics.tracking.android.EasyTracker;


/**
 * @author Andrew Codispoti
 *         This is the main activtiy that will contain the vairous fragments and also do all of the searching system wide.
 */
public class HomescreenActivity extends ActionBarActivity implements ResultsFragment.DataTransmission, PlayerCommunication, updateui {
    protected static final String PLAY_ACTION = "com.acod.play.playmusic";
    protected static final String PAUSE_ACTION = "com.acod.play.pausemusic";
    protected static final String STOP_ACTION = "com.acod.play.stopmusic";
    FragmentManager manager;
    FragmentTransaction fragmentTransaction;
    Bundle b;
    ProgressDialog pd;

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
    private updateui update;
    final private SearchView.OnQueryTextListener queryListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String s) {
            SearchSite results = new SearchSite(s, getApplicationContext(), update);
            SearchRecentSuggestions suggestionsProvider = new SearchRecentSuggestions(getApplicationContext(), RecentSearchSuggestionProvider.AUTHORITY, RecentSearchSuggestionProvider.MODE);
            suggestionsProvider.saveRecentQuery(s, null);
            results.execute();
            return false;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            return false;
        }
    };
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private String[] drawertitles = {"Top Hits", "My Songs", "Share on Twitter", "Share on Facebook"};
    private boolean playerPresent = false;

    @Override
    protected void onDestroy() {

        if (!(service == null)) {
            service.stop();
        }
        super.onDestroy();
    }

    //TODO get analytics working
    //TODO implement admob
    @Override
    protected void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this); // Add this method.

    }

    @Override
    protected void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this); // Add this method.

    }

    //TODO fix rotation
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (playerPresent && newConfig.equals(Configuration.ORIENTATION_LANDSCAPE)) {
            fragmentTransaction = manager.beginTransaction();
            Player p = new Player();
            fragmentTransaction.replace(R.id.content_frame, p);
            fragmentTransaction.commit();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homescreen);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.left_drawer);
        drawerList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, drawertitles));
        drawerList.setOnItemClickListener(new DrawerItemClickListener());
        manager = getFragmentManager();
        fragmentTransaction = manager.beginTransaction();
        HomeFragment frag = new HomeFragment();
        fragmentTransaction.add(R.id.content_frame, frag).addToBackStack(null);
        fragmentTransaction.commit();
        update = this;
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
        if (id == R.id.donate) {
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
        playerPresent = true;
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if (!menu.hasVisibleItems()) {
            inflater.inflate(R.menu.homescreen, menu);
        }
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setOnQueryTextListener(queryListener);
        MenuItem item = (MenuItem) menu.findItem(R.id.donate);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                //send to page to buy in app purchase for server
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public void openResultsFragment(Bundle bundle) {
        ResultsFragment fragment = new ResultsFragment();
        fragment.setArguments(bundle);
        fragmentTransaction = manager.beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack(null);
        fragmentTransaction.commit();

    }

    @Override
    public void openProgressDialog() {
        pd = new ProgressDialog(this);
        pd.setMessage("Loading Sources");
        pd.show();
    }

    @Override
    public void closeProgressDialog() {
        pd.dismiss();

    }


    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            switch (i) {
                case 0:
                    //top hits section
                    break;
                case 1:
                    //my songs section
                    break;
            }
        }
    }

}
