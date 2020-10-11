package com.poupa.vinylmusicplayer.misc;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.loader.AlbumLoader;
import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.service.MusicService;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;


public class NextRandomAlbum {
    public static final int BY_ALBUM = 1;
    public static final int BY_ARTIST = 2;
    public static final int BY_GENRE = 3;

    private ArrayList<Album> genre;
    private ArrayList<Album> artist;
    private ArrayList<Album> albums; // Used by randomAlbum shuffling

    static private NextRandomAlbum sInstance = new NextRandomAlbum();

    static public NextRandomAlbum getInstance() {
        return sInstance;
    }

    private NextRandomAlbum() {
        history = new History(5); // Should not be too big as history is there only so that we don't listen to the same set of album endlessly
    }

    public int getAlbumPositionByAlbum(long albumId) {
        return getAlbumPosition(albums, albumId);
    }

    public int getAlbumPositionByGenre(long albumId) {
        return getAlbumPosition(genre, albumId);
    }

    public int getAlbumPositionByArtist(long albumId) {
        return getAlbumPosition(artist, albumId);
    }

    /*public Album getRandomAlbumByAlbum(int lastSongPosition, int randomAlbumOldPosition, boolean runManually) {
        return getRandomAlbum(albums, lastSongPosition, randomAlbumOldPosition, runManually);
    }

    public Album getRandomAlbumByGenre(int lastSongPosition, int randomAlbumOldPosition, boolean runManually) {
        return getRandomAlbum(genre, lastSongPosition, randomAlbumOldPosition, runManually);
    }

    public Album getRandomAlbumByArtist(int lastSongPosition, int randomAlbumOldPosition, boolean runManually) {
        return getRandomAlbum(artist, lastSongPosition, randomAlbumOldPosition, runManually);
    } */

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

    public void constructGenreList(Song song) {
        genre.clear();

        for (Album albumGenre : albums) {
            if (albumGenre.songs != null && albumGenre.songs.size() > 0 &&
                    song.genre.equals(albumGenre.songs.get(0).genre)) {
                genre.add(albumGenre);
            }
        }
    }

    public void constructArtistList(Song song) {
        artist.clear();

        for (Album albumArtist : albums) {
            if (albumArtist.songs != null && albumArtist.songs.size() > 0 &&
                    song.artistId == albumArtist.songs.get(0).artistId) {
                artist.add(albumArtist);
            }
        }
    }

    private Album getRandomAlbum(ArrayList<Album> albumList, int lastSongPosition,
            int randomAlbumOldPosition, boolean runManually, Context context) {
        if (albumList.size() > 0) {
            int randomAlbumPosition = 0;
            int albumSize = albumList.size();
            if (albumSize > 2) {
                /*int i = 0;
                int maxLoop = 10;
                do {
                    randomAlbumPosition = new Random().nextInt(albumSize);
                    i++;
                } while (i < maxLoop && (randomAlbumPosition == lastSongPosition ||
                        randomAlbumPosition == randomAlbumOldPosition));*/

                int[] forbiddenNumbers = new int[] { lastSongPosition, randomAlbumOldPosition };
                Arrays.sort(
                        forbiddenNumbers); // sorting is needed to get sweet why of getting randomize exclusion
                randomAlbumPosition = new Random().nextInt(albumSize - forbiddenNumbers.length);
                for (int forbiddenNumber : forbiddenNumbers) {
                    if (randomAlbumPosition >= forbiddenNumber) {
                        randomAlbumPosition++;
                    }
                }


            } else if (albumSize == 2) {
                randomAlbumPosition = (lastSongPosition + 1) % albumSize;

                if (runManually && randomAlbumPosition == randomAlbumOldPosition) {
                    Toast.makeText(context, context.getResources()
                                    .getString(R.string.error_random_album_only_two_album),
                            Toast.LENGTH_SHORT).show();
                }
            } else if (runManually) {
                Toast.makeText(context, context.getResources()
                        .getString(R.string.error_random_album_only_one_album), Toast.LENGTH_SHORT)
                        .show();
            }
            return albumList.get(randomAlbumPosition);
        }
        return null;
    }

