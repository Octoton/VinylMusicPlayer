package com.poupa.vinylmusicplayer.adapter.misc;

import java.util.ArrayList;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableSwipeableItemViewHolder;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.databinding.PreferenceDialogLibraryCategoriesListitemBinding;
import com.poupa.vinylmusicplayer.util.ViewUtil;


public class DraggableListAdapter extends RecyclerView.Adapter<DraggableListAdapter.ViewHolder> implements
        DraggableItemAdapter<DraggableListAdapter.ViewHolder> {

    private ArrayList<Item> listItem;

    // next step:
    // public DraggableListAdapter(@NonNull ArrayList<Item> objects) {
    // as fragment will remember what you have selected (with parcelable)
    public DraggableListAdapter(@NonNull String[] objects) {
        listItem = new ArrayList<>();

        for (String object : objects) {
            listItem.add(new Item(object, true));
        }

        setHasStableIds(true);
    }

    @Override
    public boolean onCheckCanStartDrag(DraggableListAdapter.ViewHolder holder, int position, int x, int y) {
        return ViewUtil.hitTest(holder.dragView, x, y);
    }

    @Override
    public ItemDraggableRange onGetItemDraggableRange(ViewHolder viewHolder, int i) {
        return null;
    }

    @Override
    public void onMoveItem(int fromPosition, int toPosition) {
        Item item = listItem.remove(fromPosition);
        listItem.add(toPosition, item);
    }

    @Override
    public boolean onCheckCanDrop(int draggingPosition, int dropPosition) {
        return true;
    }

    @Override
    public void onItemDragStarted(int position) {
        notifyDataSetChanged();
    }

    @Override
    public void onItemDragFinished(int fromPosition, int toPosition, boolean result) {
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return listItem.get(position).hashCode();
    }

    @Override
    public int getItemCount() {
        return listItem.size();
    }

    @NonNull
    @Override
    public DraggableListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // LayoutInflater inflater = LayoutInflater.from(activity);
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        PreferenceDialogLibraryCategoriesListitemBinding binding =
                PreferenceDialogLibraryCategoriesListitemBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    private boolean isLastCheckedItem(Item item) {
        if (item.visible) {
            for (Item i : listItem) {
                if (i != item && i.visible) return false;
            }
        }
        return true;
    }

    @Override
    public void onBindViewHolder(@NonNull DraggableListAdapter.ViewHolder holder, int position) {
        Item o = listItem.get(position);

        holder.title.setText(o.item);

        holder.checkBox.setChecked(o.visible);

        holder.itemView.setOnClickListener(v -> {
            if (!(o.visible && isLastCheckedItem(o))) {
                o.visible = !o.visible;
                holder.checkBox.setChecked(o.visible);
            } else {
                Toast.makeText(holder.itemView.getContext(), R.string.you_have_to_select_at_least_one_category, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public class ViewHolder extends AbstractDraggableSwipeableItemViewHolder implements View.OnClickListener, View.OnLongClickListener {
        
        public final com.poupa.vinylmusicplayer.views.IconImageView dragView;
        public final com.kabouzeid.appthemehelper.common.views.ATECheckBox checkBox;
        public final TextView title;

        public ViewHolder(@NonNull PreferenceDialogLibraryCategoriesListitemBinding binding) {
            super(binding.getRoot());

            title = binding.title;
            dragView = binding.dragView;
            checkBox = binding.checkbox;

            final View itemView = binding.getRoot();
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public View getSwipeableContainerView() {
            return null;
        }

        @Override
        public boolean onLongClick(View v) {
            return false;
        }

        @Override
        public void onClick(View v) {
        }
    }

    public class Item { //implements Parcelable {
        public String item;
        public boolean visible;

        public Item(String item, boolean visible) {
            this.item = item;
            this.visible = visible;
        }

        /*private item(Parcel source) {
            item = (String) source.readSerializable();
            visible = source.readInt() == 1;
        }


        @Override
        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeSerializable(category);
            dest.writeInt(visible ? 1 : 0);
        }

        public static final Parcelable.Creator<com.poupa.vinylmusicplayer.model.CategoryInfo> CREATOR = new Parcelable.Creator<com.poupa.vinylmusicplayer.model.CategoryInfo>() {
            public com.poupa.vinylmusicplayer.model.CategoryInfo createFromParcel(Parcel source) {
                return new com.poupa.vinylmusicplayer.model.CategoryInfo(source);
            }

            public com.poupa.vinylmusicplayer.model.CategoryInfo[] newArray(int size) {
                return new com.poupa.vinylmusicplayer.model.CategoryInfo[size];
            }
        }; */

    }
}
