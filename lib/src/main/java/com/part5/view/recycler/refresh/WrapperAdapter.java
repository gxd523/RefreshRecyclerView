package com.part5.view.recycler.refresh;

import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

/**
 * 核心
 */
public class WrapperAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final AbsAdapter<?> mAdapter;
    private final SparseArray<View> headerList;
    private final SparseArray<View> footerList;
    private RecyclerView.AdapterDataObserver mObserver;

    public WrapperAdapter(AbsAdapter<?> adapter, SparseArray<View> headerList, SparseArray<View> footerList, OnWrapperAdapterListener onWrapperAdapterListener) {
        this.mAdapter = adapter;
        this.headerList = headerList;
        this.footerList = footerList;
        mObserver = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                notifyDataSetChanged();
                onWrapperAdapterListener.onDataChange();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                notifyItemRangeInserted(positionStart + getHeaderCount(), itemCount);
                onWrapperAdapterListener.onDataChange();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                notifyItemRangeRemoved(positionStart + getHeaderCount(), itemCount);
                onWrapperAdapterListener.onDataChange();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                notifyItemRangeChanged(positionStart + getHeaderCount(), itemCount);
                onWrapperAdapterListener.onDataChange();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
                notifyItemRangeChanged(positionStart + getHeaderCount(), itemCount, payload);
                onWrapperAdapterListener.onDataChange();
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                notifyItemMoved(fromPosition + getHeaderCount(), toPosition);
                onWrapperAdapterListener.onDataChange();
            }
        };
        mAdapter.registerAdapterDataObserver(mObserver);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (headerList.get(viewType) != null) {
            return new RecyclerView.ViewHolder(headerList.get(viewType)) {
            };
        } else if (footerList.get(viewType) != null) {
            return new RecyclerView.ViewHolder(footerList.get(viewType)) {
            };
        }
        return mAdapter.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        bindViewHolderWrapper(holder, position, null);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        bindViewHolderWrapper(holder, position, payloads);
    }

    private void bindViewHolderWrapper(RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
        if (isHeader(position) || isFooter(position)) {
            return;
        }
        position -= getHeaderCount();
        if (payloads == null) {
            mAdapter.onBindViewHolder((AbsHolder) holder, position);
        } else {
            mAdapter.onBindViewHolder((AbsHolder) holder, position, payloads);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (isHeader(position)) {
            return headerList.keyAt(position);
        } else if (isFooter(position)) {
            return footerList.keyAt(position - getHeaderCount() - getDataCount());
        }
        return mAdapter.getItemViewType(position - getHeaderCount());
    }

    @Override
    public int getItemCount() {
        return getHeaderCount() + getDataCount() + getFooterCount();
    }

    private boolean isHeader(int position) {
        return position < getHeaderCount();
    }

    private boolean isFooter(int position) {
        return position >= getHeaderCount() + getDataCount();
    }

    public void clearData() {
        List<?> dataList = mAdapter.getDataList();
        if (dataList != null) {
            dataList.clear();
            mAdapter.notifyDataSetChanged();
        }
    }

    public int getDataCount() {
        return mAdapter.getItemCount();
    }

    private int getHeaderCount() {
        return headerList.size();
    }

    private int getFooterCount() {
        return footerList.size();
    }

    public AbsAdapter<?> getAdapter() {
        return mAdapter;
    }

    public void unregisterAdapterDataObserver() {
        mAdapter.unregisterAdapterDataObserver(mObserver);
    }

    /**
     * GridLayout(GridView)的头部特殊处理
     */
    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof GridLayoutManager) {
            final GridLayoutManager gridManager = (GridLayoutManager) manager;
            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    boolean b = isHeader(position) || isFooter(position);
                    return b ? gridManager.getSpanCount() : 1;
                }
            });
        }
    }

    /**
     * StaggeredGridLayout(瀑布流)的头部特殊处理
     */
    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
        if (params instanceof StaggeredGridLayoutManager.LayoutParams) {
            if (holder.getLayoutPosition() < getHeaderCount() || holder.getLayoutPosition() > (getHeaderCount() + getDataCount() - 1)) {
                StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) params;
                p.setFullSpan(true);
            }
        }
    }

    public interface OnWrapperAdapterListener {
        void onDataChange();
    }
}