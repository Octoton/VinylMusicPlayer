package com.poupa.vinylmusicplayer.ui.fragments.misc;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.poupa.vinylmusicplayer.App;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;
import com.poupa.vinylmusicplayer.util.VinylMusicPlayerColorUtil;


public class DynamicElementBottomSheetDialog extends BottomSheetDialogFragment {
    public static DynamicElementBottomSheetDialog newInstance() { return new DynamicElementBottomSheetDialog(); }

    private DynamicElementPreferenceFragment preferenceFragment;
    private String style;

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

        style = PreferenceUtil.getInstance().getDynamicQueueStyle();
        Button button = view.findViewById(R.id.btnShow);
        button.setText(style);
        button.setOnClickListener(v -> {
            showMenu(this.getContext(), v, R.menu.menu_dynamic_element_type);
        });

        preferenceFragment = getFragmentFromValue(style);
        if (preferenceFragment != null) {
            getChildFragmentManager().beginTransaction()
                    .add(R.id.testFragment, preferenceFragment)
                    .commit();
        }

        view.findViewById(R.id.reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (preferenceFragment != null)
                    preferenceFragment.reset();
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

                if (preferenceFragment != null)
                    preferenceFragment.ok();

                PreferenceUtil.getInstance().setDynamicQueueStyle(style);

                dismiss();
            }
        });

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (preferenceFragment != null)
            preferenceFragment.onSaveInstanceState(outState);
    }

    private void showMenu(Context context, View view, int menuRes) {
        PopupMenu popupMenu = new PopupMenu(context, view);

        popupMenu.inflate(menuRes);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Button button = view.findViewById(R.id.btnShow);

                String title = menuItem.getTitle().toString();
                button.setText(menuItem.getTitle());

                if (!style.equals(title)) {
                    style = title;
                    updateFragment(getFragmentFromValue(style));
                }

                return true;
            }
        });

        popupMenu.show();
    }

    private void updateFragment(DynamicElementPreferenceFragment newChoice) {
        if (newChoice == null) {
            getChildFragmentManager().beginTransaction().remove(preferenceFragment).commit();
            preferenceFragment = null;
        } else {
            preferenceFragment = newChoice;
            getChildFragmentManager().beginTransaction().replace(R.id.testFragment, preferenceFragment).commit();
        }
    }

    public static DynamicElementPreferenceFragment getFragmentFromValue(String style) {
        switch (style) {
            case PreferenceUtil.STYLE_SONG:
                return null;
            case PreferenceUtil.STYLE_GENRE:
                return new TestFragment();
            case PreferenceUtil.STYLE_ALBUM:
            default:
                return new AlbumShufflingPreferenceFragment();
        }
    }
}
