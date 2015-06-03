package com.acod.play.app.Searching;

import com.acod.play.app.Models.SongResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.net.URLEncoder;t
/**
 * Created by andrewcodispoti on 2015-05-31.
 */
public abstract class SearchMuzikApi {
    public static ArrayList<SongResult> getSongs(String query) {
        ArrayList<SongResult> tempresults = new ArrayList<SongResult>();
        String tempquery = "http://muzik-api.herokuapp.com/search?songname=" + URLEncoder.encode(query);
        try {
            JSONArray elements = new JSONArray(readUrl(new URL(tempquery)));
            for (int i = 0; i < elements.length(); i++) {
                JSONObject currElement = elements.getJSONObject(i);
                tempresults.add(new SongResult(currElement.get("title").toString(), currElement.getJSONArray("url").get(0).toString()));
            }
        } catch (IOException e) {
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tempresults;
    }

    public static String readUrl(URL url) throws Exception {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuffer buffer = new StringBuffer();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1)
                buffer.append(chars, 0, read);

            return buffer.toString();
        } finally {
            if (reader != null)
                reader.close();
        }
    }

}
