package com.poupa.vinylmusicplayer.adapter.misc;

import java.io.Serializable;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.databinding.PreferenceDialogLibraryCategoriesListitemBinding;
import com.poupa.vinylmusicplayer.model.CategoryInfo;
import com.poupa.vinylmusicplayer.util.SwipeAndDragHelper;


public class DraggableListAdapter<T extends SelectableItem<U>, U extends Serializable> extends RecyclerView.Adapter<DraggableListAdapter.ViewHolder> implements SwipeAndDragHelper.ActionCompletionContract {

    protected ArrayList<T> listItem;
    protected final ItemTouchHelper touchHelper;

    public DraggableListAdapter(@NonNull ArrayList<T> objects) {
        listItem = objects;

        SwipeAndDragHelper swipeAndDragHelper = new SwipeAndDragHelper(this);
        touchHelper = new ItemTouchHelper(swipeAndDragHelper);
    }

    @Override
    public void onViewMoved(int oldPosition, int newPosition) {
        T item = listItem.remove(oldPosition);
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

    public ArrayList<T> getSelectableItems() { return listItem; }

    @SuppressLint("NotifyDataSetChanged")
    public void setSelectableItems(ArrayList<T> listItem) {
        this.listItem = listItem;
        notifyDataSetChanged();
    }

    protected boolean isLastCheckedItem(T item) {
        if (item.visible) {
            for (T i : listItem) {
                if (i != item && i.visible) return false;
            }
        }
        return true;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull DraggableListAdapter.ViewHolder holder, int position) {
        T o = listItem.get(position);

        holder.title.setText(o.toString());

        holder.checkBox.setChecked(o.visible);

        holder.dragView.setOnTouchListener((view, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                touchHelper.startDrag(holder);
            }
            return false;
        });

        holder.itemView.setOnClickListener(v -> {
            checkBoxOnClick(holder, o);
        });
    }

    protected void checkBoxOnClick(@NonNull DraggableListAdapter.ViewHolder holder, T object) {
        object.visible = !object.visible;
        holder.checkBox.setChecked(object.visible);
    }

    public void attachToRecyclerView(RecyclerView recyclerView) {
        touchHelper.attachToRecyclerView(recyclerView);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View dragView;
        public final CheckBox checkBox;
        public final TextView title;

        public ViewHolder(@NonNull PreferenceDialogLibraryCategoriesListitemBinding binding) {
            super(binding.getRoot());

            title = binding.title;
            dragView = binding.dragView;
            checkBox = binding.checkbox;

        }
    }
}
