package com.poupa.vinylmusicplayer.misc.queue.DynamicElement.AbstractShuffling;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import com.poupa.vinylmusicplayer.misc.queue.DynamicElement.DynamicElement;
import com.poupa.vinylmusicplayer.misc.queue.DynamicElement.DynamicQueueLoader;
import com.poupa.vinylmusicplayer.model.Song;


/** Abstract pseudo implementation of {@link DynamicQueueLoader} to help creating multiple shuffling system */
public abstract class AbstractQueueLoader implements DynamicQueueLoader {
    protected Song songUsedForSearching;

    public AbstractQueueLoader() {
        this.songUsedForSearching = null;
    }

    public boolean restoreQueue(Song song) {
        this.songUsedForSearching = song;

        return true;
    }

    private boolean tryUpdateDynamicElement(Song song, boolean force) {
        if ( !(song != null && (force || isSongDifferentEnough(song))) )
            return false;

        this.songUsedForSearching = song;
        return true;
    }

    public boolean setNextDynamicQueue(Context context, Song song, boolean force) {
        return tryUpdateDynamicElement(song, force);
    }

    public boolean setNextDynamicQueue(Bundle criteria, Context context, Song song, boolean force) {
        return tryUpdateDynamicElement(song, force);
    }

    @NonNull
    public DynamicElement getDynamicElement(Context context) {
        if (isNextQueueEmpty())
            return createEmptyDynamicElement(context);

        return createNewDynamicElement(context);
    }

    public void stop() {
        songUsedForSearching = null;
    }

    /** @return an DynamicElement that show to the user that nothing was found and no next queue will be loaded */
    protected abstract DynamicElement createEmptyDynamicElement(Context context);

    /** @return an DynamicElement that show to the user what the next queue will be */
    protected abstract DynamicElement createNewDynamicElement(Context context);

    /** @return song is different enough than {@link #songUsedForSearching} for dynamic element to be recalculated */
    protected abstract boolean isSongDifferentEnough(@NonNull Song song);

}
