package com.poupa.vinylmusicplayer.adapter;

import androidx.annotation.NonNull;

import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.adapter.misc.DraggableListAdapter;
import com.poupa.vinylmusicplayer.model.CategoryInfo;
import com.poupa.vinylmusicplayer.util.SafeToast;

import java.util.ArrayList;

public class CategoryInfoAdapter extends DraggableListAdapter<CategoryInfo, CategoryInfo.Category> {

    public CategoryInfoAdapter(ArrayList<CategoryInfo> categoryInfos) {
        super(categoryInfos);
    }

    @Override
    protected void checkBoxOnClick(@NonNull DraggableListAdapter.ViewHolder holder, CategoryInfo object) {
        if (!(object.visible && isLastCheckedItem(object))) {
            object.visible = !object.visible;
            holder.checkBox.setChecked(object.visible);
        } else {
            SafeToast.show(holder.itemView.getContext(), R.string.you_have_to_select_at_least_one_category);
        }
    }

    public void setCategoryInfos(ArrayList<CategoryInfo> categoryInfos) {
        super.setSelectableItems(categoryInfos);
    }

    public ArrayList<CategoryInfo> getCategoryInfos() {
        return getSelectableItems();
    }
}

