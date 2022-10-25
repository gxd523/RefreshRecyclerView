package com.part5.view.recycler.refresh.sample.view;

import android.content.Context;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;

import com.part5.view.recycler.refresh.sample.R;
import com.part5.view.recycler.refresh.view.AbsStateView;

public class ErrorView extends AbsStateView {
    public ErrorView(@NonNull Context context) {
        super(context);
    }

    @Override
    public void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_error_state, this, true);
        //空布局的点击刷新
        setOnClickListener(v -> retry(false));
    }

}
