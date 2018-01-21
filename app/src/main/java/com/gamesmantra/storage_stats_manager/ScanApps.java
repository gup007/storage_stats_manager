package com.gamesmantra.storage_stats_manager;

import android.annotation.TargetApi;
import android.app.usage.StorageStats;
import android.app.usage.StorageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Process;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class ScanApps extends AsyncTask<Void, Object, List<CacheItem>> {

    private int mAppCount = 0;
    private List<ApplicationInfo> packages;
    private List<CacheItem> apps = new ArrayList<>();

    private OnTaskScanListener taskScanListener;
    private Context context;
    private long mCacheSize;

    ScanApps(Context context) {
        this.context = context;
    }

    public void setTaskScanListener(OnTaskScanListener taskScanListener) {
        this.taskScanListener = taskScanListener;
    }

    public long getCacheSize() {
        return mCacheSize;
    }


    @Override
    protected void onPreExecute() {
        if (taskScanListener != null) {
            taskScanListener.onScanStarted();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    @Override
    protected List<CacheItem> doInBackground(final Void... params) {
        mCacheSize = 0;
        PackageManager packageManager = context.getPackageManager();
        final StorageStatsManager storageStatsManager = (StorageStatsManager) context.getSystemService(Context.STORAGE_STATS_SERVICE);
        final StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        if (storageManager == null || storageStatsManager == null) {
            postPublishProgress();
            return new ArrayList<>(apps);
        }
        final List<StorageVolume> storageVolumes = storageManager.getStorageVolumes();
        final UserHandle user = Process.myUserHandle();

        try {
            packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
            apps.clear();
            for (ApplicationInfo pkg : packages) {
                long cacheSize = 0;
                for (StorageVolume storageVolume : storageVolumes) {
                    final String uuidStr = storageVolume.getUuid();
                    final UUID uuid = uuidStr == null ? StorageManager.UUID_DEFAULT : UUID.fromString(uuidStr);
                    try {
                        final StorageStats storageStats = storageStatsManager.queryStatsForPackage(uuid, pkg.packageName, user);
                        cacheSize += storageStats.getCacheBytes();
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                mCacheSize += addPackage(apps, pkg.packageName, cacheSize);
                mAppCount++;
                postPublishProgress();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>(apps);
    }

    private void postPublishProgress() {
        if (mAppCount == packages.size()) {
            publishProgress(mAppCount, mAppCount, null);
        }
    }

    @Override
    protected void onProgressUpdate(Object... values) {
        if (taskScanListener != null) {
            if (values.length == 3) {
                taskScanListener.onScanProgressUpdated((int) values[0], (int) values[1], (CacheItem) values[2]);
            }
        }
    }

    @Override
    protected void onPostExecute(List<CacheItem> result) {
        if (taskScanListener != null) {
            taskScanListener.onScanCompleted(result);
        }
    }

    private long addPackage(List<CacheItem> apps, String packageName, long cacheSize) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo info = packageManager.getApplicationInfo(packageName,
                    PackageManager.GET_META_DATA);

            CacheItem cacheItem = new CacheItem(packageName,
                    packageManager.getApplicationLabel(info).toString(),
                    packageManager.getApplicationIcon(packageName),
                    cacheSize);
            apps.add(cacheItem);

            publishProgress(mAppCount, packages.size(), cacheItem);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return cacheSize;
    }
}