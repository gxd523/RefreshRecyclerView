package com.part5.view.recycler.refresh.config;

import android.content.Context;

import com.part5.view.recycler.refresh.view.AbsLoader;
import com.part5.view.recycler.refresh.view.AbsRefresher;
import com.part5.view.recycler.refresh.view.AbsStateView;

public interface RecyclerModule {
    default AbsRefresher getRefresher(Context context) {
        return null;
    }

    default AbsLoader getLoader(Context context) {
        return null;
    }

    default AbsStateView getEmptyView(Context context) {
        return null;
    }

    default AbsStateView getErrorView(Context context) {
        return null;
    }

    default AbsStateView getRefreshingView(Context context) {
        return null;
    }
}