    private History history;
    private ArrayList<Integer> searchType;
    private boolean isManual;

    public void initSearch(ArrayList<Integer> searchType, boolean isManual) {
        this.searchType = searchType;
        this.isManual = isManual;
    }

    // necessary as albumId are unique but neither in order, neither consecutive (only certainty is albumId > 0)
    private int getAlbumIdPosition(ArrayList<Long> albumIdList, long albumId) {
        int i = 0;
        for (Long id : albumIdList) {
            if (id == albumId) {
                return i;
            }
            i++;
        }
        return -1;
    }

    private boolean isIdForbidden(long id, ArrayList<Long> array) {
        for (Long forbiddenId : array) {
            if (id == forbiddenId) {
                return true;
            }
        }
        return false;
    }

    // fordiddenPosition array as special position
    // index 0 is reserved with lastSong position
    // index 1 is reserved with lastRandomAlbum position
    private Album getRandomAlbum(ArrayList<Album> albumArrayList, int lastSongPosition, ArrayList<Integer> forbiddenSearchPosition, ArrayList<Long> forbiddenSearchId, ArrayList<Integer> forbiddenListenPosition, Context context) {
        Album album;
        int albumSize = albumArrayList.size();

        if (albumSize > 0) {
            int randomAlbumPosition = -1;
            int authorizeAlbumNumber = albumSize - forbiddenSearchPosition.size() - forbiddenListenPosition.size() - 1; // -1 is to take into account lastSongPosition

            if (albumSize == 1) {
                if (isManual) {
                    Toast.makeText(context, context.getResources().getString(R.string.error_random_album_only_one_album), Toast.LENGTH_SHORT).show();
                }
                Log.d("TOTO", "RETURN: array size is 1");
            } else if (albumSize == 2) {
                randomAlbumPosition = (lastSongPosition + 1) % albumSize;

                album = albumArrayList.get(randomAlbumPosition);
                if (isManual && isIdForbidden(album.getId(), history.getSearchHistory())) {
                    Toast.makeText(context, context.getResources().getString(R.string.error_random_album_only_two_album), Toast.LENGTH_SHORT).show();
                }
                Log.d("TOTO", "RETURN: array size is 2");
                return album;
            } else if (authorizeAlbumNumber >  0) {
                Log.d("TOTO", "RETURN: no error");
                ArrayList<Integer> forbiddenPosition = new ArrayList<>();
                forbiddenPosition.add(lastSongPosition);
                forbiddenPosition.addAll(forbiddenListenPosition);
                forbiddenPosition.addAll(forbiddenSearchPosition);

                randomAlbumPosition = randomIntWithinForbiddenNumber(albumSize, forbiddenPosition);
            } else {
                authorizeAlbumNumber = authorizeAlbumNumber + forbiddenSearchPosition.size();

                if (authorizeAlbumNumber > 0) { // search history forbid any new search
                    Log.d("TOTO", "RETURN: search error");
                    ArrayList<Integer> forbiddenPosition = new ArrayList<>();
                    forbiddenPosition.add(lastSongPosition);
                    forbiddenPosition.addAll(forbiddenListenPosition);
                    if (forbiddenSearchPosition.size() > 1) {
                        int firstElementPos = -1;
                        while (firstElementPos == -1) {
                            firstElementPos = getAlbumIdPosition(forbiddenSearchId, history.popSearchHistory()); // first element is now authorized
                            //Log.d("TOTO", String.valueOf(firstElementPos));
                        }
                        forbiddenSearchPosition.remove(firstElementPos);
                        forbiddenPosition.addAll(forbiddenSearchPosition);
                    } else {
                        history.clearSearchHistory();
                    }

                    randomAlbumPosition = randomIntWithinForbiddenNumber(albumSize, forbiddenPosition);
                } else { // search AND listen history forbid any new search, then take first id of listen history
                    Log.d("TOTO", "RETURN: listen error");
                    while (randomAlbumPosition == -1 && history.listenHistory.size() > 0) {
                        randomAlbumPosition = getAlbumPosition(albumArrayList, history.popListenHistory());
                    }

                    while (randomAlbumPosition == -1 && history.searchHistory.size() > 0) {
                        randomAlbumPosition = getAlbumPosition(albumArrayList, history.popSearchHistory());
                    }
                }
            }

            if (randomAlbumPosition != -1) {
                return albumArrayList.get(randomAlbumPosition);
            }
        }
        return null;
    }

