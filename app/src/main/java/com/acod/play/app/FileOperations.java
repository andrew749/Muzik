package com.acod.play.app;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Created by andrew on 12/21/14.
 */
public class FileOperations {
    public static String getFileValue(String fileName, Context context) {
        try {
            StringBuffer outStringBuf = new StringBuffer();
            String inputLine = "";
            /*
             * We have to use the openFileInput()-method the ActivityContext
             * provides. Again for security reasons with openFileInput(...)
             */
            FileInputStream fIn = context.openFileInput(fileName);
            InputStreamReader isr = new InputStreamReader(fIn);
            BufferedReader inBuff = new BufferedReader(isr);
            while ((inputLine = inBuff.readLine()) != null) {
                outStringBuf.append(inputLine);
                outStringBuf.append("\n");
            }
            inBuff.close();
            return outStringBuf.toString();
        } catch (IOException e) {
            return null;
        }
    }

    public static boolean appendFileValue(String fileName, String value,
                                          Context context) {
        return writeToFile(fileName, value, context);
    }

    public static boolean setFileValue(String fileName, String value,
                                       Context context) {
        return writeToFile(fileName, value, context);
    }

    public static boolean writeToFile(String fileName, String value,
                                      Context context) {

            /*
             * We have to use the openFileOutput()-method the ActivityContext
             * provides, to protect your file from others and This is done for
             * security-reasons. We chose MODE_WORLD_READABLE, because we have
             * nothing to hide in our file
             */
            File root = android.os.Environment.getExternalStorageDirectory();

            // See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder

            File dir = new File (root.getAbsolutePath());
            dir.mkdirs();
            File file = new File(dir,fileName+".xml");

            try {
                FileOutputStream f = new FileOutputStream(file);
                PrintWriter pw = new PrintWriter(f);
                pw.println("Hi , How are you");
                pw.println("Hello");
                pw.flush();
                pw.close();
                f.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.i("Play", "******* File not found. Did you" +
                        " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
            } catch (IOException e) {
                e.printStackTrace();
            }

        return true;
    }

    public static void deleteFile(String fileName, Context context) {
        context.deleteFile(fileName);
    }
}
