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
        constructPositionAlbum(song, albums, previousNextRandomAlbumId, searchHistory, listenHistory);

        int albumPosition = getRandomAlbumPosition(albumArrayList.size(), currentSongPosition, currentlyShownNextRandomAlbumPosition, forbiddenPosition);

        if (albumPosition >= 0)
            return albumArrayList.get(albumPosition);
        else if (setNextSearchType()) // set new search type via global variable or exit with null album
            return foundNextAlbum(song, albums, previousNextRandomAlbumId, listenHistory, searchHistory, context);

        return null;
    }

    private boolean setNextSearchType() {
        boolean nextCriteriaFound = false;

        ArrayList<AlbumShufflingCriteria> searchCriteria = AlbumShufflingUtil.getInstance().getCriteria();
        AlbumShufflingCriteria criteria;
        while (!nextCriteriaFound && fallbackLevel < searchCriteria.size()) {
            criteria = searchCriteria.get(fallbackLevel);
            if (criteria.visible) {
                searchType = criteria.item;
                nextCriteriaFound = true;
            }

            fallbackLevel++;
        }

        return nextCriteriaFound;
    }
}