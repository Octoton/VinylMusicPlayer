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

    private History listenHistory;
    private History searchHistory;
    private long nextRandomAlbumId;
    private long lastAlbumIdSearched;
    private ArrayList<Integer> searchType;
    private boolean isManual;


    static public NextRandomAlbum getInstance() {
        return sInstance;
    }

    private NextRandomAlbum() {
        int historySize = 5; // Should not be too big as history is there only so that we don't listen to the same set of album endlessly

        searchHistory = new History(historySize, "search");
        listenHistory = new History(historySize, "listen");

        lastAlbumIdSearched = -1;
        nextRandomAlbumId = -1;
    }

    public int getAlbumPositionByAlbum(long albumId) {
        return getAlbumPosition(albums, albumId);
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


    private static final int ERROR_ARRAY_SIZE_IS_1 = -2;
    private static final int ERROR_ARRAY_SIZE_IS_0 = -3;
    private static final int ERROR_UNRESOLVED = -4;
    private int getRandomAlbumPosition(int albumSize, History history, int lastSongPosition, int nextRandomAlbumPosition, ArrayList<Integer> forbiddenPositionOfArray, ArrayList<Long> forbiddenIdOfArray, Context context) {
        int randomAlbumPosition;

        if (albumSize > 0) {
            int authorizeAlbumNumber;
            if (nextRandomAlbumPosition != -1) {
                authorizeAlbumNumber = albumSize - forbiddenPositionOfArray.size() - 2; // -1 is to take into account lastSongPosition and nextRandomAlbumPosition
            } else {
                authorizeAlbumNumber = albumSize - forbiddenPositionOfArray.size() - 1; // -1 is to take into account lastSongPosition
            }

            if (albumSize == 1) {
                randomAlbumPosition = ERROR_ARRAY_SIZE_IS_1;
            } else if (albumSize == 2) {
                randomAlbumPosition = (lastSongPosition + 1) % albumSize;
            } else if (authorizeAlbumNumber >  0) {
                ArrayList<Integer> forbiddenPosition = new ArrayList<>();
                forbiddenPosition.add(lastSongPosition);
                if (nextRandomAlbumPosition != -1) {
                    forbiddenPosition.add(nextRandomAlbumPosition);
                }
                forbiddenPosition.addAll(forbiddenPositionOfArray);

                randomAlbumPosition = randomIntWithinForbiddenNumber(albumSize, forbiddenPosition);
            } else {
                authorizeAlbumNumber = authorizeAlbumNumber + forbiddenPositionOfArray.size();

                if (authorizeAlbumNumber > 0) { // history forbid any new search
                    ArrayList<Integer> forbiddenPosition = new ArrayList<>();
                    forbiddenPosition.add(lastSongPosition);
                    if (nextRandomAlbumPosition != -1) {
                        forbiddenPosition.add(nextRandomAlbumPosition);
                    }

                    int firstElementPos = -1;
                    while (firstElementPos == -1) {
                        firstElementPos = getAlbumIdPosition(forbiddenIdOfArray, history.popHistory()); // first element is now authorized
                    }
                    forbiddenPositionOfArray.remove(firstElementPos);
                    forbiddenPosition.addAll(forbiddenPositionOfArray);

                    randomAlbumPosition = randomIntWithinForbiddenNumber(albumSize, forbiddenPosition);
                } else {
                    randomAlbumPosition = ERROR_UNRESOLVED;
                }
            }
        } else {
            randomAlbumPosition = ERROR_ARRAY_SIZE_IS_0;
        }

        return randomAlbumPosition;
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
        ArrayList<Integer> forbiddenPosition = new ArrayList<>();
        ArrayList<Long> forbiddenId = new ArrayList<>();
        ArrayList<Album> albumArrayList = new ArrayList<>();

        lastAlbumIdSearched = song.albumId;

        if (!isManual) {
            Log.d("TOTO", "AUTOMATIC SEARCH");
            searchHistory.clearHistory();
        } else {
            Log.d("TOTO", "MANUAL SEARCH");
        }

        Log.d("TOTO", "Search album id: " + song.albumId);
        for (Long albumId: searchHistory.getHistory()) {
            Log.d("TOTO", "Search history: " + albumId + ", name: " + albums.get(getAlbumPosition(albums, albumId)).getTitle());
        }
        for (Long albumId: listenHistory.getHistory()) {
            Log.d("TOTO", "Listen history: " + albumId + ", name: " + albums.get(getAlbumPosition(albums, albumId)).getTitle());
        }

        int i = 0;
        int lastSongPosition = -1;
        int nextRandomAlbumPosition = -1;
        for (Album album: albums) {
            if (searchTypeIsTrue(song, album)) { //condition depend of array searchType
                if (album.getId() == song.albumId) {
                    lastSongPosition = i;
                    Log.d("TOTO", "    forbidden last song pos: " + i + ", name: " + album.getTitle());
                } else if (album.getId() == nextRandomAlbumId) {
                    nextRandomAlbumPosition = i;
                    Log.d("TOTO", "    forbidden next random album pos: " + i + ", name: " + album.getTitle());
                } else {
                    if (isManual) {
                        if (isIdForbidden(album.getId(), searchHistory.getHistory())) {
                            forbiddenPosition.add(i);
                            forbiddenId.add(album.getId());
                            Log.d("TOTO", "    forbidden search pos: " + i + ", name: " +
                                    album.getTitle());
                        }
                    } else {
                        if (isIdForbidden(album.getId(), listenHistory.getHistory())) {
                            forbiddenPosition.add(i);
                            forbiddenId.add(album.getId());
                            Log.d("TOTO", "    forbidden listen pos: " + i + ", name: " + album.getTitle());
                        }
                    }
                }

                albumArrayList.add(album);
                i++;
            }
        }

        int albumPosition;
        Album album = null;
        if (isManual) {
            albumPosition = getRandomAlbumPosition(albumArrayList.size(), searchHistory, lastSongPosition, nextRandomAlbumPosition, forbiddenPosition, forbiddenId, context);

            if (albumPosition == ERROR_ARRAY_SIZE_IS_1) {
                Toast.makeText(context, context.getResources().getString(R.string.error_random_album_only_one_album), Toast.LENGTH_SHORT).show();
            } else if (albumPosition >= 0) {

                album = albumArrayList.get(albumPosition);
                if (isIdForbidden(album.getId(), searchHistory.getHistory())) {
                    Toast.makeText(context, context.getResources().getString(R.string.error_random_album_only_two_album), Toast.LENGTH_SHORT).show();
                }
            } else if (nextRandomAlbumPosition != -1) {
                album = albumArrayList.get(nextRandomAlbumPosition);
                Toast.makeText(context, "no other album where found with this criteria", Toast.LENGTH_SHORT).show();
            }

            if (album != null) {
                searchHistory.addIdToHistory(nextRandomAlbumId);
                searchHistory.synchronizeHistory();
            }

        } else {
            albumPosition = getRandomAlbumPosition(albumArrayList.size(), listenHistory, lastSongPosition, nextRandomAlbumPosition, forbiddenPosition, forbiddenId, context);

            if (albumPosition >= 0) {
                album = albumArrayList.get(albumPosition);
            } /*else { // will be use when fallback is Implemented
                listenHistory.revertHistory();
            }*/
        }

        if (album != null) {
            Log.d("TOTO", "Album is: " + album.getTitle());

            nextRandomAlbumId = album.getId();
        }
        return album;
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

    // called when albums cache change
    public void reloadAlbums(Context context) {
        albums = AlbumLoader.getAllAlbums(context); // not updated when device memory changed, what to do
        genre = new ArrayList<>();
        artist = new ArrayList<>();
    }

    private class History {
        private ArrayList<Long> history;
        private ArrayList<Long> originalHistory;

        private final int historySize;
        private final String debugName;

        public History(int historySize, String debugName) {
            this.historySize = historySize;
            this.debugName = debugName;

            history = new ArrayList<>();
            originalHistory = new ArrayList<>();
        }

        public ArrayList<Long> getHistory() {
            return history;
        }

        public void clearHistory() {
            history.clear();
            Log.d("TOTO", "clear history " + debugName);
        }

        private void clearOriginalHistory() {
            originalHistory.clear();
            Log.d("TOTO", "clear original " + debugName);
        }

        private void stop() {
            clearHistory();
            clearOriginalHistory();
            Log.d("TOTO", "stop history " + debugName);
        }

        public long popHistory() {
            if (history.size() > 0) {
                long id = history.get(0);
                history.remove(0);

                Log.d("TOTO", "    pop " + debugName + ": " + id);

                return id;
            }
            return -1;
        }

        public void revertHistory() {
            history = new ArrayList<>(originalHistory);
        }

        public void synchronizeHistory() {
            originalHistory = new ArrayList<>(history);
        }

        public void addIdToHistory(long id) {
            if (history.size() >= historySize) {
                history.remove(0);
                originalHistory.remove(0);

                Log.d("TOTO", "remove first element of history " + debugName);
            }
            if (!isIdForbidden(id, history)) { // i don't want duplication in this array as it complicate what to do when no new random album can be found because of this array
                history.add(id);
                originalHistory.add(id);

                Log.d("TOTO", "add to history " + debugName + ": " + id);
            }
        }
    }
}
