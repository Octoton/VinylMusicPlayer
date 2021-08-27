package com.poupa.vinylmusicplayer.model;

import com.poupa.vinylmusicplayer.App;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.adapter.misc.SelectableItem;
import com.poupa.vinylmusicplayer.model.CategoryInfo.Category;


public class CategoryInfo extends SelectableItem<Category> {

    public CategoryInfo(Category category, boolean visible) {
        super(category, visible);
    }

    @Override
    public String toString() {
        return App.getStaticContext().getResources().getString(item.stringRes);
    }

    public enum Category {
        SONGS(R.string.songs),
        ALBUMS(R.string.albums),
        ARTISTS(R.string.artists),
        GENRES(R.string.genres),
        PLAYLISTS(R.string.playlists);

        public final int stringRes;

        Category(int stringRes) {
            this.stringRes = stringRes;
        }
    }
}
