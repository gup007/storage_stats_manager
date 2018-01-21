package com.gamesmantra.storage_stats_manager;

import android.app.AppOpsManager;
import android.app.usage.StorageStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_USAGE_ACCESS_SETTINGS = 34;
    private ScanApps taskScan;
    private RecyclerView mRecyclerView;
    private RecycleViewCacheAdapter cacheListadapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = findViewById(R.id.app_stats_list);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            showStorageVolumes();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showStorageVolumes() {
        StorageStatsManager storageStatsManager = (StorageStatsManager) getSystemService(Context.STORAGE_STATS_SERVICE);
        StorageManager storageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        if (storageManager == null || storageStatsManager == null) {
            return;
        }
        List<StorageVolume> storageVolumes = storageManager.getStorageVolumes();
        for (StorageVolume storageVolume : storageVolumes) {
            final String uuidStr = storageVolume.getUuid();
            final UUID uuid = uuidStr == null ? StorageManager.UUID_DEFAULT : UUID.fromString(uuidStr);
            try {
                Log.d("AppLog", "storage:" + uuid + " : " + storageVolume.getDescription(this) + " : " + storageVolume.getState());
                Log.d("AppLog", "getFreeBytes:" + Formatter.formatShortFileSize(this, storageStatsManager.getFreeBytes(uuid)));
                Log.d("AppLog", "getTotalBytes:" + Formatter.formatShortFileSize(this, storageStatsManager.getTotalBytes(uuid)));
            } catch (Exception e) {
                // IGNORED
            }
        }
    }

    private boolean hasUsageStatsPermission(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return true;

        boolean granted;
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        if (appOps == null) {
            return false;
        }
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), context.getPackageName());

        if (mode == AppOpsManager.MODE_DEFAULT) {
            granted = (context.checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED);
        } else {
            granted = (mode == AppOpsManager.MODE_ALLOWED);
        }
        return granted;
    }

    private void showPopup() {
        AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setTitle("ACTION USAGE ACCESS SETTINGS");
        dialog.setMessage("Please give permission to access cache setting");
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivityForResult(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), REQUEST_USAGE_ACCESS_SETTINGS);
                        dialog.dismiss();
                    }
                });
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        dialog.show();
    }

    private void startScanning() {
        taskScan = new ScanApps(getApplicationContext());

        taskScan.setTaskScanListener(new OnTaskScanListener() {

            boolean updated;

            @Override
            public void onScanStarted() {
                initializeAdapter(new ArrayList<CacheItem>());
            }

            @Override
            public void onScanProgressUpdated(int current, int max, CacheItem cacheItem) {
                if (updated) return;
                if (cacheItem != null) {
                    cacheListadapter.addItem(cacheItem);
                    cacheListadapter.sort(RecycleViewCacheAdapter.CACHE_ITEM_SIZE_COMPARATOR);
                    String fileSize = Formatter.formatFileSize(getApplicationContext(), cacheListadapter.getCacheSize());
                }

                if (current != -1 && current == max && !updated) {
                    updated = true;
                    updateFinishUI(cacheListadapter.getItemCount() > 0);
                }
            }

            @Override
            public void onScanCompleted(List<CacheItem> apps) {
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        if (cacheListadapter.getAllItems().size() == 0 && !updated) {
                            updated = true;
                            updateFinishUI(cacheListadapter.getItemCount() > 0);
                        }
                    }
                }, 3000);
            }
        });

        taskScan.execute();
    }

    private void updateFinishUI(boolean foundApps) {
        if (foundApps) {
            String fileSize = Formatter.formatFileSize(getApplicationContext(), cacheListadapter.getCacheSize());
        } else {

        }
    }

    private void initializeAdapter(List<CacheItem> items) {
        cacheListadapter = new RecycleViewCacheAdapter(getApplicationContext(), items);
        mRecyclerView.setAdapter(cacheListadapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (hasUsageStatsPermission(getApplicationContext())) {
            startScanning();
        } else {
            showPopup();
        }
    }


    @Override
    public void onStop() {
        stopScanning();
        super.onStop();
    }

    public void onDestroy() {
        stopScanning();
        super.onDestroy();
    }

    private void stopScanning() {
        if (taskScan != null) {
            taskScan.setTaskScanListener(null);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_USAGE_ACCESS_SETTINGS) {
            if (hasUsageStatsPermission(getApplicationContext())) {
                startScanning();
            } else {
                showPopup();
            }
        }
    }
}
