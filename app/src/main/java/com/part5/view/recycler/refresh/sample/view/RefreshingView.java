package com.part5.view.recycler.refresh.sample.view;

import android.content.Context;
import android.view.LayoutInflater;

import com.part5.view.recycler.refresh.sample.R;
import com.part5.view.recycler.refresh.view.AbsStateView;

import androidx.annotation.NonNull;

public class RefreshingView extends AbsStateView {
    public RefreshingView(@NonNull Context context) {
        super(context);
    }

    @Override
    public void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_refreshing_state, this, true);
    }

}
