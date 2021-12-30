package com.poupa.vinylmusicplayer.misc.queue.DynamicElement.TestShuffling;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.misc.queue.DynamicElement.DynamicElementPreferenceFragment;


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