    private int randomIntWithinForbiddenNumber(int bound, ArrayList<Integer> forbiddenPosition) {
        Collections.sort(forbiddenPosition); // sorting is needed to get sweet why of getting randomize exclusion

        int random = new Random().nextInt(bound - forbiddenPosition.size());
        int previousForbiddenNumber = -1;
        for (int forbiddenNumber : forbiddenPosition) {
            if (forbiddenNumber != previousForbiddenNumber && random >= forbiddenNumber) {
                random++;
            }
            previousForbiddenNumber = forbiddenNumber;
        }
        return random;
    }

    private boolean searchTypeIsTrue(Song song, Album album) {
        if (searchType.get(0) == BY_GENRE) {
            return album.songs != null && album.songs.size() > 0 &&
                    song.genre.equals(album.songs.get(0).genre);
        } else if (searchType.get(0) == BY_ARTIST) {
            return album.songs != null && album.songs.size() > 0 &&
                    song.artistId == album.songs.get(0).artistId;
        } else {
            return searchType.get(0) == BY_ALBUM;
        }
    }

    public Album search(Song song, Context context) {
        // search inside array to find new random album
        // only one loop should be needed as every position can be found in the same loop
        ArrayList<Integer> forbiddenSearchPosition = new ArrayList<>();
        ArrayList<Long> forbiddenSearchId = new ArrayList<>();
        ArrayList<Integer> forbiddenListenPosition = new ArrayList<>();
        ArrayList<Album> albumArrayList = new ArrayList<>();

        history.setLastAlbumIdSearched(song.albumId);

        if (!isManual) {
            Log.d("TOTO", "AUTOMATIC SEARCH");
            history.clearSearchHistory();
        } else {
            Log.d("TOTO", "MANUAL SEARCH");
        }

        Log.d("TOTO", "Search album id: " + song.albumId);
        for (Long albumId: history.getSearchHistory()) {
            Log.d("TOTO", "Search history: " + albumId + ", name: " + albums.get(getAlbumPosition(albums, albumId)).getTitle());
        }
        for (Long albumId: history.getListenHistory()) {
            Log.d("TOTO", "Listen history: " + albumId + ", name: " + albums.get(getAlbumPosition(albums, albumId)).getTitle());
        }

        int i = 0;
        int lastSongPosition = -1;
        for (Album album: albums) {
            if (searchTypeIsTrue(song, album)) { //condition depend of array searchType
                if (album.getId() == song.albumId) {
                    lastSongPosition = i;
                    Log.d("TOTO", "    forbidden last song pos: " + i + ", name: " + album.getTitle());
                } else if (isIdForbidden(album.getId(), history.getListenHistory())) {
                    forbiddenListenPosition.add(i);
                    Log.d("TOTO", "    forbidden listen pos: " + i + ", name: " + album.getTitle());
                } else if (isIdForbidden(album.getId(), history.getSearchHistory())) {
                    forbiddenSearchPosition.add(i);
                    forbiddenSearchId.add(album.getId());
                    Log.d("TOTO", "    forbidden search pos: " + i + ", name: " + album.getTitle());
                }

                albumArrayList.add(album);
                i++;
            }
        }

        Album album = getRandomAlbum(albumArrayList, lastSongPosition, forbiddenSearchPosition, forbiddenSearchId, forbiddenListenPosition, context);

        if (album != null) {
            Log.d("TOTO", "Album is: " + album.getTitle());
            history.addIdToSearchHistory(album.getId());
            history.synchronizeHistory();
        } /*else { // will be use when fallback is Implemented
            history.revertHistory();
        }*/
        return album;
    }

    public void resetSearchHistory(long albumId) {
        history.clearSearchHistory();
        history.addIdToSearchHistory(albumId);
    }

    public void commit(long albumId) {
        // add id to listen history, this should be the old album not the wanted one
        history.addIdToListenHistory(albumId);
    }

