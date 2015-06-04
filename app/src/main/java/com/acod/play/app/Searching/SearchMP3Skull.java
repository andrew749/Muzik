package com.acod.play.app.Searching;

import android.util.Log;

import com.acod.play.app.Activities.HomescreenActivity;
import com.acod.play.app.Models.SongResult;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by andrewcodispoti on 2015-04-24.
 */
public abstract class SearchMP3Skull {
    //query mp3skull and return an arraylist with all of the results
    //working fully
    public static ArrayList<SongResult> getSongs(String query) {
        ArrayList<SongResult> tempresults = new ArrayList<SongResult>();
        String tempquery = "http://mp3skull.com/mp3/" + query + ".html";
        Elements searchresults = new Elements();
        try {
            Document document = Jsoup.connect(tempquery).get();
            searchresults = document.select("div#song_html");
        } catch (IOException e) {
        }
        if (!searchresults.isEmpty()) {
            for (Element inputelement : searchresults) {
                String name, duration, url;
                name = inputelement.select("b").text();
                url = inputelement.select("a[href]").first().attr("href");
                if (HomescreenActivity.debugMode) {
                    Log.d("PLAY", "Found entry name=" + name + " url=" + url);
                }
                tempresults.add(new SongResult(name, url));
            }
        }

        return tempresults;
    }
}
