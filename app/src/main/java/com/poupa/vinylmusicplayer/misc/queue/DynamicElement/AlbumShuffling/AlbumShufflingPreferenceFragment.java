package com.poupa.vinylmusicplayer.misc.queue.DynamicElement.AlbumShuffling;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.adapter.DynamicElementAdapter;
import com.poupa.vinylmusicplayer.misc.queue.DynamicElement.DynamicElementPreferenceFragment;

import static java.lang.Integer.parseInt;


public class AlbumShufflingPreferenceFragment extends DynamicElementPreferenceFragment {

    private DynamicElementAdapter adapter;
    private boolean blackListStatus;
    private View view;

    public void reset() {
        updateCriterion(AlbumShufflingUtil.getInstance().getDefaultCriteria());
        adapter.setCriteria(AlbumShufflingUtil.getInstance().getDefaultCriteria());

        EditText history = (EditText)view.findViewById(R.id.history_size);
        AlbumShufflingUtil.getInstance().resetHistorySize();
        history.setText(String.valueOf(AlbumShufflingUtil.getInstance().getHistorySize()));

        blackListStatus = AlbumShufflingUtil.getInstance().getDefaultBlackListUse();
        AlbumShufflingUtil.getInstance().updateBlackListUse(blackListStatus);
        updateBlackListStatus();
    }

    public void ok() {
        updateCriterion(adapter.getCriteria());

        EditText history = (EditText)view.findViewById(R.id.history_size);
        int history_size = parseInt(history.getText().toString());
        AlbumShufflingUtil.getInstance().updateHistorySize(history_size);

        AlbumShufflingUtil.getInstance().updateBlackListUse(blackListStatus);
    }

    private void updateCriterion(ArrayList<AlbumShufflingCriteria> criterion) {
        AlbumShufflingUtil.getInstance().setCriteria(criterion);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(AlbumShufflingUtil.CRITERION, adapter.getCriteria());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_album_shuffling_preference,
                container, false);

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

        CheckBox blackList = (CheckBox)view.findViewById(R.id.use_black_list);
        blackListStatus = AlbumShufflingUtil.getInstance().getBlackListUse();
        updateBlackListStatus();
        blackList.setOnClickListener(v -> {
            blackListStatus = !blackListStatus;
            updateBlackListStatus();
        });

        return view;
    }

    private void updateBlackListStatus() {
        CheckBox blackList = (CheckBox)view.findViewById(R.id.use_black_list);
        blackList.setChecked(blackListStatus);
    }

}
