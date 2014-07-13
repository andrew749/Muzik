package com.acod.play.app.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.util.Log;
import android.view.Menu;

import com.acod.play.app.Interfaces.DataTransmission;
import com.acod.play.app.Interfaces.updateui;
import com.acod.play.app.R;
import com.acod.play.app.Searching.RecentSearchSuggestionProvider;
import com.acod.play.app.Searching.SearchSite;
import com.acod.play.app.fragments.ResultsFragment;

/**
 * Created by andrew on 03/07/14.
 */
public class SearchActivity extends Activity implements DataTransmission, updateui {
    ProgressDialog pd;
    ResultsFragment resultsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.searchview);
        resultsFragment = (ResultsFragment) getFragmentManager().findFragmentById(R.id.resultsFrag);

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
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
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
            SearchRecentSuggestions suggestionsProvider = new SearchRecentSuggestions(getApplicationContext(), RecentSearchSuggestionProvider.AUTHORITY, RecentSearchSuggestionProvider.MODE);
            suggestionsProvider.saveRecentQuery(query, null);
            SearchSite search = new SearchSite(query, getApplicationContext(), this);
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
