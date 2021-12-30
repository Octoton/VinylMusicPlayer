package com.poupa.vinylmusicplayer.misc.queue.DynamicElement.AlbumShuffling.Search;


import java.util.ArrayList;

import android.content.Context;

import com.poupa.vinylmusicplayer.misc.queue.DynamicElement.AlbumShuffling.AlbumShufflingCriteria;
import com.poupa.vinylmusicplayer.misc.queue.DynamicElement.AlbumShuffling.AlbumShufflingUtil;
import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.model.Song;


/*
 * AutomaticSearch: Class that search the next random album to be played
 *                  For this, all albums that follow the search criteria set in preferences but not present in listenHistory are found and take one randomly
 *                  If no album are found, go to the next preferences criteria until something is found
 *                  If nothing is possible given the criteria, no album will be played at the end of the playlist
 */
public class AutomaticSearch extends Search {
    @Override
    public boolean isManual() {
        return false;
    }

    private int fallbackLevel;

    public AutomaticSearch() {
        super();
        fallbackLevel = 0;
        setNextSearchType();
    }

    @Override
    public Album foundNextAlbum(Song song, ArrayList<Album> albums, long previousNextRandomAlbumId, History listenHistory, History searchHistory, Context context) {
        Album album = null;

        constructPositionAlbum(song, albums, previousNextRandomAlbumId, searchHistory, listenHistory);

        int albumPosition = getRandomAlbumPosition(albumArrayList.size(), currentSongPosition, currentlyShownNextRandomAlbumPosition, forbiddenPosition);

        if (albumPosition >= 0) {
            album = albumArrayList.get(albumPosition);
        } else if (setNextSearchType()) { // will be use when fallback is Implemented
            //listenHistory.revertHistory(); really ??? only if error_history is taken into account (doesn't seems to be useful for auto)
            //set new search type via global variable + exit condition with null album
            return foundNextAlbum(song, albums, previousNextRandomAlbumId, listenHistory, searchHistory, context);
        }

        return album;
    }

    private boolean setNextSearchType() {
        boolean hasNext = false;

        ArrayList<AlbumShufflingCriteria> searchCriteria = AlbumShufflingUtil.getInstance().getCriteria();
        AlbumShufflingCriteria criteria;
        do {
            criteria = searchCriteria.get(fallbackLevel);
            if (criteria.visible) {
                searchType = criteria.item;
                hasNext = true;
            }

            fallbackLevel++;
        } while (!hasNext && fallbackLevel < searchCriteria.size());

        return hasNext;
    }
}