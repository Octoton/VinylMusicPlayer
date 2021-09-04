package com.poupa.vinylmusicplayer.ui.fragments.misc;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.util.DynamicElement.AlbumShufflingUtil;

import static java.lang.Integer.parseInt;


public class TestFragment extends DynamicElementPreferenceFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_test,
                container, false);
        return view;
    }

    public void reset() {

    }

    public void ok() {

    }

}
