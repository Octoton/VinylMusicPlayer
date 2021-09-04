package com.poupa.vinylmusicplayer.ui.fragments.misc;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;


public class DynamicElementBottomSheetDialog extends BottomSheetDialogFragment {
    public static DynamicElementBottomSheetDialog newInstance() { return new DynamicElementBottomSheetDialog(); }

    private DynamicElementPreferenceFragment preferenceFragment;
    private Type searchType;

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

        searchType = Type.toType(PreferenceUtil.getInstance().getDynamicQueueStyle());
        Button button = view.findViewById(R.id.searchType);
        button.setText(getText(Type.getStringRes(searchType)));
        button.setOnClickListener(v -> {
            showMenu(this.getContext(), v);
        });

        preferenceFragment = Type.getFragmentFromValue(searchType);
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

                PreferenceUtil.getInstance().setDynamicQueueStyle(searchType.id);

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

    public static final int STYLE_MENU = 0;
    private void showMenu(Context context, View view) {
        PopupMenu popupMenu = new PopupMenu(context, view);

        popupMenu.getMenu().add(STYLE_MENU, Type.ALBUM.id, Type.ALBUM.id, getText(Type.getStringRes(Type.ALBUM)));
        popupMenu.getMenu().add(STYLE_MENU, Type.SONG.id, Type.SONG.id, getText(Type.getStringRes(Type.SONG)));
        popupMenu.getMenu().add(STYLE_MENU, Type.GENRE.id, Type.GENRE.id, getText(Type.getStringRes(Type.GENRE)));

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Button button = view.findViewById(R.id.searchType);

                Type newSearchType = Type.toType(menuItem.getItemId());
                button.setText(menuItem.getTitle());

                if (searchType != newSearchType) {
                    searchType = newSearchType;
                    updateFragment(Type.getFragmentFromValue(searchType));
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

    private final static Type[] typeValues = Type.values();
    public enum Type {
        ALBUM(0),
        SONG(1),
        GENRE(2);

        /** as the id is saved in shared preference to remember user chose, existing value should not changed or swapped **/
        private final int id;

        Type(int id) { this.id = id; }

        public static int getStringRes (Type e) {
            switch (e) {
                case SONG:
                    return R.string.song;
                case GENRE:
                    return R.string.genre;
                case ALBUM:
                default:
                    return R.string.album;
            }
        }

        public static DynamicElementPreferenceFragment getFragmentFromValue(Type e) {
            switch (e) {
                case SONG:
                    return null;
                case GENRE:
                    return new TestFragment();
                case ALBUM:
                default:
                    return new AlbumShufflingPreferenceFragment();
            }
        }

        public static Type toType (int id) {
            for (Type type : typeValues) {
                if (type.id == id) {
                    return type;
                }
            }
            return ALBUM;
        }
    }
}
