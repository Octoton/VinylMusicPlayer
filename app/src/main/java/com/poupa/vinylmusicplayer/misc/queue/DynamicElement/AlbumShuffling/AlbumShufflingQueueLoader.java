package com.poupa.vinylmusicplayer.misc.queue.DynamicElement.AlbumShuffling;


import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.discog.Discography;
import com.poupa.vinylmusicplayer.misc.queue.DynamicElement.AbstractShuffling.AbstractQueueLoader;
import com.poupa.vinylmusicplayer.misc.queue.DynamicElement.AlbumShuffling.AlbumShufflingCriteria.Criteria;
import com.poupa.vinylmusicplayer.misc.queue.DynamicElement.DynamicElement;
import com.poupa.vinylmusicplayer.misc.queue.DynamicElement.DynamicQueueItemAdapter;
import com.poupa.vinylmusicplayer.misc.queue.DynamicElement.DynamicQueueLoader;
import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.util.MusicUtil;


/** Album shuffling implementation of {@link com.poupa.vinylmusicplayer.misc.queue.DynamicElement.DynamicQueueLoader} */
public class AlbumShufflingQueueLoader extends AbstractQueueLoader {
    public static final String SEARCH_TYPE = "search_type";

    private final DB database;
    private Album nextAlbum;

    public AlbumShufflingQueueLoader() {
        super();

        this.nextAlbum = new Album();
        this.database = new DB();
    }

    @Override
    public boolean restoreQueue(Context context, Song song) {
        this.nextAlbum = Discography.getInstance().getAlbum(database.fetchNextRandomAlbumId());

        return super.restoreQueue(song);
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

    // /!\ in full auto, nextAlbum is null when calling this function even when changing settings which is a shame
    @Override
    public boolean setNextDynamicQueue(Context context, Song song, boolean force) {
        if (!super.setNextDynamicQueue(null, context, song, force))
            return false;

        ArrayList<AlbumShufflingCriteria> searchCriteria = AlbumShufflingUtil.getInstance().getCriteria();
        Album album = null;
        boolean foundSomething = false;

        int i = 0;

        do {
            AlbumShufflingCriteria criteria = searchCriteria.get(i);

            if (criteria.visible) {
                Bundle bundle = new Bundle();
                bundle.putInt(AlbumShufflingQueueLoader.SEARCH_TYPE, criteria.item.id);

                album = search(bundle, song);
                if (album != null) {
                    foundSomething = true;
                }
            }
            i++;
        } while (!foundSomething && i < searchCriteria.size());

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

        Album album = search(criteria, song);

        if (album != null) {
            this.nextAlbum = album;
            this.database.setNextRandomAlbumId(album.getId());
        } else {
            Toast.makeText(context, context.getResources().getString(R.string.no_other_album_found), Toast.LENGTH_SHORT).show();
        }

        return true;
    }

    private Album search(Bundle criteria, Song song) {
        int searchType = criteria.getInt(SEARCH_TYPE);

        ArrayList<Album> albums;
        synchronized (Discography.getInstance()) {
            albums = new ArrayList<>(Discography.getInstance().getAllAlbums());
        }

        ArrayList<Album> subList = new ArrayList<>();
        boolean isAlbumInCriteria = false;
        for (Album album : albums) {
            if (song.albumId != album.getId() && (this.nextAlbum == null || this.nextAlbum.getId() != album.getId())) {
                if (searchType == Criteria.RANDOM.id) {
                    isAlbumInCriteria = true;
                } else if (searchType == Criteria.ARTIST.id) {
                    isAlbumInCriteria = album.getArtistId() == song.artistId;
                } else if (searchType == Criteria.GENRE.id) {
                    isAlbumInCriteria = album.songs != null && album.songs.size() > 0 &&
                                song.genre.equals(album.songs.get(0).genre);
                }

                if (isAlbumInCriteria) {
                    subList.add(album);
                }
            }
        }

        Album album = null;
        if (subList.size() > 0) {
            Random rand = new Random();
            album = subList.get(rand.nextInt(subList.size()));
        }

        return album;
    }

    public static ArrayList<Song> getNextRandomQueue() {
        ArrayList<Album> albums;
        synchronized (Discography.getInstance()) {
            albums = new ArrayList<>(Discography.getInstance().getAllAlbums());
        }
        Random rand = new Random();
        Album album = albums.get(rand.nextInt(albums.size()));

        return album.songs;
    }

    public boolean isNextQueueEmpty() {
        return  (nextAlbum == null);
    }

    public ArrayList<Song> getNextQueue() {
        if (isNextQueueEmpty())
            return null;

        return nextAlbum.songs;
    }

    @Override
    protected DynamicElement createEmptyDynamicElement(Context context) {
        return new DynamicElement(context.getResources().getString(R.string.next_album),
                context.getResources().getString(R.string.no_album_found),
                R.drawable.ic_shuffle_album_white_24dp); //"-");
    }

    @Override
    protected DynamicElement createNewDynamicElement(Context context) {
        return new DynamicElement(context.getResources().getString(R.string.next_album),
                MusicUtil.buildInfoString(this.nextAlbum.getArtistName(), this.nextAlbum.getTitle()),
                R.drawable.ic_shuffle_album_white_24dp); //"-");
    }

    @Override
    protected boolean isSongDifferentEnough(@NonNull Song song) {
        return (song.albumId != songUsedForSearching.albumId);
    }
}
