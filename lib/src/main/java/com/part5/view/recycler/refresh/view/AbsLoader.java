package com.part5.view.recycler.refresh.view;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * 功能：抽象的加载尾部，可继承并自定义加载尾部
 */
public abstract class AbsLoader extends LinearLayout {
    protected LoaderState loaderState = LoaderState.IDLE;
    private LoaderListener loaderListener;

    public AbsLoader(Context context) {
        super(context);
        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        init();
        setVisibility(GONE);
    }

    public void changeLoaderState(LoaderState state) {
        loaderState = state;
        switch (state) {
            case IDLE:
                setVisibility(GONE);
                break;
            case ERROR:
            case LOADING:
            case NO_MORE:
                setVisibility(VISIBLE);
                break;
        }
    }

    public boolean isLoadingState() {
        return loaderState == LoaderState.LOADING;
    }

    /**
     * 错误重试
     */
    final public void errorRetry() {
        if (loaderListener != null) {
            loaderListener.onErrorRetry();
        }
    }

    public final void setLoaderListener(LoaderListener listener) {
        this.loaderListener = listener;
    }

    public abstract void init();

    /**
     * SRecyclerView的onDetachedFromWindow被调用，可能SRecyclerView所在的界面要被销毁，
     * 如果子类中有动画等未完成，可以重写此方法取消动画等耗时操作，避免造成内存泄露
     */
    public void onRecyclerDetachedFromWindow() {
    }

    public enum LoaderState {
        /**
         * 未加载或加载已完成
         */
        IDLE,
        LOADING,
        NO_MORE,
        ERROR
    }

    public interface LoaderListener {
        void onErrorRetry();
    }
}
