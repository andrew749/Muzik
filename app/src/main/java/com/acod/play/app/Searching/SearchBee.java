package com.acod.play.app.Searching;

import android.util.Log;

import com.acod.play.app.Models.SongResult;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Andrew on 7/12/2014.
 */
public abstract class SearchBee {
    public static ArrayList<SongResult> getSongs(String query) {
        ArrayList<SongResult> results = new ArrayList<SongResult>();
        String url = "http://beemp3s.org/index.php?q=" + query.replace(" ", "+");
        Log.d("Play", url);
        try {
            Document doc = Jsoup.connect(url).get();
            Elements elements;

            elements = doc.select("ol > li");
            Log.d("Play", "elements=" + elements);
            for (Element x : elements) {
                String nextUrl = "http://beemp3s.org/" + x.select("a").attr("href");
                Log.d("Play", nextUrl);
                Document next = Jsoup.connect(nextUrl).get();
                String furl = next.select("#ssilka").select("a").attr("href");
                String name = next.select(".h1-title-sing").text();
                results.add(new SongResult(name, furl));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return results;

    }
}
