package com.acod.play.app;

import android.content.Context;
import android.os.Environment;
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
        FileOperations op=new FileOperations();
        op.writeToFile("PlaySave",string,context);

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
        XmlPullParser parser = factory.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(in, null);

        return parseXml(parser);
    }

    private ArrayList<SongResult> parseXml(XmlPullParser parser) throws XmlPullParserException, IOException {
        int eventType = parser.getEventType();
        ArrayList songresults = new ArrayList();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            String name = null;
            switch (eventType) {


                case XmlPullParser.START_TAG:
                    name = parser.getName();
                    if (name == "entry") {
                        String named = parser.nextText();
                        String url = parser.nextText();
                        SongResult result = new SongResult(named, url);
                        songresults.add(result);

                    }
                    eventType = parser.next();
                    break;
            }

        }
        return songresults;
    }


}
