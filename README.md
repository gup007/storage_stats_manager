# Android StorageStatsManager

From Android O (API 26) StorageStatsManager class is available to Access detailed storage statistics. It provides a summary of how apps, users, and external/shared storage is utilizing disk space.

## Permission

No permissions are required when calling these APIs for your own package or UID. However, requesting details for any other package requires the android.Manifest.permission#PACKAGE_USAGE_STATS permission, which is a system-level permission that will not be granted to normal apps. Declaring that permission expresses your intention to use this API and an end user can then choose to grant this permission through the Settings application.

Using following piece of code you can easily gets Storage volume statistics. It gives you free space on the requested storage volume and total size of the underlying physical media that is hosting this storage volume.


```java
StorageStatsManager storageStatsManager = (StorageStatsManager) getSystemService(Context.STORAGE_STATS_SERVICE);
StorageManager storageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
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
```
## Query Stats For Package

You can get storage statistics for a specific package on the requested storage volume. You need UserHandler, UUID and package name which you want to get storage stats.

**UUID**
 represents an immutable universally unique identifier (UUID). A UUID represents a 128-bit value.
 
The following piece of code will give you. 

**getAppBytes**
Return the size of app. This includes APK files, optimized compiler output, and unpacked native libraries.

**getDataBytes**
Return the size of all data. This includes files stored under getDataDir(), getCacheDir(), getCodeCacheDir().

**getCacheBytes**
Return the size of all cached data. This includes files stored under getCacheDir() and getCodeCacheDir().

```java
final UserHandle user = Process.myUserHandle();
final String uuidStr = storageVolume.getUuid();
final UUID uuid = uuidStr == null ? StorageManager.UUID_DEFAULT : UUID.fromString(uuidStr);
try {
    final StorageStats storageStats = storageStatsManager.queryStatsForPackage(uuid, pkg.packageName, user);
    storageStats.getAppBytes()
    storageStats.getDataBytes()
    storageStats.getCacheBytes()
} catch (Exception e) {
    e.printStackTrace();
}

```
