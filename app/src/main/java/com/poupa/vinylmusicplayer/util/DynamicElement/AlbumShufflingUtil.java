package com.poupa.vinylmusicplayer.util.DynamicElement;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.EnumSet;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.poupa.vinylmusicplayer.App;
import com.poupa.vinylmusicplayer.model.AlbumShufflingCriteria;

public final class AlbumShufflingUtil {

    public static final String PREFERENCE_KEY = "album_shuffling_preference";
    public static final String CRITERION = "criterion";

    private static AlbumShufflingUtil sInstance;
    private final SharedPreferences mPreferences;

    private AlbumShufflingUtil() {
        mPreferences = App.getStaticContext().getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE);
    }

    public static AlbumShufflingUtil getInstance() {
        if (sInstance == null) {
            sInstance = new AlbumShufflingUtil();
        }
        return sInstance;
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
