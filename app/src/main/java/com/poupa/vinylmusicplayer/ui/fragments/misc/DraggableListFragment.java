package com.poupa.vinylmusicplayer.ui.fragments.misc;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupMenu;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.adapter.misc.DraggableListAdapter;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;


public class DraggableListFragment extends BottomSheetDialogFragment {

    public RecyclerViewDragDropManager recyclerViewDragDropManager;
    public RecyclerView.Adapter wrappedAdapter;
    public LinearLayoutManager layoutManager;

    public static DraggableListFragment newInstance() {
        return new DraggableListFragment();
    }

    @Override
    public void onDestroyView() {
        if (recyclerViewDragDropManager != null) {
            recyclerViewDragDropManager.release();
            recyclerViewDragDropManager = null;
        }

        if (wrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(wrappedAdapter);
            wrappedAdapter = null;
        }
        layoutManager = null;
        super.onDestroyView();
    }

    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        BottomSheetDialog dialog = new BottomSheetDialog(getActivity());

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                BottomSheetDialog d = (BottomSheetDialog) dialog;
                FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);

                BottomSheetBehavior behaviour = BottomSheetBehavior.from(bottomSheet);
                behaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
                behaviour.setDraggable(false);
                behaviour.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                    @Override public void onStateChanged(@NonNull View bottomSheet, int newState) {
                        if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                            behaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        }
                    }

                    @Override
                    public void onSlide(@NonNull View bottomSheet, float slideOffset) {

                    }
                });
            }
        });

        return dialog;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final GeneralItemAnimator animator = new DraggableItemAnimator();
        recyclerViewDragDropManager = new RecyclerViewDragDropManager();

        View view = inflater.inflate(R.layout.bottom_sheet_dynamic_element_preference, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);

        String[] listItem = this.getResources().getStringArray(R.array.album_shuffling_order);
        DraggableListAdapter adapter = new DraggableListAdapter(listItem);

        wrappedAdapter = recyclerViewDragDropManager.createWrappedAdapter(adapter);
        layoutManager = new LinearLayoutManager(this.getContext());

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(wrappedAdapter);
        recyclerView.setItemAnimator(animator);

        recyclerViewDragDropManager.attachRecyclerView(recyclerView);

        Button button = view.findViewById(R.id.btnShow);
        button.setText("Album");
        button.setOnClickListener(v -> {
            showMenu(this.getContext(), v, R.menu.menu_dynamic_element_type);
        });


        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        view.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return view;
    }

    private void showMenu(Context context, View view, int menuRes) {
        PopupMenu popupMenu = new PopupMenu(context, view);

        popupMenu.inflate(menuRes);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Button button = view.findViewById(R.id.btnShow);
                button.setText(menuItem.getTitle());
                return true;
            }
        });

        popupMenu.show();
    }
}
