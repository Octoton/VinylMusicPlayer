package com.poupa.vinylmusicplayer.misc.queue.DynamicElement.AlbumShuffling;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.discog.Discography;
import com.poupa.vinylmusicplayer.misc.queue.DynamicElement.AbstractShuffling.AbstractQueueLoader;
import com.poupa.vinylmusicplayer.misc.queue.DynamicElement.AlbumShuffling.Search.AutomaticSearch;
import com.poupa.vinylmusicplayer.misc.queue.DynamicElement.AlbumShuffling.Search.ManualSearch;
import com.poupa.vinylmusicplayer.misc.queue.DynamicElement.AlbumShuffling.Search.Search;
import com.poupa.vinylmusicplayer.misc.queue.DynamicElement.DynamicElement;
import com.poupa.vinylmusicplayer.misc.queue.DynamicElement.DynamicQueueItemAdapter;
import com.poupa.vinylmusicplayer.misc.queue.DynamicElement.DynamicQueueLoader;
import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.sort.AlbumSortOrder;
import com.poupa.vinylmusicplayer.util.MusicUtil;


/** Album shuffling implementation of {@link DynamicQueueLoader} */
public class AlbumShufflingQueueLoader extends AbstractQueueLoader {
    public static final String SEARCH_TYPE = "search_type";

    private final DB database;
    private Album nextAlbum;

    public AlbumShufflingQueueLoader() {
        super();

        this.nextAlbum = null;
        this.database = new DB();
    }

    @Override
    public boolean restoreQueue(Context context, Song song) {
        this.nextAlbum = Discography.getInstance().getAlbum(database.fetchNextRandomAlbumId());

        AlbumShufflingUtil.getInstance().restoreHistories();
        return super.restoreQueue(song);
    }

    @Override
    public void stop() {
        super.stop();
        AlbumShufflingUtil.getInstance().stopHistories();
    }

    @Override
    public void transferDynamicElement(DynamicQueueLoader loader) {
        if (loader != null && loader.getClass().equals(this.getClass())) {
            this.nextAlbum = ((AlbumShufflingQueueLoader) loader).nextAlbum;
        }
    }

    public DynamicQueueItemAdapter getAdapter() {
        return new AlbumShufflingQueueItemAdapter();
    }

    @Override
    public boolean setNextDynamicQueue(Context context, Song song, boolean force) {
        if (!super.setNextDynamicQueue(null, context, song, force))
            return false;

        Album album;
        long currentAlbumId = -1;
        if (nextAlbum != null) {
            currentAlbumId = nextAlbum.getId();
        }
        AlbumShufflingUtil.getInstance().resetSearchHistory();
        album = search(song, currentAlbumId, new AutomaticSearch(), context);

        if (album != null) {
            this.nextAlbum = album;
            this.database.setNextRandomAlbumId(album.getId());
        } else {
            this.nextAlbum = null;
            this.database.setNextRandomAlbumId(-1);
        }

        return true;
    }

    @Override
    public boolean setNextDynamicQueue(Bundle criteria, @NonNull Context context, Song song, boolean force) {
        if (!super.setNextDynamicQueue(criteria, context, song, force))
            return false;

        Album album;
        long currentAlbumId = -1;
        if (nextAlbum != null) {
            currentAlbumId = nextAlbum.getId();
        }
        album = search(song, currentAlbumId, new ManualSearch(criteria), context);

        if (album != null) {
            this.nextAlbum = album;
            this.database.setNextRandomAlbumId(album.getId());
        }

        return true;
    }

    // Search for next album by using song as the currently played album
    public Album search(Song song, long currentAlbumId, Search searchFunction, Context context) {

        // Get all possible album to be play
        ArrayList<Album> albums;
        synchronized (Discography.getInstance()) {
            albums = new ArrayList<>(Discography.getInstance().getAllAlbums(AlbumSortOrder.BY_YEAR_DESC));
        }

        // Search
        return searchFunction.foundNextAlbum(song, albums, currentAlbumId, AlbumShufflingUtil.getInstance().getListenHistory(), AlbumShufflingUtil.getInstance().getSearchHistory(), context);
    }

    public static ArrayList<Song> getNextRandomQueue() {
        ArrayList<Album> albums;
        synchronized (Discography.getInstance()) {
            albums = new ArrayList<>(Discography.getInstance().getAllAlbums(AlbumSortOrder.BY_YEAR_DESC));
        }
        ArrayList<Integer> forbiddenPosition = new ArrayList<>();
        int i = 0;
        if (AlbumShufflingUtil.getInstance().getBlackListUse()) {
            for (Album album : albums) {
                if (album.getIsBlackListedFromAlbumSearch()) {
                    forbiddenPosition.add(i);
                }
                i++;
            }
        }

        int randomAlbumPosition = Search.randomIntInBoundWithForbiddenNumber(albums.size(), forbiddenPosition);
        if (randomAlbumPosition > 0) {
            Album album = albums.get(randomAlbumPosition);
            return album.songs;
        }

        return null;
    }

    public boolean isNextQueueEmpty() {
        return  (nextAlbum == null);
    }

    public ArrayList<Song> getNextQueue() {
        if (isNextQueueEmpty())
            return null;

        if (this.songUsedForSearching != null)
            AlbumShufflingUtil.getInstance().commitHistories(this.songUsedForSearching.albumId); // commit ensure no duplication, this call help remember first album listen too

        AlbumShufflingUtil.getInstance().commitHistories(nextAlbum.getId());

        return nextAlbum.songs;
    }

    @Override
    protected DynamicElement createEmptyDynamicElement(Context context) {
        return new DynamicElement(context.getResources().getString(R.string.next_album),
                context.getResources().getString(R.string.no_album_found),
                R.drawable.ic_shuffle_album_white_24dp);
    }

    @Override
    protected DynamicElement createNewDynamicElement(Context context) {
        return new DynamicElement(context.getResources().getString(R.string.next_album),
                MusicUtil.buildInfoString(this.nextAlbum.getArtistNames().get(0), this.nextAlbum.getTitle()),
                R.drawable.ic_shuffle_album_white_24dp);
    }

    @Override
    protected boolean isSongDifferentEnough(@NonNull Song song) {
        if (this.songUsedForSearching == null)
            return true;
        return (song.albumId != this.songUsedForSearching.albumId);
    }
}
