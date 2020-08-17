package com.part5.view.recycler.refresh.sample.view;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.part5.util.ResUtil;
import com.part5.view.recycler.refresh.sample.R;
import com.part5.view.recycler.refresh.view.AbsRefresher;

public class MyRefresher extends AbsRefresher {
    private TextView refreshText;
    private ClockView clockView;

    public MyRefresher(Context context) {
        super(context);
    }

    @Override
    public void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.refresh_view, this, false);
        clockView = view.findViewById(R.id.v_refresh);
        refreshText = view.findViewById(R.id.tv_refresh);
        addView(view);
    }


    /**
     * 如果需要设置刷新动画时间，可以重写此方法
     */
    @Override
    public int getRefreshDuration() {
        return 300;
    }

    /**
     * 如果需要设置头部的Gravity，可以重写此方法
     *
     * @return HEADER_CENTER，HEADER_BOTTOM
     */
    @Override
    public int getRefreshGravity() {
        return Gravity.CENTER;
    }

    /**
     * 如果需要设置刷新高度，也就是刷新临界值，可以重写此方法
     */
    @Override
    public int getRefreshHeight() {
        return ResUtil.dpToPx(70);
    }

    /**
     * 刷新时下拉的灵敏度，数值越大越不灵敏
     */
    @Override
    public int getRefreshSensitivity() {
        return 1;
    }

    /**
     * SRecyclerView的onDetachedFromWindow被调用，可能SRecyclerView所在的界面要被销毁，
     * 如果子类中有动画等未完成，可以重写此方法取消动画等耗时操作，避免造成内存泄露
     */
    @Override
    public void onRecyclerDetachedFromWindow() {
        if (clockView != null) {
            clockView.resetClock();
        }
    }

    @Override
    public void onRefresherStateChanged(RefreshState state, int height) {
        switch (state) {
            case IDLE:
                refreshText.setText("下拉刷新");
                clockView.stopClockAnim();
                break;
            case REFRESHING:
                refreshText.setText("正在刷新...");
                clockView.startClockAnim();
                break;
            case ALMOST_REFRESH:
                refreshText.setText("下拉刷新");
                clockView.setClockAngle(height);
                break;
            case RELEASE_REFRESH:
                refreshText.setText("释放立即刷新");
                clockView.setClockAngle(height);
                break;
        }
    }


}
