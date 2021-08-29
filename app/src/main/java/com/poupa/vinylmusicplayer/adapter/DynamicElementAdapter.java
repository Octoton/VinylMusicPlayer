package com.poupa.vinylmusicplayer.adapter;

import java.util.ArrayList;
import android.os.Handler;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.adapter.misc.DraggableListAdapter;
import com.poupa.vinylmusicplayer.model.AlbumShufflingCriteria;

import static com.poupa.vinylmusicplayer.model.AlbumShufflingCriteria.Criteria.RANDOM;


public class DynamicElementAdapter extends DraggableListAdapter<AlbumShufflingCriteria, AlbumShufflingCriteria.Criteria> {

    public DynamicElementAdapter(ArrayList<AlbumShufflingCriteria> criteria) {
        super(criteria);
    }

    @Override
    protected void checkBoxOnClick(@NonNull DraggableListAdapter.ViewHolder holder, AlbumShufflingCriteria object) {
        if ((!object.visible && !isBelowRandom(object)) || (object.visible && !isLastCheckedItem(object))) {
            object.visible = !object.visible;
            holder.checkBox.setChecked(object.visible);

            if (object.item == RANDOM && object.visible)
                resetVisibilityAfterRandom();
        } else if (!object.visible) {
            Toast.makeText(holder.itemView.getContext(), R.string.you_cannot_activate_criteria_after_random, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(holder.itemView.getContext(), R.string.you_have_to_select_at_least_one_criteria, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onViewActionEnded() {
        resetVisibilityAfterRandom();
    }

    private void resetVisibilityAfterRandom() {
        boolean randomIsBehind = false;

        int position=0;
        for (AlbumShufflingCriteria o : listItem) {
            if (randomIsBehind && o.visible) {
                o.visible = false;

                notifyItemChanged(position);
            }

            if (o.item == RANDOM && o.visible) {
                randomIsBehind = true;
            }

            position++;
        }
    }

    private boolean isBelowRandom(AlbumShufflingCriteria object) {
        if (object.item == RANDOM) return false;

        boolean randomIsBehind = false;
        boolean allHasChanged = false;
        for (AlbumShufflingCriteria o : listItem) {
            if (randomIsBehind && o.equals(object)) {
                allHasChanged = true;
            }

            if (o.item == RANDOM && o.visible) {
                randomIsBehind = true;
            }
        }

        return allHasChanged;
    }

    public void setCriteria(ArrayList<AlbumShufflingCriteria> criteria) {
        super.setSelectableItems(criteria);
    }

    public ArrayList<AlbumShufflingCriteria> getCriteria() {
        return getSelectableItems();
    }
}

