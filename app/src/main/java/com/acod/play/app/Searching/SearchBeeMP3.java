package com.acod.play.app.Searching;

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
public abstract class SearchBeeMP3 {
    public static ArrayList<SongResult> getSongs(String query) {
        ArrayList<SongResult> results = new ArrayList<SongResult>();
        //the base url to search for songs
        String url = "http://beemp3s.org/index.php?q=" + query.replace(" ", "+");
        try {
            Document doc = Jsoup.connect(url).get();
            Elements elements;
            //take all list elements
            elements = doc.select("ol > li");
            for (Element x : elements) {
                //go to the nexturl to get a download link
                String nextUrl = "http://beemp3s.org/" + x.select("a").attr("href");
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
