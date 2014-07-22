package com.acod.play.app.Activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.acod.play.app.R;
import com.acod.play.app.fragments.HomeFragment;
import com.acod.play.app.services.MediaService;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;
import com.inscription.ChangeLogDialog;


/**
 * @author Andrew Codispoti
 *         This is the main activtiy that will contain the vairous fragments and also do all of the searching system wide.
 */
public class HomescreenActivity extends SherlockActivity {
    public static final String PLAY_ACTION = "com.acod.play.playmusic";
    public static final String PAUSE_ACTION = "com.acod.play.pausemusic";
    public static final String STOP_ACTION = "com.acod.play.stopmusic";
    public static final boolean debugMode = false;
    public static float APP_VERSION = 1;
    FragmentManager manager;
    FragmentTransaction fragmentTransaction;
    Bundle b;
    ProgressDialog pd, resultsProgressDialog;
    MediaService service;
    SearchView searchView;
    Context c;
    HomescreenActivity a;
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private String[] drawertitles = {"My Songs", "Share on Twitter", "Share on Facebook"};
    private ActionBarDrawerToggle toggle;

    public static Intent getOpenFacebookIntent(Context context) {

        try {
            context.getPackageManager().getPackageInfo("com.facebook.katana", 0);
            return new Intent(Intent.ACTION_VIEW, Uri.parse("fb://page/296814327167093"));
        } catch (Exception e) {
            return new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/pages/Play/296814327167093"));
        }
    }

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
        c = this;

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.left_drawer);
        drawerList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, drawertitles));
        drawerList.setOnItemClickListener(new DrawerItemClickListener());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer, R.string.opendrawer, R.string.app_name);
        drawerLayout.setDrawerListener(toggle);


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
        switch (item.getItemId()) {
            case android.R.id.home:
                if (!drawerLayout.isDrawerOpen(Gravity.LEFT))
                    drawerLayout.openDrawer(Gravity.LEFT);
                else drawerLayout.closeDrawer(Gravity.LEFT);
                break;
            case R.id.changes:
                ChangeLogDialog dialog = new ChangeLogDialog(c);
                dialog.show();
                break;
            case R.id.report:
                startEmailIntent();
                break;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggle.onConfigurationChanged(newConfig);
    }

    public void startEmailIntent() {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{"andrewcod749@gmail.com"});
        i.putExtra(Intent.EXTRA_SUBJECT, "Play (Android)");
        i.putExtra(Intent.EXTRA_TEXT, "");
        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.homescreen, menu);
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
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggle.syncState();
    }

    public void handleTwitter() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("twitter://user?screen_name=andrewcod749"));
            startActivity(intent);

        } catch (Exception e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://twitter.com/#!/andrewcod749")));
        }
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            switch (i) {

                case 0:
                    //my songs section
                    startActivity(new Intent(getApplicationContext(), SavedActivity.class));
                    break;
                case 1:
                    //twitter list
                    handleTwitter();
                    break;
                case 2:
                    //facebook page
                    startActivity(getOpenFacebookIntent(getApplicationContext()));
                    break;
            }
        }
    }

}
