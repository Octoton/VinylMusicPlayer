package com.poupa.vinylmusicplayer.misc.queue.DynamicElement.AlbumShuffling;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.EnumSet;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.poupa.vinylmusicplayer.App;
import com.poupa.vinylmusicplayer.misc.queue.DynamicElement.AlbumShuffling.Search.History;


public final class AlbumShufflingUtil {

    public static final String PREFERENCE_KEY = "album_shuffling_preference";
    public static final String CRITERION = "criterion";
    public static final String HISTORY_SIZE = "history_size";

    private static AlbumShufflingUtil sInstance;
    private final SharedPreferences mPreferences;

    private final History listenHistory; // already listen album
    private final History searchHistory; // manually searched album (with 3-dot menu) on this playlist (before going to next random album)

    private AlbumShufflingUtil() {
        mPreferences = App.getStaticContext().getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE);

        int historySize = getHistorySize();
        searchHistory = new History(historySize, false);
        listenHistory = new History(historySize, true);
    }

    public static AlbumShufflingUtil getInstance() {
        if (sInstance == null) {
            sInstance = new AlbumShufflingUtil();
        }
        return sInstance;
    }

    public int getDefaultHistorySize() {
        return 5;
    }

    public void resetHistorySize() {
        int historySize = getDefaultHistorySize();
        mPreferences.edit().putInt(HISTORY_SIZE, historySize).apply();

        setHistoriesSize(historySize);
    }

    public void updateHistorySize(int historySize) {
        mPreferences.edit().putInt(HISTORY_SIZE, historySize).apply();

        setHistoriesSize(historySize);
    }

    public final int getHistorySize() {
        return mPreferences.getInt(HISTORY_SIZE, getDefaultHistorySize()); // should be shared with empty case and reset
    }

    private void setHistoriesSize(int size) {
        searchHistory.setHistorySize(size);
        listenHistory.setHistorySize(size);
    }

    public History getListenHistory() {
        return listenHistory;
    }

    public History getSearchHistory() {
        return searchHistory;
    }

    public void resetSearchHistory() {
        searchHistory.setHistory(listenHistory);
    }

    public void restoreHistories() {
        searchHistory.clearHistory();

        listenHistory.fetchHistory();
    }

    // next random album is been loaded into queue, thus old one as been listen too
    public void commitHistories(long albumId) {
        // add id to listen history, this should be the old album not the wanted one
        listenHistory.addIdToHistory(albumId, true);
        searchHistory.clearHistory();
    }

    // called when random album shuffling mode end
    public void stopHistories() {
        // clear history search and listen
        searchHistory.stop();
        listenHistory.stop();
    }

    public void setCriteria(ArrayList<AlbumShufflingCriteria> criterion) {
        Gson gson = new Gson();
        Type collectionType = new TypeToken<ArrayList<AlbumShufflingCriteria>>() {
        }.getType();

        mPreferences.edit()
                .putString(CRITERION, gson.toJson(criterion, collectionType))
                .apply();
    }

    public ArrayList<AlbumShufflingCriteria> getCriteria() {
        String data = mPreferences.getString(CRITERION, null);
        if (data != null) {
            Gson gson = new Gson();
            Type collectionType = new TypeToken<ArrayList<AlbumShufflingCriteria>>() {
            }.getType();

            try {
                return gson.fromJson(data, collectionType);
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
            }
        }

        return getDefaultCriteria();
    }

    public ArrayList<AlbumShufflingCriteria> getDefaultCriteria() {
        ArrayList<AlbumShufflingCriteria> defaultCriterion = new ArrayList<>();

        ArrayList<AlbumShufflingCriteria.Criteria> objects = new ArrayList<>(EnumSet.allOf(AlbumShufflingCriteria.Criteria.class));
        for (AlbumShufflingCriteria.Criteria object : objects) {
            defaultCriterion.add(new AlbumShufflingCriteria(object, true));
        }

        return defaultCriterion;
    }
}
