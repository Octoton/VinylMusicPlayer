package com.poupa.vinylmusicplayer.ui.fragments.misc;


import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.adapter.DynamicElementAdapter;
import com.poupa.vinylmusicplayer.model.AlbumShufflingCriteria;
import com.poupa.vinylmusicplayer.util.DynamicElement.AlbumShufflingUtil;

import static java.lang.Integer.parseInt;


public class DynamicElementBottomSheetDialog extends BottomSheetDialogFragment {
    public static DynamicElementBottomSheetDialog newInstance() { return new DynamicElementBottomSheetDialog(); }

    private DynamicElementAdapter adapter;

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
        View view = inflater.inflate(R.layout.bottom_sheet_dynamic_element_preference, container, false);

        AlbumShufflingPreferenceFragment myfragment = new AlbumShufflingPreferenceFragment();

        getChildFragmentManager().beginTransaction()
                .add(R.id.testFragment, myfragment)
                .commit();

        /* to move to new album preference fragment */
        ArrayList<AlbumShufflingCriteria> criteria;
        if (savedInstanceState != null) {
            criteria = savedInstanceState.getParcelableArrayList(AlbumShufflingUtil.CRITERION);
        } else {
            criteria = AlbumShufflingUtil.getInstance().getCriteria();
        }
        adapter = new DynamicElementAdapter(criteria);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerView.setAdapter(adapter);

        adapter.attachToRecyclerView(recyclerView);

        EditText history = (EditText)view.findViewById(R.id.history_size);
        history.setText(String.valueOf(AlbumShufflingUtil.getInstance().getHistorySize()));

        Button button = view.findViewById(R.id.btnShow);
        button.setText("Album");
        button.setOnClickListener(v -> {
            showMenu(this.getContext(), v, R.menu.menu_dynamic_element_type);
        });

        view.findViewById(R.id.reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getChildFragmentManager().beginTransaction().remove(myfragment).commit();
                //AlbumShufflingPreferenceFragment toto = new AlbumShufflingPreferenceFragment();
                //getChildFragmentManager().beginTransaction().replace(R.id.testFragment, toto).commit();

                updateCriterion(AlbumShufflingUtil.getInstance().getDefaultCriteria());
                adapter.setCriteria(AlbumShufflingUtil.getInstance().getDefaultCriteria());

                EditText history = (EditText)view.findViewById(R.id.history_size);
                AlbumShufflingUtil.getInstance().resetHistorySize();
                history.setText(String.valueOf(AlbumShufflingUtil.getInstance().getHistorySize()));

            }
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
                updateCriterion(adapter.getCriteria());

                EditText history = (EditText)view.findViewById(R.id.history_size);
                int history_size = parseInt(history.getText().toString());
                AlbumShufflingUtil.getInstance().updateHistorySize(history_size);

                dismiss();
            }
        });

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(AlbumShufflingUtil.CRITERION, adapter.getCriteria());
    }

    private void updateCriterion(ArrayList<AlbumShufflingCriteria> criterion) {
        AlbumShufflingUtil.getInstance().setCriteria(criterion);
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
