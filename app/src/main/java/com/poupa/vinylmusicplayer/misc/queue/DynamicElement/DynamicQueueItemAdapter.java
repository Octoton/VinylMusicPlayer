package com.poupa.vinylmusicplayer.misc.queue.DynamicElement;

import android.content.Context;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.poupa.vinylmusicplayer.adapter.song.SongAdapter;

/** Provide all methods needed by DynamicPlayingQueueAdapter to show the {@link DynamicElement} at the end of the playing queue with the correct menu */
public interface DynamicQueueItemAdapter {
    int getSongMenuRes(int itemViewType);
    boolean onSongMenuItemClick(MenuItem item, Context context);
    void onBindViewHolder(@NonNull SongAdapter.ViewHolder holder, @NonNull final AppCompatActivity activity);

    void reloadDynamicElement();
}
