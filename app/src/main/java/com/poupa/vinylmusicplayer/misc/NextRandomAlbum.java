package com.poupa.vinylmusicplayer.misc;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import android.content.Context;
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
    private Context context;

    public NextRandomAlbum(Context context) {
        this.context = context;
        albums = AlbumLoader.getAllAlbums(this.context); // not updated when device memory changed, what to do
        genre = new ArrayList<Album>();
        artist = new ArrayList<Album>();
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

    public Album getRandomAlbumByAlbum(int lastSongPosition, int randomAlbumOldPosition, boolean runManually) {
        return getRandomAlbum(albums, lastSongPosition, randomAlbumOldPosition, runManually);
    }

    public Album getRandomAlbumByGenre(int lastSongPosition, int randomAlbumOldPosition, boolean runManually) {
        return getRandomAlbum(genre, lastSongPosition, randomAlbumOldPosition, runManually);
    }

    public Album getRandomAlbumByArtist(int lastSongPosition, int randomAlbumOldPosition, boolean runManually) {
        return getRandomAlbum(artist, lastSongPosition, randomAlbumOldPosition, runManually);
    }

    public void constructGenreList(Song song) {
        genre.clear();

        for (Album albumGenre : albums) {
            if (albumGenre.songs != null && albumGenre.songs.size() > 0 && song.genre.equals(albumGenre.songs.get(0).genre)) {
                genre.add(albumGenre);
            }
        }
    }

    public void constructArtistList(Song song) {
        artist.clear();

        for (Album albumArtist : albums) {
            if (albumArtist.songs != null && albumArtist.songs.size() > 0 && song.artistId == albumArtist.songs.get(0).artistId) {
                artist.add(albumArtist);
            }
        }
    }

    // necessary as albumId are unique but neither in order, neither consecutive (only certainty is albumId > 0)
    private int getAlbumPosition(ArrayList<Album> albumList, long albumId) {
        int i = 0;
        for (Album album: albumList) {
            if (album.getId() == albumId) {
                return i;
            }
            i++;
        }
        return -1;
    }

    private Album getRandomAlbum(ArrayList<Album> albumList, int lastSongPosition, int randomAlbumOldPosition, boolean runManually) {
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

                int[] forbiddenNumbers = new int[] {lastSongPosition, randomAlbumOldPosition};
                Arrays.sort(forbiddenNumbers); // sorting is needed to get sweet why of getting randomize exclusion
                randomAlbumPosition = new Random().nextInt(albumSize - forbiddenNumbers.length);
                for (int forbiddenNumber : forbiddenNumbers) {
                    if (randomAlbumPosition >= forbiddenNumber) {
                        randomAlbumPosition++;
                    }
                }


            } else if (albumSize == 2) {
                randomAlbumPosition = (lastSongPosition + 1) % albumSize;

                if (runManually && randomAlbumPosition == randomAlbumOldPosition) {
                    Toast.makeText(context, context.getResources().getString(R.string.error_random_album_only_two_album), Toast.LENGTH_SHORT).show();
                }
            } else if (runManually) {
                Toast.makeText(context, context.getResources().getString(R.string.error_random_album_only_one_album), Toast.LENGTH_SHORT).show();
            }
            return albumList.get(randomAlbumPosition);
        }
        return null;
    }


}
