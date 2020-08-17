package com.part5.view.recycler.refresh.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.part5.view.recycler.refresh.R;

public class DefaultRefresher extends AbsRefresher {
    private View progressBar, iconView;
    private TextView titleTv;
    private ValueAnimator arrowAnim;

    public DefaultRefresher(Context context) {
        super(context);
    }

    @Override
    public void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_refresher, this, true);
        titleTv = findViewById(R.id.layout_refresher_title_tv);
        progressBar = findViewById(R.id.layout_refresher_progress_bar);
        iconView = findViewById(R.id.layout_refresher_icon_view);

        arrowAnim = ValueAnimator.ofInt(0, 180).setDuration(200);
        arrowAnim.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            iconView.setRotation(value);
        });
    }

    @Override
    public void onRefresherStateChanged(RefreshState state, int height) {
        switch (state) {
            case IDLE:
                progressBar.setVisibility(GONE);
                iconView.setVisibility(VISIBLE);
                titleTv.setText(getContext().getString(R.string.srv_refresh_normal));
                if (iconView.getRotation() > 0) {
                    iconView.setRotation(0);
                }
                break;
            case REFRESHING:
                progressBar.setVisibility(VISIBLE);
                arrowAnim.cancel();
                iconView.setVisibility(GONE);
                titleTv.setText(getContext().getString(R.string.srv_refreshing));
                break;
            case ALMOST_REFRESH:
                titleTv.setText(getContext().getString(R.string.srv_refresh_normal));
                if (iconView.getRotation() == 180) {
                    arrowAnim.setIntValues(180, 360);
                    arrowAnim.start();
                }
                break;
            case RELEASE_REFRESH:
                titleTv.setText(getContext().getString(R.string.srv_refresh_prepare));
                if (iconView.getRotation() == 0 || iconView.getRotation() == 360) {
                    arrowAnim.setIntValues(0, 180);
                    arrowAnim.start();
                }
                break;
        }
    }

    @Override
    public int getRefreshSensitivity() {
        return 1;
    }

    @Override
    public void onRecyclerDetachedFromWindow() {
        super.onRecyclerDetachedFromWindow();
        if (arrowAnim != null && arrowAnim.isRunning()) {
            arrowAnim.cancel();
        }
    }
}
