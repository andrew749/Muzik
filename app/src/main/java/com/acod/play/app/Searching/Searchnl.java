package com.acod.play.app.Searching;

import android.net.Uri;
import android.util.Log;

import com.acod.play.app.Activities.HomescreenActivity;
import com.acod.play.app.Models.SongResult;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by andrew on 7/11/14.
 */
public abstract class Searchnl {
    public static ArrayList<SongResult> getSongs(String query) {
        ArrayList<SongResult> temp = new ArrayList<SongResult>();
        String u = "http://www.downloads.nl/results/mp3/1/" + Uri.parse(query);
        Elements searchResults = new Elements();
        try {
            Document document = Jsoup.connect(u).get();
            searchResults = document.select(".tl");
            for (Element x : searchResults) {
                String url = "http://www.downloads.nl" + x.attr("href");
                //todo add artist string to the name so that result is clearer
                URL url2 = new URL(url);
                HttpURLConnection ucon = (HttpURLConnection) url2.openConnection();
                ucon.setInstanceFollowRedirects(false);
                URL secondURL = new URL(ucon.getHeaderField("Location"));
                String name = x.select("span").text();
                if (HomescreenActivity.debugMode) {
                    Log.d("Play", "Downloads.nl Name=" + name + " url=" + secondURL);
                }
                temp.add(new SongResult(name, secondURL.toString()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return temp;
    }
}
