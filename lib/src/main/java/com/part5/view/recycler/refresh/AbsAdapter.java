package com.part5.view.recycler.refresh;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Created by guoxiaodong on 2020/8/8 15:12
 */
public abstract class AbsAdapter<T> extends RecyclerView.Adapter<AbsHolder<T>> {
    private List<T> dataList;

    public AbsAdapter() {
    }

    public AbsAdapter(List<T> dataList) {
        this.dataList = dataList;
    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }

    public T getItem(int position) {
        return dataList == null ? null : dataList.get(position);
    }

    public List<T> getDataList() {
        return dataList;
    }

    public void setDataList(List<T> dataList) {
        this.dataList = dataList;
    }

    @Override
    public void onBindViewHolder(@NonNull AbsHolder<T> holder, int position) {
        holder.onBindViewHolder(position);
    }
}
