package com.part5.view.recycler.refresh.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import com.part5.util.ResUtil;

/**
 * 下拉刷新控件
 */
public abstract class AbsRefresher extends LinearLayout {
    private RefreshState mRefreshState = RefreshState.IDLE;
    private ValueAnimator heightAnim;
    private RefreshListener refreshListener;
    private final Runnable refreshTask = () -> {
        mRefreshState = RefreshState.IDLE;
        startHeightAnim();
    };

    public AbsRefresher(Context context) {
        super(context);
        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
        setGravity(getRefreshGravity());
        init();
    }

    /**
     * 手指拖动时
     */
    public final void onActionMove(float offset) {
        if (mRefreshState == RefreshState.REFRESHING || (isAnimRunning())) return;
        offset = offset / getRefreshSensitivity();
        int height = getHeight();
        height += offset;
        setHeight(height);
        if (getLayoutParams().height == 0) {
            mRefreshState = RefreshState.IDLE;
        } else if (getHeight() < getRefreshHeight()) {
            mRefreshState = RefreshState.ALMOST_REFRESH;
        } else {
            mRefreshState = RefreshState.RELEASE_REFRESH;
        }
        onRefresherStateChanged(mRefreshState, height);
    }

    /**
     * 手指抬起时
     */
    public final void onActionUp() {
        if (mRefreshState == RefreshState.IDLE) return;
        // 处于将要刷新状态时，松开手指即可刷新，同时改变到刷新高度
        if (mRefreshState == RefreshState.RELEASE_REFRESH && refreshListener != null) {
            onRefresherStateChanged(RefreshState.REFRESHING, getRefreshHeight());
            refreshListener.refresh();
        }
        startHeightAnim();
    }

    public final void startRefresh() {
        if (refreshListener == null || mRefreshState == RefreshState.REFRESHING) return;
        int delay = getWidth() == 0 ? 500 : 0;
        removeCallbacks(refreshTask);
        postDelayed(refreshTask, delay);
    }

    /**
     * 请求数据，刷新完成，恢复初始状态
     */
    public final void endRefresh() {
        if (mRefreshState == RefreshState.IDLE) return;
        if (isAnimRunning()) {
            heightAnim.cancel();
        }
        startHeightAnim();
    }

    private void startHeightAnim() {
        if (isAnimRunning()) return;
        int start = getHeight();
        int end;
        switch (mRefreshState) {
            case REFRESHING:// 接下来,刷新结束变为正常
            case ALMOST_REFRESH:// 在低于刷新高度的位置松开拖动，准备回归初始状态
                end = 0;
                break;
            case RELEASE_REFRESH:// 在高于刷新高度的位置松开拖动，准备开始刷新
            case IDLE:// 代码调用开始刷新，准备开始刷新
                end = getRefreshHeight();
                break;
            default:
                return;
        }
        if (heightAnim == null) {
            heightAnim = ValueAnimator.ofInt(start, end);
            int duration = getRefreshDuration();
            heightAnim.setDuration(duration).setInterpolator(new DecelerateInterpolator());
            heightAnim.addUpdateListener(animation -> {
                int height = (int) animation.getAnimatedValue();
                setHeight(height);
                // 高度自动更新的两个状态：刷新结束后的状态，未达到刷新高度而松手的状态
                if (mRefreshState == RefreshState.ALMOST_REFRESH || mRefreshState == RefreshState.IDLE) {
                    onRefresherStateChanged(RefreshState.ALMOST_REFRESH, height);
                }
            });
            heightAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    endHeightAnim();
                }
            });
        } else {
            heightAnim.setIntValues(start, end);
        }
        heightAnim.start();
    }

    private void endHeightAnim() {
        int refreshHeight = getRefreshHeight();
        switch (mRefreshState) {
            case REFRESHING:// 刷新结束
                mRefreshState = RefreshState.IDLE;
                onRefresherStateChanged(RefreshState.IDLE, 0);
                break;
            case ALMOST_REFRESH://在低于刷新高度的位置松开拖动，当前已回归初始状态
                mRefreshState = RefreshState.IDLE;
                break;
            case RELEASE_REFRESH://在高于刷新高度的位置松开拖动，当前已开始刷新
                mRefreshState = RefreshState.REFRESHING;
                break;
            case IDLE:// 代码调用自动刷新，当前已开始刷新
                if (getHeight() < 0) return;
                mRefreshState = RefreshState.REFRESHING;
                onRefresherStateChanged(RefreshState.REFRESHING, refreshHeight);
                refreshListener.refresh();
                break;
        }
    }

    private void setHeight(int height) {
        height = Math.max(height, 0);
        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = height;
        setLayoutParams(params);
    }

    public final boolean isConsumeTouchEvent() {
        return getLayoutParams().height > 0 && mRefreshState != RefreshState.REFRESHING;
    }

    private boolean isAnimRunning() {
        return heightAnim != null && heightAnim.isRunning();
    }

    /**
     * SRecyclerView的onDetachedFromWindow被调用，可能SRecyclerView所在的界面要被销毁，
     * 如果子类中有动画等未完成，可以重写此方法取消动画等耗时操作，避免造成内存泄露
     */
    public void onRecyclerDetachedFromWindow() {
        removeCallbacks(refreshTask);
        if (isAnimRunning()) {
            heightAnim.cancel();
            endHeightAnim();
            setHeight(0);
        }
    }

    public final void setRefreshListener(RefreshListener listener) {
        refreshListener = listener;
    }

    public int getRefreshHeight() {
        return ResUtil.dpToPx(60);
    }

    public int getRefreshGravity() {
        return Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
    }

    public int getRefreshDuration() {
        return 300;
    }

    /**
     * 刷新灵敏度
     */
    public int getRefreshSensitivity() {
        return 3;
    }

    public abstract void init();

    public abstract void onRefresherStateChanged(RefreshState state, int height);

    public enum RefreshState {
        /**
         * Refresher未露出来，初始状态
         */
        IDLE,
        /**
         * 快要刷新，Refresher已露出部分，但还未达到刷新临界值
         */
        ALMOST_REFRESH,
        /**
         * 松开刷新，Refresher已全部露出，达到或超过了刷新临界值，此时松手就会触发刷新
         */
        RELEASE_REFRESH,
        /**
         * 松手后正在刷新的状态
         */
        REFRESHING
    }

    public interface RefreshListener {
        void refresh();
    }
}
