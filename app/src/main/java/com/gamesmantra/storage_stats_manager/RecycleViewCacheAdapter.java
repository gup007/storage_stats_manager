package com.gamesmantra.storage_stats_manager;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class RecycleViewCacheAdapter extends RecyclerView.Adapter<RecycleViewCacheAdapter.TaskViewHolder> {

    public static final CacheItemSizeComparator CACHE_ITEM_SIZE_COMPARATOR = new CacheItemSizeComparator();
    private List<SelectedCacheItem> mCacheItemList;
    private long cacheSize;
    private Context context;

    public void addItem(CacheItem cacheItem) {
        mCacheItemList.add(new SelectedCacheItem(cacheItem));
        notifyDataSetChanged();
        cacheSize += cacheItem.getCacheSize();
    }

    public long getCacheSize() {
        return cacheSize;
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder {
        ImageView appIcon;
        TextView appName;
        TextView memoryDetails;

        TaskViewHolder(View itemView) {
            super(itemView);
            appIcon = (ImageView) itemView.findViewById(R.id.sfItemAppIconImageView);
            appName = (TextView) itemView.findViewById(R.id.sfItemAppTitleTextView);
            memoryDetails = (TextView) itemView.findViewById(R.id.sfAppItemSizeTextView);
        }
    }

    public RecycleViewCacheAdapter(Context context, List<CacheItem> cacheList) {
        this.context = context;
        mCacheItemList = new ArrayList<>();
        cacheSize = 0;
        if (cacheList != null)
            for (int i = 0; i < cacheList.size(); i++) {
                mCacheItemList.add(new SelectedCacheItem(cacheList.get(i)));
                cacheSize += cacheList.get(i).getCacheSize();
            }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.sf_app_item, viewGroup, false);
        return new TaskViewHolder(v);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder taskViewHolder, int pos) {
        SelectedCacheItem selectedCacheItem = mCacheItemList.get(pos);
        CacheItem cacheItem = selectedCacheItem.cacheItem;

        NumberFormat formatter = NumberFormat.getNumberInstance();
        formatter.setMaximumFractionDigits(1);

        taskViewHolder.appIcon.setImageDrawable(cacheItem.getApplicationIcon());
        taskViewHolder.appName.setText(cacheItem.getApplicationName());
        String fileSize = Formatter.formatFileSize(context, cacheItem.getCacheSize());
//        taskViewHolder.memoryDetails.setText(String.format("%s %s", formatter.format(storageSize.getValue()), storageSize.getSuffix()));
        taskViewHolder.memoryDetails.setText(fileSize);
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public int getItemCount() {
        return mCacheItemList.size();
    }

    public List<SelectedCacheItem> getAllItems() {
        return mCacheItemList;
    }

    public class SelectedCacheItem {
        boolean isSelected = true;
        CacheItem cacheItem;

        public SelectedCacheItem(CacheItem cacheItem) {
            this.cacheItem = cacheItem;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public CacheItem getCacheItem() {
            return cacheItem;
        }
    }

    public void sort(CacheItemSizeComparator comparator) {
        try {
            Collections.sort(mCacheItemList, comparator);
        } catch (Exception e) {
            // IGNORED
        }

        notifyDataSetChanged();
    }

    public static class CacheItemSizeComparator implements Comparator<SelectedCacheItem> {

        @Override
        public int compare(SelectedCacheItem lhs, SelectedCacheItem rhs) {
            if (lhs.cacheItem.getCacheSize() > rhs.cacheItem.getCacheSize())
                return -1;
            else if (lhs.cacheItem.getCacheSize() < rhs.cacheItem.getCacheSize())
                return 1;
            else return 0;
        }
    }
}
