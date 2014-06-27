package com.acod.play.app;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
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
            SearchRecentSuggestions suggestionsProvider = new SearchRecentSuggestions(getActivity(), RecentSearchSuggestionProvider.AUTHORITY, RecentSearchSuggestionProvider.MODE);
            suggestionsProvider.saveRecentQuery(s, null);
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

    public void showList(ArrayList<SongResult> songResults) {
        Log.d("Play", songResults.get(0).name + songResults.get(1).name);
        adapter = new SearchListAdapter(getActivity().getApplicationContext(), songResults);

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
        if (!menu.hasVisibleItems()) {
            inflater.inflate(R.menu.homescreen, menu);
        }
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
            ArrayList<SongResult> allResults = new ArrayList<SongResult>();
            allResults.addAll(searchMP3Skull());
//            searchBeeMP3();
            return allResults;
        }

        //query mp3skull and return an arraylist with all of the results
        //working fully
        private ArrayList<SongResult> searchMP3Skull() {
            String tempquery = "http://mp3skull.com/mp3/" + query + ".html";
            ArrayList<SongResult> tempresults = new ArrayList<SongResult>();
            Elements searchresults = new Elements();
            try {
                Document document = Jsoup.connect(tempquery).get();
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
                    tempresults.add(new SongResult(name, url));
                }
            }
            return tempresults;
        }

        //query beemp3 and return an array list with all of the results
        //not working and query is slow
        private ArrayList<SongResult> searchBeeMP3() {
            ArrayList<SongResult> tempresults = new ArrayList<SongResult>();
            String formatstring = query.replace(" ", "+");
            String tempquery = "http://beemp3s.org/index.php?q=" + formatstring;
            Elements searchResults = new Elements();
            try {
                Document document = Jsoup.connect(tempquery).get();
                searchResults = document.select("ol");
                searchResults = searchResults.select("");

            } catch (IOException e) {
                e.printStackTrace();
            }
            return tempresults;
        }

        //search the itemvn site for songs
        private ArrayList<SongResult> searchitemvn() {
            String tempquery = "http://www.itemvn.com/listsong/?keyword=";
            tempquery.replace(" ", "%20");
            ArrayList<SongResult> tempresults = new ArrayList<SongResult>();
            Elements searchresults = new Elements();
            try {
                Document document = Jsoup.connect(tempquery).get();
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
                    tempresults.add(new SongResult(name, url));
                }
            }
            return tempresults;
        }

        @Override
        protected void onPostExecute(ArrayList<SongResult> songResults) {
            super.onPostExecute(songResults);
            pd.dismiss();
            Log.d("Play", "Done loading query");
            results = songResults;
            if (songResults.size() > 0)
                showList(songResults);
            else
                Toast.makeText(getActivity().getApplicationContext(), "Song not found", Toast.LENGTH_LONG).show();
        }
    }
}
