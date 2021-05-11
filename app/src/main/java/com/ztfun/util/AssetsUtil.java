package com.ztfun.util;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AssetsUtil {
    public static String get(Context context, String name) {
//        BufferedReader reader = null;
//        StringBuilder sb = new StringBuilder();
//        try {
//            reader = new BufferedReader(new InputStreamReader(context.getAssets().open(name)));
//            String line;
//            while((line = reader.readLine()) != null) {
//                // todo: the readline drops the line terminator if any
//                sb.append(line);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (reader != null) {
//                try {
//                    reader.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
//        return sb.toString();

        InputStream is = null;
        String result = null;
        try {
            is = context.getAssets().open(name);
            // fixme: is InputStream.available() reliable?
            int size = is.available();
            byte[] buffer = new byte[size];
            int bytesActualRead = is.read(buffer);
            result = new String(buffer, 0, bytesActualRead);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return result;
    }

    public static JSONObject getAsJson(Context context, String name) {
        try {
            String content = get(context, name);
            return new JSONObject(content);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
