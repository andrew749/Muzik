package com.acod.play.app;

/**
 * Created by andrew on 09/07/14.
 */


import android.util.Log;

import com.acod.play.app.Activities.HomescreenActivity;
import com.acod.play.app.Models.SongResult;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Print a list of videos matching a search term.
 */
public class SearchYou {

    /**
     * Define a global variable that identifies the name of a file that
     * contains the developer's API key.
     */
    private static final String API_KEY = "AIzaSyBZSR3nrb0re2fTRlNoBy0nn_5zdTGmKYo";

    private static final long NUMBER_OF_VIDEOS_RETURNED = 5;
    static String query = "";
    /**
     * Define a global instance of a Youtube object, which will be used
     * to make YouTube Data API requests.
     */
    private YouTube youtube;


    public SearchYou(String query) {
        this.query = query;
    }

    /*
     * Prints out all results in the Iterator. For each result, print the
     * title, video ID, and thumbnail.
     *
     * @param iteratorSearchResults Iterator of SearchResults to print
     *
     * @param query Search query (String)
     */
    private ArrayList<SongResult> parseSongResult(Iterator<SearchResult> iteratorSearchResults, String query) {
        ArrayList<SongResult> t = new ArrayList<SongResult>();
        if (!iteratorSearchResults.hasNext()) {
            Log.d("Play", "No results");
        }

        while (iteratorSearchResults.hasNext()) {

            SearchResult singleVideo = iteratorSearchResults.next();
            ResourceId rId = singleVideo.getId();

            // Confirm that the result represents a video. Otherwise, the
            // item will not contain a video ID.
            if (rId.getKind().equals("youtube#video")) {
                t.add(new SongResult(singleVideo.getSnippet().getTitle(), rtspURL(rId.getVideoId())));
            }
        }
        return t;
    }

    private String rtspURL(String video_id) {
        String anotherurl = "";
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = null;
            db = dbf.newDocumentBuilder();

            Document doc1 = db.parse("http://gdata.youtube.com/feeds/mobile/videos/" + video_id);

            Element rsp = (Element) doc1.getElementsByTagName("media:content").item(1);
            anotherurl = rsp.getAttribute("url");
            System.out.println("my " + anotherurl);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (HomescreenActivity.debugMode) {
            Log.d("Play", "Another Url");
        }
        return anotherurl;
    }

    public ArrayList<SongResult> getSongs() {
        ArrayList<SongResult> temp = new ArrayList<SongResult>();
        try {
            // This object is used to make YouTube Data API requests. The last
            // argument is required, but since we don't need anything
            // initialized when the HttpRequest is initialized, we override
            // the interface and provide a no-op function.
            youtube = new YouTube.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), new HttpRequestInitializer() {
                public void initialize(HttpRequest request) throws IOException {
                }
            }).setApplicationName("Play").build();

            // Prompt the user to enter a query term.
            String queryTerm = query;

            // Define the API request for retrieving search results.
            YouTube.Search.List search = youtube.search().list("id,snippet");

            // Set your developer key from the Google Developers Console for
            // non-authenticated requests. See:
            // https://console.developers.google.com/
            String apiKey = API_KEY;
            search.setKey(apiKey);
            search.setQ(queryTerm);

            // Restrict the search results to only include videos. See:
            // https://developers.google.com/youtube/v3/docs/search/list#type
            search.setType("video");

            // To increase efficiency, only retrieve the fields that the
            // application uses.
            search.setFields("items(id/kind,id/videoId,snippet/title)");
            search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);

            // Call the API and print results.
            SearchListResponse searchResponse = search.execute();
            List<SearchResult> searchResultList = searchResponse.getItems();
            if (searchResultList != null) {
                temp = parseSongResult(searchResultList.iterator(), queryTerm);
            }
        } catch (GoogleJsonResponseException e) {
            e.printStackTrace();
            Log.d("Play", "Gson error");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("Play", "io error");
        } catch (Throwable t) {

            t.printStackTrace();
        }
        return temp;
    }
}