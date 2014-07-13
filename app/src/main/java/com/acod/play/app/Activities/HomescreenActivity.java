package com.acod.play.app.Activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;

import com.acod.play.app.R;
import com.acod.play.app.fragments.HomeFragment;
import com.acod.play.app.services.MediaService;
import com.google.analytics.tracking.android.EasyTracker;


/**
 * @author Andrew Codispoti
 *         This is the main activtiy that will contain the vairous fragments and also do all of the searching system wide.
 */
public class HomescreenActivity extends Activity {
    public static final String PLAY_ACTION = "com.acod.play.playmusic";
    public static final String PAUSE_ACTION = "com.acod.play.pausemusic";
    public static final String STOP_ACTION = "com.acod.play.stopmusic";
    public static final boolean debugMode = false;
    FragmentManager manager;
    FragmentTransaction fragmentTransaction;
    Bundle b;
    ProgressDialog pd, resultsProgressDialog;
    MediaService service;
    SearchView searchView;

    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private String[] drawertitles = {"Top Hits", "My Songs", "Share on Twitter", "Share on Facebook"};


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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_homescreen);
        //put the homescreen view into place
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.left_drawer);
        drawerList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, drawertitles));
        drawerList.setOnItemClickListener(new DrawerItemClickListener());
        manager = getFragmentManager();
        fragmentTransaction = manager.beginTransaction();
        HomeFragment frag = new HomeFragment();
        fragmentTransaction.add(R.id.content_frame, frag).addToBackStack(null);
        fragmentTransaction.commit();

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.homescreen, menu);
        SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                startActivity(new Intent(getApplicationContext(), SearchActivity.class).putExtra(SearchManager.QUERY, s).setAction("android.intent.action.SEARCH"));
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        searchView.setIconifiedByDefault(false);
        return true;

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem item = (MenuItem) menu.findItem(R.id.donate);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                //send to page to buy in app purchase for server
                return false;
            }
        });
        return super.onPrepareOptionsMenu(menu);
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
                    startActivity(new Intent(getApplicationContext(), SavedActivity.class));
                    break;
            }
        }
    }

}
