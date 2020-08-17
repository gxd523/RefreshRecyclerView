package com.part5.view.recycler.refresh.sample.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.part5.view.recycler.refresh.sample.R;
import com.part5.view.recycler.refresh.view.AbsLoader;

public class MyLoader extends AbsLoader {
    private View progressBar;
    private TextView titleTv;

    public MyLoader(Context context) {
        super(context);
    }

    @Override
    public void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_loader, this, true);
        progressBar = findViewById(R.id.layout_loader_progress_bar);
        titleTv = findViewById(R.id.layout_loader_title_tv);
        titleTv.setOnClickListener(v -> {
            if (loaderState == LoaderState.ERROR) {
                errorRetry();
            }
        });
    }

    @Override
    public void changeLoaderState(LoaderState state) {
        super.changeLoaderState(state);
        switch (state) {
            case IDLE:
                progressBar.setVisibility(GONE);
                titleTv.setVisibility(GONE);
                break;
            case ERROR:
                progressBar.setVisibility(GONE);
                titleTv.setVisibility(VISIBLE);
                titleTv.setText(getResources().getText(R.string.srv_load_error));
                break;
            case NO_MORE:
                progressBar.setVisibility(GONE);
                titleTv.setVisibility(VISIBLE);
                titleTv.setText(getResources().getText(R.string.srv_load_no_more));
                break;
            case LOADING:
                progressBar.setVisibility(VISIBLE);
                titleTv.setVisibility(VISIBLE);
                titleTv.setText(getResources().getText(R.string.srv_loading));
                break;
        }
    }
}
