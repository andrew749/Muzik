package com.acod.play.app.Activities;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.util.Log;
import android.widget.SearchView;
import android.widget.Toast;

import com.acod.play.app.Interfaces.DataTransmission;
import com.acod.play.app.Interfaces.updateui;
import com.acod.play.app.R;
import com.acod.play.app.Searching.RecentSearchSuggestionProvider;
import com.acod.play.app.Searching.SearchSite;
import com.acod.play.app.fragments.ResultsFragment;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

/**
 * Created by andrew on 03/07/14.
 */
public class SearchActivity extends SherlockActivity implements DataTransmission, updateui {
    ProgressDialog pd;
    ResultsFragment resultsFragment;
    String query;
    SearchView sv;
    SearchSite search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.searchview);
        AdView adView = (AdView) this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        resultsFragment = (ResultsFragment) getFragmentManager().findFragmentById(R.id.resultsFrag);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Log.d("Play", "SearchActivity Started");
        handleIntent(getIntent());

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!HomescreenActivity.checkNetworkState(this))
            Toast.makeText(this, "Check your internet connection", Toast.LENGTH_LONG).show();
        if (!(this.query == null) && !(sv == null))
            sv.setQuery(this.query, false);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        search.cancel(true);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.searchmenu, menu);
        sv = (SearchView) menu.findItem(R.id.search).getActionView();
        SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        sv.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                startActivity(new Intent(getApplicationContext(), SearchActivity.class).putExtra(SearchManager.QUERY, s).setAction("android.intent.action.SEARCH"));

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        sv.setIconifiedByDefault(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void openPlayer(Bundle data) {
        if (HomescreenActivity.debugMode) {
            Log.d("PLAY", "Opening player");
        }
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("data", data);
        startActivity(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            this.query = query;
            SearchRecentSuggestions suggestionsProvider = new SearchRecentSuggestions(getApplicationContext(), RecentSearchSuggestionProvider.AUTHORITY, RecentSearchSuggestionProvider.MODE);
            suggestionsProvider.saveRecentQuery(query, null);
            search = new SearchSite(query, getApplicationContext(), this);
            search.execute();

        }
    }

    @Override
    public void openResultsFragment(Bundle results) {
        resultsFragment.setResults(results);


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
}
