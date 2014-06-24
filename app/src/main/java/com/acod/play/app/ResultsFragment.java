package com.acod.play.app;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Andrew on 6/14/2014.
 */
public class ResultsFragment extends Fragment {
    final private SearchView.OnQueryTextListener queryListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String s) {
            SearchSite results = new SearchSite(s);
            results.execute();
            return false;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            return false;
        }
    };
    ArrayList<SongResult> results = new ArrayList<SongResult>();
    ListView lv;
    SearchListAdapter adapter;
    MediaPlayer player;
    DataTransmission transmission;

    public ResultsFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.search_list, null);
        lv = (ListView) v.findViewById(R.id.searchlist);
        adapter = new SearchListAdapter(getActivity().getApplicationContext(), results);
        lv.setAdapter(adapter);
        player = new MediaPlayer();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Bundle b = new Bundle();
                b.putString("url", results.get(i).url);
                b.putString("name", results.get(i).name);
                //open the player via the activity
                transmission.openPlayer(b);

            }
        });
        return v;
    }

    public void showList() {
        Log.d("Play", results.get(0).name + results.get(1).name);
        adapter = new SearchListAdapter(getActivity().getApplicationContext(), results);

        lv.setAdapter(adapter);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        transmission = (DataTransmission) activity;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.homescreen, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setOnQueryTextListener(queryListener);
    }

    public interface DataTransmission {
        public void openPlayer(Bundle b);

    }

    class SearchSite extends AsyncTask<Void, Void, ArrayList<SongResult>> {
        private final HttpClient client = new DefaultHttpClient();
        String query;
        ProgressDialog pd;

        public SearchSite(String query) {
            this.query = query;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(getActivity());
            pd.setMessage("Loading Sources");
            pd.show();
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

        // encapsulate methods for each data source
        private void searchMP3Skull() {

        }

        @Override
        protected void onPostExecute(ArrayList<SongResult> songResults) {
            super.onPostExecute(songResults);
            pd.dismiss();
            Log.d("Play", "Done loading query");
            results = songResults;
            if (songResults.size() > 0)
                showList();
            else
                Toast.makeText(getActivity().getApplicationContext(), "Song not found", Toast.LENGTH_LONG).show();
        }
    }
}
