package com.part5.view.recycler.refresh.sample;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;

public class ConfigDialog extends Dialog {
    private ConfigDialogListener listener;

    public ConfigDialog(@NonNull Context context, @NonNull ConfigDialogListener listener) {
        super(context, R.style.dialogStyle);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        if (window == null) return;
        window.setContentView(R.layout.dialog_config);
        window.setGravity(Gravity.BOTTOM);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        setClickListener(R.id.dialog_config_add_header, v -> {
            listener.addHeader();
            dismiss();
        });
        setClickListener(R.id.dialog_config_remove_header, v -> {
            listener.removeHeader();
            dismiss();
        });
        setClickListener(R.id.dialog_config_normal_refresh, v -> {
            listener.refresh();
            dismiss();
        });
        setClickListener(R.id.dialog_config_add_footer, v -> {
            listener.addFooter();
            dismiss();
        });
        setClickListener(R.id.dialog_config_remove_footer, v -> {
            listener.removeFooter();
            dismiss();
        });
        setClickListener(R.id.dialog_config_refresher_refresh, v -> {
            listener.refreshAnim();
            dismiss();
        });
        setClickListener(R.id.dialog_config_empty, v -> {
            listener.refreshEmpty();
            dismiss();
        });
        setClickListener(R.id.dialog_config_error, v -> {
            listener.refreshError();
            dismiss();
        });
        setClickListener(R.id.dialog_config_refresh_normal, v -> {
            listener.refreshNormal();
            dismiss();
        });
        setClickListener(R.id.dialog_config_loading_normal, v -> {
            listener.loadNormal();
            dismiss();
        });
        setClickListener(R.id.dialog_config_loading_error, v -> {
            listener.loadError();
            dismiss();
        });
        setClickListener(R.id.dialog_config_loading_no_data, v -> {
            listener.loadNoMore();
            dismiss();
        });
    }

    private void setClickListener(@IdRes int viewId, View.OnClickListener onClickListener) {
        findViewById(viewId).setOnClickListener(onClickListener);
    }

    public interface ConfigDialogListener {
        void addHeader();

        void removeHeader();

        void addFooter();

        void removeFooter();

        void refresh();

        void refreshAnim();

        void refreshEmpty();

        void refreshError();

        void refreshNormal();

        void loadNormal();

        void loadError();

        void loadNoMore();
    }
}
