package com.poupa.vinylmusicplayer.misc.queue.AlbumShuffling;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.helper.MusicPlayerRemote;
import com.poupa.vinylmusicplayer.misc.queue.AbstractShuffling.AbstractQueueItemAdapter;
import com.poupa.vinylmusicplayer.ui.fragments.misc.DynamicElementBottomSheetDialog;


/** Album shuffling implementation of {@link com.poupa.vinylmusicplayer.misc.queue.DynamicQueueItemAdapter} */
public class AlbumShufflingQueueItemAdapter extends AbstractQueueItemAdapter {

    public AlbumShufflingQueueItemAdapter() {
        super();
    }

    @Override
    public void reloadDynamicElement() {
        super.reloadDynamicElement();
    }

    public int getSongMenuRes(int itemViewType) {
        return R.menu.menu_item_playing_queue_album_shuffling;
    }

    public boolean onSongMenuItemClick(MenuItem item, Context context) {
        Bundle bundle = new Bundle();
        if (item.getItemId() == R.id.action_shuffle_random_album) {
            // Refresh proposed next random album with one completely random
            bundle.putInt(AlbumShufflingQueueLoader.SEARCH_TYPE, AlbumShufflingQueueLoader.RANDOM_SEARCH);
            MusicPlayerRemote.setNextDynamicQueue(bundle);
            return true;
        } else if (item.getItemId() == R.id.action_shuffle_artist_album) {
            // Refresh proposed next random album with one with the same artist than the songs I am listening to right now
            bundle.putInt(AlbumShufflingQueueLoader.SEARCH_TYPE, AlbumShufflingQueueLoader.ARTIST_SEARCH);
            MusicPlayerRemote.setNextDynamicQueue(bundle);
            return true;
        } else if (item.getItemId() == R.id.action_shuffle_genre_album) {
            // Refresh proposed next random album with one with the same genre than the songs I am listening to right now
            bundle.putInt(AlbumShufflingQueueLoader.SEARCH_TYPE, AlbumShufflingQueueLoader.GENRE_SEARCH);
            MusicPlayerRemote.setNextDynamicQueue(bundle);
            return true;
        } else if (item.getItemId() == R.id.action_delete_dynamic_element) {
            MusicPlayerRemote.setQueueToStaticQueue();
        } else if (item.getItemId() == R.id.pref) {
            showBottomSheetDialog(context);
        }
        return false;
    }


    private void showBottomSheetDialog(Context context) {
        DynamicElementBottomSheetDialog dynamicElementBottomSheetDialog = DynamicElementBottomSheetDialog
                .newInstance();

        dynamicElementBottomSheetDialog
                .show( ((AppCompatActivity) context).getSupportFragmentManager(), "dynamic_element_bottom_sheet");
    }



}
