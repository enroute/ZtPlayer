package com.ztfun.util;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidKeyException;

public class AppCache {
    private static JSONObject defaultJson;
    private static final String DEFAULT_JSON_ASSETS = "default.json";
    private static final String TAG = AppCache.class.getSimpleName();

    private static final AppCache instance;
    static {
        instance = new AppCache();
    }

    public AppCacheExecutor with(Context context) {
        return new AppCacheExecutor(context);
    }

    public void initialize(Context context) {
        // get default from 'default.json' in assets
        defaultJson = AssetsUtil.getAsJson(context, DEFAULT_JSON_ASSETS);
    }

    public static AppCache getInstance() {
        Log.d(TAG, "Default from assets:" + defaultJson.toString());
        return instance;
    }

    public String get(Context context, String name) throws InvalidKeyException {
        // todo:

        // fallback to default
        try {
            return defaultJson.getString(name);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        throw new InvalidKeyException("Key=" + name + " not found!");
    }

    public static class AppCacheExecutor {
        private final Context context;
        private AppCache cache = AppCache.getInstance();
        public AppCacheExecutor(Context context) {
            this.context = context;
        }

        public String get(String name) throws InvalidKeyException {
            return cache.get(context, name);
        }
    }

}
