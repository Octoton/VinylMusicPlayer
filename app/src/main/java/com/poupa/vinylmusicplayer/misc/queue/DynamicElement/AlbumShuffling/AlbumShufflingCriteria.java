package com.poupa.vinylmusicplayer.misc.queue.DynamicElement.AlbumShuffling;

import java.io.Serializable;

import com.poupa.vinylmusicplayer.App;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.adapter.misc.SelectableItem;
import com.poupa.vinylmusicplayer.misc.queue.DynamicElement.AlbumShuffling.AlbumShufflingCriteria.Criteria;
import com.poupa.vinylmusicplayer.misc.queue.DynamicElement.DynamicElementBottomSheetDialog.Type;


public class AlbumShufflingCriteria extends SelectableItem<Criteria> implements Serializable {

    public AlbumShufflingCriteria(Criteria criteria, boolean visible) {
        super(criteria, visible);
    }

    @Override
    public String toString() {
        return App.getStaticContext().getResources().getString(Criteria.getStringRes(item));
    }

    private final static Criteria[] criteriaValues = Criteria.values();
    public enum Criteria {
        ARTIST(0),
        GENRE(1),
        RANDOM(2);

        /** as the id is saved in shared preference to remember user chose, existing value should not changed or swapped **/
        public final int id;

        Criteria(int id) {
            this.id = id;
        }

        public static int getStringRes (Criteria e) {
            switch (e) {
                case ARTIST:
                    return R.string.artists;
                case GENRE:
                    return R.string.genres;
                case RANDOM:
                default:
                    return R.string.random;
            }
        }

        public static Criteria toCriteria (int id) {
            for (Criteria type : criteriaValues) {
                if (type.id == id) {
                    return type;
                }
            }
            return null;
        }
    }
}
