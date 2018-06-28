package com.kinetic.fit.data;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by andrew on 6/10/15.
 */
public class FileSystem {

    public static String readFile(String arg1){
        StringBuilder text = new StringBuilder();
        try {
            File file = new File(arg1);

            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close() ;
        }catch (IOException e) {
            e.printStackTrace();
        }
        return text.toString();
    }

    public static String loadAssetByName(Context context, String filename) {
        try {
            InputStream is = context.getAssets().open(filename);
            return readInputStream(is);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static String loadRawAssetByIdentifier(Context context, int asset) {
        InputStream is = context.getResources().openRawResource(asset);
        return readInputStream(is);
    }

    public static String loadRawAssetByName(Context context, String asset) {
        InputStream is = context.getResources().openRawResource(
                context.getResources().getIdentifier("raw/" + asset,
                        "raw", context.getPackageName()));

        return readInputStream(is);
    }

    private static String readInputStream(InputStream is){
        String contents;
        try {
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            contents = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return contents;
    }
}
