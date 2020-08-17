package com.part5.view.recycler.refresh.sample;

import android.content.Context;

import com.part5.view.recycler.refresh.config.RecyclerModule;
import com.part5.view.recycler.refresh.sample.view.EmptyView;
import com.part5.view.recycler.refresh.sample.view.ErrorView;
import com.part5.view.recycler.refresh.sample.view.MyLoader;
import com.part5.view.recycler.refresh.sample.view.RefreshingView;
import com.part5.view.recycler.refresh.view.AbsLoader;
import com.part5.view.recycler.refresh.view.AbsRefresher;
import com.part5.view.recycler.refresh.view.AbsStateView;
import com.part5.view.recycler.refresh.view.DefaultRefresher;

public class MyRecyclerModule implements RecyclerModule {
    @Override
    public AbsRefresher getRefresher(Context context) {
        return new DefaultRefresher(context);
    }

    @Override
    public AbsLoader getLoader(Context context) {
        return new MyLoader(context);
    }

    @Override
    public AbsStateView getEmptyView(Context context) {
        return new EmptyView(context);
    }

    @Override
    public AbsStateView getErrorView(Context context) {
        return new ErrorView(context);
    }

    @Override
    public AbsStateView getRefreshingView(Context context) {
        return new RefreshingView(context);
    }
}
