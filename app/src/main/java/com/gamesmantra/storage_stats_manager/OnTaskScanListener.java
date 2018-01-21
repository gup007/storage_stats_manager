package com.gamesmantra.storage_stats_manager;

import java.util.List;

public interface OnTaskScanListener {
    void onScanStarted();

    void onScanProgressUpdated(int current, int max, CacheItem cacheItem);

    void onScanCompleted(List<CacheItem> apps);
}