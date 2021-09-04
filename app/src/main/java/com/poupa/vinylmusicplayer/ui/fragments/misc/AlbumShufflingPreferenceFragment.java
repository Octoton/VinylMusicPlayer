package com.poupa.vinylmusicplayer.ui.fragments.misc;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import com.poupa.vinylmusicplayer.R;

public class AlbumShufflingPreferenceFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album_shuffling_preference,
                container, false);
        return view;
    }

}
