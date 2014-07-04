package com.acod.play.app.Activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import com.acod.play.app.Interfaces.DataTransmission;
import com.acod.play.app.R;
import com.acod.play.app.fragments.ResultsFragment;

/**
 * Created by andrew on 03/07/14.
 */
public class SearchActivity extends Activity implements DataTransmission {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.searchview);
        ResultsFragment resultsFragment = (ResultsFragment) getFragmentManager().findFragmentById(R.id.resultsFrag);
        resultsFragment.setResults(getIntent().getBundleExtra("a"));

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
        Log.d("PLAY", "Opening player");
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("data", data);
        startActivity(intent);
    }


}
