package com.poupa.vinylmusicplayer.misc.queue.DynamicElement;

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
import android.widget.FrameLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.helper.MusicPlayerRemote;
import com.poupa.vinylmusicplayer.misc.queue.DynamicElement.AbstractShuffling.AbstractQueueLoader;
import com.poupa.vinylmusicplayer.misc.queue.DynamicElement.AlbumShuffling.AlbumShufflingPreferenceFragment;
import com.poupa.vinylmusicplayer.misc.queue.DynamicElement.AlbumShuffling.AlbumShufflingQueueLoader;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;


public class DynamicElementBottomSheetDialog extends BottomSheetDialogFragment {
    public static DynamicElementBottomSheetDialog newInstance() { return new DynamicElementBottomSheetDialog(); }

    public final static String NEW_QUEUE_SONGS = "newQueueSongs";
    public final static String ALBUM_TYPE = "albumType";
    private ArrayList<Song> songs;
    private boolean isAlbumType;

    private DynamicElementPreferenceFragment preferenceFragment;
    private Type searchType;

    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            songs = bundle.getParcelableArrayList(NEW_QUEUE_SONGS);
            isAlbumType = bundle.getBoolean(ALBUM_TYPE, false);
        } else {
            songs = null;
        }

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

        if (isAlbumType) {
            searchType = Type.ALBUM;
        } else {
            searchType = Type.toType(PreferenceUtil.getInstance().getDynamicQueueStyle());
        }

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

        int accentColor = ThemeStore.accentColor(getContext());

        TextView title = view.findViewById(R.id.setting_title);
        title.setTextColor(accentColor);

        Button reset = view.findViewById(R.id.reset);
        reset.setTextColor(accentColor);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (preferenceFragment != null)
                    preferenceFragment.reset();
            }
        });

        Button cancel = view.findViewById(R.id.cancel);
        cancel.setTextColor(accentColor);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dismiss();
            }
        });

        Button ok = view.findViewById(R.id.ok);
        ok.setTextColor(accentColor);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (preferenceFragment != null)
                    preferenceFragment.ok();

                PreferenceUtil.getInstance().setDynamicQueueStyle(searchType.id);

                if (songs != null && !songs.isEmpty())
                    MusicPlayerRemote.openQueue(songs, 0, true);

                MusicPlayerRemote.setQueueToDynamicQueue(true);

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
        ALBUM(0);

        /** as the id is saved in shared preference to remember user chose, existing value should not changed or swapped **/
        public final int id;

        Type(int id) { this.id = id; }

        public static int getStringRes (Type e) {
            switch (e) {
                case ALBUM:
                default:
                    return R.string.album;
            }
        }

        public static DynamicElementPreferenceFragment getFragmentFromValue(Type e) {
            switch (e) {
                case ALBUM:
                default:
                    return new AlbumShufflingPreferenceFragment();
            }
        }

        public static AbstractQueueLoader getQueueLoader(Type e) {
            switch (e) {
                case ALBUM:
                default:
                    return new AlbumShufflingQueueLoader();
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