    public Long getLastAlbumIdSearched() {
        return history.getLastAlbumIdSearched();
    }

    // called when shuffling change
    public void stop() {
        // clear history search and listen
        history.stop();
    }

    public void clearSearchHistory() {
        history.clearSearchHistory();
        history.undefinedLastSearchAlbum();
    }

    // called when albums cache change
    public void reloadAlbums(Context context) {
        albums = AlbumLoader.getAllAlbums(context); // not updated when device memory changed, what to do
        genre = new ArrayList<>();
        artist = new ArrayList<>();
    }

    private class History {
        private long lastAlbumIdSearched;
        private ArrayList<Long> searchHistory;
        private ArrayList<Long> listenHistory;

        private ArrayList<Long> originalSearchHistory;
        private ArrayList<Long> originalListenHistory;

        private final int historySize;

        public History(int historySize) {
            this.historySize = historySize;
            this.lastAlbumIdSearched = (long)-1;

            searchHistory = new ArrayList<>();
            listenHistory = new ArrayList<>();

            originalSearchHistory = new ArrayList<>();
            originalListenHistory = new ArrayList<>();
        }

        public ArrayList<Long> getSearchHistory() {
            return searchHistory;
        }

        public ArrayList<Long> getListenHistory() {
            return listenHistory;
        }

        public void clearSearchHistory() {
            searchHistory.clear();
            Log.d("TOTO", "clear search");
        }

        public void undefinedLastSearchAlbum() {
            lastAlbumIdSearched = -1;
            Log.d("TOTO", "undefined last search album");
        }

        private void stop() {
            clearHistory();
            clearOriginalHistory();
            undefinedLastSearchAlbum();
            Log.d("TOTO", "stop");
        }


        private void clearOriginalSearchHistory() {
            originalSearchHistory.clear();
            Log.d("TOTO", "clear original search");
        }

        private void clearHistory() {
            clearSearchHistory();
            listenHistory.clear();
        }

        private void clearOriginalHistory() {
            clearOriginalSearchHistory();
            originalListenHistory.clear();
        }

        public long popListenHistory() {
            if (listenHistory.size() > 0) {
                long id = listenHistory.get(0);
                listenHistory.remove(0);

                Log.d("TOTO", "    pop listen: " + id);

                return id;
            }
            return -1;
        }

        public long popSearchHistory() {
            if (searchHistory.size() > 0) {
                long id = searchHistory.get(0);
                searchHistory.remove(0);

                Log.d("TOTO", "    pop search: " + id);

                return id;
            }
            return -1;
        }

        public void revertHistory() {
            listenHistory = new ArrayList<>(originalListenHistory);
            searchHistory = new ArrayList<>(originalSearchHistory);
        }

        public void synchronizeHistory() {
            originalListenHistory = new ArrayList<>(listenHistory);
            originalSearchHistory = new ArrayList<>(searchHistory);
        }

        public void setLastAlbumIdSearched(Long lastAlbumIdSearched) {
            this.lastAlbumIdSearched = lastAlbumIdSearched;
        }

        public long getLastAlbumIdSearched() {
            return lastAlbumIdSearched;
        }

        private void addIdToHistory(long id, ArrayList<Long> history, ArrayList<Long> originalHistory) {
            if (history.size() >= historySize) {
                history.remove(0);
                originalHistory.remove(0);
            }
            if (!isIdForbidden(id, history)) { // i don't want duplication in this array as it complicate what to do when no new random album can be found because of this array
                history.add(id);
                originalHistory.add(id);
            }
        }

        public void addIdToListenHistory(long id) {
            Log.d("TOTO", "add to listen: " + id);
            clearSearchHistory();
            clearOriginalSearchHistory();

            addIdToHistory(id, listenHistory, originalListenHistory);
        }

        public void addIdToSearchHistory(long id) {
            Log.d("TOTO", "add to search: " + id);
            for (Long idListen : listenHistory) {
                if (id == idListen) {
                    Log.d("TOTO", "not allowed");
                    return;
                }
            }

            addIdToHistory(id, searchHistory, originalSearchHistory);
        }
    }
}
