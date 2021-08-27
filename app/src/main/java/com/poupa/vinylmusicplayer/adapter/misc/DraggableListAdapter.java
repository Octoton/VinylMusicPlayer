package com.poupa.vinylmusicplayer.adapter.misc;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.databinding.PreferenceDialogLibraryCategoriesListitemBinding;
import com.poupa.vinylmusicplayer.util.SwipeAndDragHelper;


public class DraggableListAdapter extends RecyclerView.Adapter<DraggableListAdapter.ViewHolder> implements SwipeAndDragHelper.ActionCompletionContract {

    private ArrayList<Item> listItem;
    private final ItemTouchHelper touchHelper;

    // next step:
    // public DraggableListAdapter(@NonNull ArrayList<Item> objects) {
    // as fragment will remember what you have selected (with parcelable)
    public DraggableListAdapter(@NonNull String[] objects) {
        listItem = new ArrayList<>();

        for (String object : objects) {
            listItem.add(new Item(object, true));
        }

        SwipeAndDragHelper swipeAndDragHelper = new SwipeAndDragHelper(this);
        touchHelper = new ItemTouchHelper(swipeAndDragHelper);
    }

    @Override
    public void onViewMoved(int oldPosition, int newPosition) {
        Item item = listItem.remove(oldPosition);
        listItem.add(newPosition, item);

        notifyItemMoved(oldPosition, newPosition);
    }

    @Override
    public int getItemCount() {
        return listItem.size();
    }

    @NonNull
    @Override
    public DraggableListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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

    @SuppressLint("ClickableViewAccessibility")
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

        holder.dragView.setOnTouchListener((view, event) -> {
                    if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                        touchHelper.startDrag(holder);
                    }
                    return false;
                }
        );
    }

    public void attachToRecyclerView(RecyclerView recyclerView) {
        touchHelper.attachToRecyclerView(recyclerView);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public final View dragView;
        public final com.kabouzeid.appthemehelper.common.views.ATECheckBox checkBox;
        public final TextView title;

        public ViewHolder(@NonNull PreferenceDialogLibraryCategoriesListitemBinding binding) {
            super(binding.getRoot());

            title = binding.title;
            dragView = binding.dragView;
            checkBox = binding.checkbox;

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
