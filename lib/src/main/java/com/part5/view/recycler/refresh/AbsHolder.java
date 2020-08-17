package com.part5.view.recycler.refresh;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by guoxiaodong on 2020/8/8 15:11
 */
public abstract class AbsHolder<T> extends RecyclerView.ViewHolder {
    protected AbsAdapter<T> adapter;
    /**
     * 注意不要用getAdapterPosition(),没有处理过Refresher和Header
     */
    protected int position;

    public AbsHolder(ViewGroup parent, @LayoutRes int layout, AbsAdapter<T> adapter) {
        this(LayoutInflater.from(parent.getContext()).inflate(layout, parent, false), adapter);
    }

    public AbsHolder(View itemView, AbsAdapter<T> adapter) {
        super(itemView);
        this.adapter = adapter;
    }

    public void onBindViewHolder(int position) {
        this.position = position;
    }
}
