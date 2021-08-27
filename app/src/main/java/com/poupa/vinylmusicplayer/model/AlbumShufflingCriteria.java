package com.poupa.vinylmusicplayer.model;

import java.io.Serializable;

import com.poupa.vinylmusicplayer.App;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.adapter.misc.SelectableItem;
import com.poupa.vinylmusicplayer.model.AlbumShufflingCriteria.Criteria;

public class AlbumShufflingCriteria extends SelectableItem<Criteria> implements Serializable {

    public AlbumShufflingCriteria(Criteria criteria, boolean visible) {
        super(criteria, visible);
    }

    @Override
    public String toString() {
        return App.getStaticContext().getResources().getString(item.stringRes);
    }

    public enum Criteria {
        ARTIST(R.string.artists),
        GENRE(R.string.genres),
        RANDOM(R.string.random);

        public final int stringRes;

        Criteria(int stringRes) {
            this.stringRes = stringRes;
        }
    }
}
