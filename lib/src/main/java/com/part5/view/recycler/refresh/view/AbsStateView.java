package com.part5.view.recycler.refresh.view;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

public abstract class AbsStateView extends FrameLayout {
    private RetryListener listener;

    public AbsStateView(@NonNull Context context) {
        super(context);
        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        init();
    }

    /**
     * 状态View的异常时的刷新重新
     *
     * @param isAnim 是否有刷新动画
     */
    final public void retry(boolean isAnim) {
        if (listener != null) {
            listener.retry(isAnim);
        }
    }

    public final void setRetryListener(RetryListener listener) {
        this.listener = listener;
    }

    public abstract void init();

    public interface RetryListener {
        void retry(boolean isAnim);
    }

}
