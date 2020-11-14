package com.poupa.vinylmusicplayer.misc.RandomAlbum;


import java.util.ArrayList;

import android.content.Context;
import android.util.Log;

import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.discog.Discography;
import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.model.Song;


public class NextRandomAlbum {

    public static final int RANDOM_ALBUM_SONG_ID = -2;
    public static final int EMPTY_NEXT_RANDOM_ALBUM_ID = -3;

    private ArrayList<Album> albums;

    static private NextRandomAlbum sInstance = new NextRandomAlbum();

    private History listenHistory;
    private History searchHistory;
    private long nextRandomAlbumId;
    private long lastAlbumIdSearched;
    private Search searchFunction;


    static public NextRandomAlbum getInstance() {
        return sInstance;
    }

    private NextRandomAlbum() {
        int historySize = 5; // Doesn't need to be too big, as history is there only so that we don't listen to the same set of album endlessly

        searchHistory = new History(historySize, "search");
        listenHistory = new History(historySize, "listen");

        lastAlbumIdSearched = -1;
        nextRandomAlbumId = -1;
    }

    // necessary as albumId are unique but neither in order, neither consecutive (only certainty is albumId > 0)
    private int getAlbumPosition(ArrayList<Album> albumList, long albumId) {
        int i = 0;
        for (Album album : albumList) {
            if (album.getId() == albumId) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public void initSearch(Search searchFunction) {
        this.searchFunction = searchFunction;
    }

    private Song addNewRandomAlbum(long albumId, String albumName, long artistId, String artistName, Context context) {
        return new Song(RANDOM_ALBUM_SONG_ID, context.getResources().getString(R.string.next_album), 0, -1, -1, "",
                -1, -1, albumId, albumName, artistId, artistName);
    }

    private Song addEmptyRandomAlbum(Context context) {
        return addNewRandomAlbum(EMPTY_NEXT_RANDOM_ALBUM_ID, "None", -1, context.getResources().getString(R.string.no_album_found), context);
    }

    private Song addNewRandomAlbum(Album album, Context context) {
        if (album != null) {
            return addNewRandomAlbum(album.getId(), album.getTitle(), album.getArtistId(),
                    album.getArtistName(), context);
        } else {
            return addEmptyRandomAlbum(context);
        }
    }

    public Song search(Song song, Context context) {

        synchronized (Discography.getInstance()) {
            albums = new ArrayList<>(Discography.getInstance().getAllAlbums());
        }

        lastAlbumIdSearched = song.albumId;

        if (!searchFunction.isManual()) {
            Log.d("TOTO", "AUTOMATIC SEARCH");
            searchHistory.clearHistory();
        } else {
            Log.d("TOTO", "MANUAL SEARCH");
        }

        Log.d("TOTO", "Search album id: " + song.albumId);
        for (Long albumId : searchHistory.getHistory()) {
            Log.d("TOTO", "Search history: " + albumId + ", name: " +
                    albums.get(getAlbumPosition(albums, albumId)).getTitle());
        }
        for (Long albumId : listenHistory.getHistory()) {
            Log.d("TOTO", "Listen history: " + albumId + ", name: " +
                    albums.get(getAlbumPosition(albums, albumId)).getTitle());
        }

        Album album = searchFunction.foundNextAlbum(song, albums, nextRandomAlbumId, listenHistory, searchHistory, context);

        if (album != null) {
            Log.d("TOTO", "Album is: " + album.getTitle());

            nextRandomAlbumId = album.getId();
        }
        return addNewRandomAlbum(album, context);
    }

    public void resetHistories(long albumId) {
        searchHistory.clearHistory();
        listenHistory.clearHistory();
        nextRandomAlbumId = albumId;
    }

    public void commit(long albumId) {
        // add id to listen history, this should be the old album not the wanted one
        listenHistory.addIdToHistory(albumId);
        nextRandomAlbumId = -1; // until new search is done
        lastAlbumIdSearched = -1;
    }

    public Long getLastAlbumIdSearched() {
        return lastAlbumIdSearched;
    }

    // called when shuffling change
    public void stop() {
        // clear history search and listen
        searchHistory.stop();
        listenHistory.stop();
        nextRandomAlbumId = -1;
        lastAlbumIdSearched = -1;
    }

    public void clearSearchHistory() {
        searchHistory.clearHistory();
        lastAlbumIdSearched = -1;
    }
}