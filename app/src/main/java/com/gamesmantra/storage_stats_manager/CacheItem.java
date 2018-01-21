package com.gamesmantra.storage_stats_manager;

import android.graphics.drawable.Drawable;

public class CacheItem {

    private long mCacheSize;
    private String mPackageName, mApplicationName;
    private Drawable mIcon;

    public CacheItem(String packageName, String applicationName, Drawable icon, long cacheSize) {
        mCacheSize = cacheSize;
        mPackageName = packageName;
        mApplicationName = applicationName;
        mIcon = icon;
    }

    public Drawable getApplicationIcon() {
        return mIcon;
    }

    public String getApplicationName() {
        return mApplicationName;
    }

    public long getCacheSize() {
        return mCacheSize;
    }

    public String getPackageName() {
        return mPackageName;
    }
}
