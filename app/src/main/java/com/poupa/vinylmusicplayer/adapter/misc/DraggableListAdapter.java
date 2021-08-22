package com.poupa.vinylmusicplayer.adapter.misc;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableSwipeableItemViewHolder;
import com.poupa.vinylmusicplayer.databinding.ItemListBinding;
import com.poupa.vinylmusicplayer.util.ViewUtil;


public class DraggableListAdapter extends RecyclerView.Adapter<DraggableListAdapter.ViewHolder> implements
        DraggableItemAdapter<DraggableListAdapter.ViewHolder> {

    String[] listItem;

    public DraggableListAdapter(@NonNull String[] objects) {
        listItem = objects;

        setHasStableIds(true);
    }

    @Override
    public boolean onCheckCanStartDrag(DraggableListAdapter.ViewHolder holder, int position, int x, int y) {
        return ViewUtil.hitTest(holder.imageText, x, y);
    }

    @Override
    public ItemDraggableRange onGetItemDraggableRange(ViewHolder viewHolder, int i) {
        return null;
    }

    @Override
    public void onMoveItem(int fromPosition, int toPosition) {
        String o = listItem[fromPosition];

        listItem[fromPosition] = listItem[toPosition];
        listItem[toPosition] = o;
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
        return listItem[position].hashCode();
    }

    @Override
    public int getItemCount() {
        return listItem.length;
    }

    @NonNull
    @Override
    public DraggableListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // LayoutInflater inflater = LayoutInflater.from(activity);
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        ItemListBinding binding = ItemListBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DraggableListAdapter.ViewHolder holder, int position) {
        holder.imageText.setText(String.valueOf(position));
        holder.imageText.setVisibility(View.VISIBLE);
        holder.title.setText(listItem[position]);
    }

    public class ViewHolder extends AbstractDraggableSwipeableItemViewHolder implements View.OnClickListener, View.OnLongClickListener {

        @Nullable
        public final TextView imageText;

        @Nullable
        public final TextView title;

        public ViewHolder(@NonNull ItemListBinding binding) {
            super(binding.getRoot());

            title = binding.title;
            imageText = binding.imageText;

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
}
