package com.poupa.vinylmusicplayer.misc.RandomAlbum;


import java.util.ArrayList;

import android.util.Log;


public class History {
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

    public void clearOriginalHistory() {
        originalHistory.clear();
        Log.d("TOTO", "clear original " + debugName);
    }

    public void stop() {
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

    static public boolean isIdForbidden(long id, ArrayList<Long> array) {
        for (Long forbiddenId : array) {
            if (id == forbiddenId) {
                return true;
            }
        }
        return false;
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
