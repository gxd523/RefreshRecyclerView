package com.part5.view.recycler.refresh.sample;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.part5.view.recycler.refresh.AbsAdapter;
import com.part5.view.recycler.refresh.AbsHolder;

public class MyAdapter extends AbsAdapter<String> {
    private final OnSRVAdapterListener onSRVAdapterListener;

    public MyAdapter(OnSRVAdapterListener onSRVAdapterListener) {
        super();
        this.onSRVAdapterListener = onSRVAdapterListener;
    }

    @NonNull
    @Override
    public AbsHolder<String> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyHolder(parent.getContext(), this);
    }

    public OnSRVAdapterListener getOnSRVAdapterListener() {
        return onSRVAdapterListener;
    }

    public interface OnSRVAdapterListener {
        void onItemClick(View view, int position);
    }
}
