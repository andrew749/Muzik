package com.acod.play.app;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

import com.acod.play.app.Models.SongResult;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;

/**
 * Created by Andrew on 9/5/2014.
 */
public class XMLParser {
    Context context;

    public XMLParser(Context context) {
        this.context = context;
    }

    public void writeToXML(ArrayList<SongResult> results) throws IOException {
        String string = createXml(results);
        FileOutputStream outputStream;
        FileOperations op = new FileOperations();
        op.writeToFile("PlaySave", string, context);

    }


    private String createXml(ArrayList<SongResult> results) throws IOException {
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        serializer.setOutput(writer);
        serializer.startDocument("UTF-8", true);
        for (SongResult result : results) {
            serializer.startTag("", "entry");
            serializer.startTag("", "name");
            serializer.text(result.getName());
            serializer.endTag("", "name");
            serializer.startTag("", "url");
            serializer.text(result.url);
            serializer.endTag("", "url");
            serializer.endTag("", "entry");
        }
        serializer.endDocument();
        return writer.toString();
    }

    public ArrayList<SongResult> readFromXML(InputStream in) throws XmlPullParserException, IOException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(in, null);

        return parseXml(parser);
    }

    private ArrayList<SongResult> parseXml(XmlPullParser parser) throws XmlPullParserException, IOException {
        int eventType = parser.getEventType();
        ArrayList<SongResult> songresults = null;
        SongResult result = null;
        //not entering
        while (eventType != XmlPullParser.END_DOCUMENT) {
            String name = null;
            while (eventType != XmlPullParser.END_TAG) {
                if (eventType == XmlPullParser.START_DOCUMENT) {
                    songresults = new ArrayList<SongResult>();
                    Log.d("Play", "startdocument");
                } else if (eventType == XmlPullParser.START_TAG) {
                    name = parser.getName();
                    if (name.equalsIgnoreCase("entry")) {
                        result = new SongResult(null, null);
                        Log.d("Play", " new result");
                    }

                    if (name.equalsIgnoreCase( "name")) {
                        result.name = parser.nextText();
                        Log.d("Play", result.name);

                    } else if (name.equalsIgnoreCase("url")) {
                        result.url = parser.nextText();
                        Log.d("Play", result.url);

                    }

                }
                eventType = parser.next();
            }
            if (result != null)
                songresults.add(result);
               eventType=parser.next();
        }

        return songresults;
    }


}
