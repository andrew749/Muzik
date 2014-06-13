package com.acod.play.app;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Andrew on 6/11/2014.
 */
public class SearchActivity extends Activity {
    ArrayList<SongResult> results = new ArrayList<SongResult>();
    ListView lv;
    SearchListAdapter adapter;
    MediaPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_list);
        Intent intent = getIntent();
        lv = (ListView) findViewById(R.id.searchlist);
        adapter = new SearchListAdapter(getApplicationContext(), results);
        lv.setAdapter(adapter);
        player = new MediaPlayer();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    Uri uri = Uri.parse(results.get(i).url);
                    player.setDataSource(getApplicationContext(), uri);
                    player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    player.prepare();
                    player.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            //handle search data
            String query = intent.getStringExtra(SearchManager.QUERY);
            // send string via get to multiple websites and get responses to display as array list
            SearchSite results = new SearchSite(query);
            results.execute();

        }

    }

    public void showList() {
        Log.d("PLay", results.get(0).name + results.get(1).name);
        adapter = new SearchListAdapter(getApplicationContext(), results);

        lv.setAdapter(adapter);
        Log.d("Play", "Loaded Adapter");
    }

    class SearchSite extends AsyncTask<Void, Void, ArrayList<SongResult>> {
        private final HttpClient client = new DefaultHttpClient();
        String query;

        public SearchSite(String query) {
            this.query = query;
        }

        /*
                parse the webstie and place the various song urls and names into a data object to be returned to the calling class
                 */
        @Override
        protected ArrayList<SongResult> doInBackground(Void... voids) {
            query = "http://mp3skull.com/mp3/" + query + ".html";
            ArrayList<SongResult> results = new ArrayList<SongResult>();
            String site = "";
            Elements searchresults = new Elements();
            try {
                Document document = Jsoup.connect(query).get();
                searchresults = document.select("div#song_html");

            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!searchresults.isEmpty()) {
                for (Element inputelement : searchresults) {
                    String name, duration, url;
                    name = inputelement.select("b").text();
                    url = inputelement.select("a[href]").first().attr("href");
                    Log.d("PLAY", "Found entry name=" + name + " url=" + url);
                    results.add(new SongResult(name, url));
                }
            }
            return results;
        }

        @Override
        protected void onPostExecute(ArrayList<SongResult> songResults) {
            super.onPostExecute(songResults);
            Log.d("Play", "Done loading query");
            results = songResults;
            showList();
        }
    }
}
