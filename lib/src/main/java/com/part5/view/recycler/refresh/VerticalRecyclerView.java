package com.part5.view.recycler.refresh;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.part5.view.recycler.refresh.config.RecyclerConfig;
import com.part5.view.recycler.refresh.config.RecyclerModule;
import com.part5.view.recycler.refresh.view.AbsLoader;
import com.part5.view.recycler.refresh.view.AbsRefresher;
import com.part5.view.recycler.refresh.view.AbsStateView;
import com.part5.view.recycler.refresh.view.DefaultLoader;
import com.part5.view.recycler.refresh.view.DefaultRefresher;

public class VerticalRecyclerView extends RecyclerView implements AppBarLayout.OnOffsetChangedListener {
    /**
     * Header包括refresher、header
     */
    private final SparseArray<View> headerList = new SparseArray<>();
    /**
     * Footer包括loader、footer及stateView(loadingView、emptyView、errorView)
     */
    private final SparseArray<View> footerList = new SparseArray<>();
    /**
     * 假设数据不超过10万条
     */
    private final int REFRESHER_HEADER = 100000;
    private final int LOADER_FOOTER = 500000;
    private final int currentScrollMode;
    private final int dividerColor;
    private final float dividerHeight;
    private final float dividerRightMargin;
    private final float dividerLeftMargin;
    private int STATE_FOOTER = 300000;
    private int HEADER_TYPE = 200000;
    private int FOOTER_TYPE = 400000;
    private AbsRefresher refresher;
    private AbsLoader loader;
    /**
     * 无数据时的stateView
     */
    private View emptyView;
    /**
     * 错误时的stateView
     */
    private View errorView;
    /**
     * 数据加载时的stateView
     */
    private View refreshingView;
    private RecyclerState mRecyclerState = RecyclerState.IDLE;
    private RefreshAndLoadingListener refreshLoadingListener;
    private WrapperAdapter wrapperAdapter;
    private RecyclerDivider mDivider;
    private AppBarLayout appBarLayout;
    private boolean isAppBarExpand = true;
    /**
     * 往上滑，加载更多
     */
    private boolean isSlideUp;
    private boolean isFirstMove = true;
    private float downY;
    private float moveY;

    public VerticalRecyclerView(Context context) {
        this(context, null);
    }

    public VerticalRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerticalRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.VerticalRecyclerView, defStyle, 0);
        dividerHeight = typedArray.getDimension(R.styleable.VerticalRecyclerView_dividerHeight, 0);
        dividerColor = typedArray.getColor(R.styleable.VerticalRecyclerView_dividerColor, Color.TRANSPARENT);
        dividerLeftMargin = typedArray.getDimension(R.styleable.VerticalRecyclerView_dividerLeftMargin, 0);
        dividerRightMargin = typedArray.getDimension(R.styleable.VerticalRecyclerView_dividerRightMargin, 0);
        typedArray.recycle();
        currentScrollMode = getOverScrollMode();
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        setLayoutManager(manager);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (refresher == null) {
            return super.onTouchEvent(e);
        }
        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (isFirstMove) {
                    isFirstMove = false;
                    downY = e.getRawY();
                    moveY = downY;
                }
                float y = e.getRawY();
                float offset = y - moveY;
                moveY = y;
                if (refresher.getParent() != null && isAppBarExpand) {
                    refresher.onActionMove(offset);
                    setOverScrollMode(View.OVER_SCROLL_NEVER);
                    if (refresher.isConsumeTouchEvent()) {
                        return true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                isFirstMove = true;
                isSlideUp = e.getRawY() - downY < 0;
                setOverScrollMode(currentScrollMode);
                if (refresher.getParent() != null && isAppBarExpand) {
                    refresher.onActionUp();
                }
                break;
        }
        return super.onTouchEvent(e);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ViewParent parent = getParent();
        while (parent != null) {
            if (parent instanceof CoordinatorLayout) {
                break;
            }
            parent = parent.getParent();
        }
        if (parent == null) {
            return;
        }
        CoordinatorLayout layout = (CoordinatorLayout) parent;
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            if (child instanceof AppBarLayout) {
                appBarLayout = (AppBarLayout) child;
                break;
            }
        }
        if (appBarLayout != null) {
            appBarLayout.addOnOffsetChangedListener(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (wrapperAdapter != null) {
            wrapperAdapter.unregisterAdapterDataObserver();
        }
        if (appBarLayout != null) {
            appBarLayout.removeOnOffsetChangedListener(this);
        }
        if (refresher != null) {
            refresher.onRecyclerDetachedFromWindow();
        }
        if (loader != null) {
            loader.onRecyclerDetachedFromWindow();
        }
        headerList.clear();
        footerList.clear();
        clearOnScrollListeners();
        refreshLoadingListener = null;
        wrapperAdapter = null;
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        isAppBarExpand = verticalOffset == 0;
    }

    @Nullable
    @Override
    public AbsAdapter<?> getAdapter() {
        return wrapperAdapter == null ? null : wrapperAdapter.getAdapter();
    }

    @Override
    public void setAdapter(Adapter adapter) {
        if (wrapperAdapter != null) {
            wrapperAdapter.unregisterAdapterDataObserver();
        }
        if (adapter instanceof AbsAdapter<?>) {
            wrapperAdapter = new WrapperAdapter((AbsAdapter<?>) adapter, headerList, footerList, this::updateRecyclerState);
        }
        super.setAdapter(wrapperAdapter);
        if (mDivider == null) {
            setDivider(dividerColor, dividerHeight, dividerLeftMargin, dividerRightMargin);
        }
        initRefresher();
        initLoader();
    }

    private void changeRecyclerState(RecyclerState recyclerState) {
        // 非IDLE状态下需要去除LOADER,也就是StateView和Loader不会同时存在
        boolean hasLoader = footerList.get(LOADER_FOOTER) != null;
        if (recyclerState == RecyclerState.IDLE && !hasLoader) {
            footerList.put(LOADER_FOOTER, loader);
            int insertPosition = wrapperAdapter.getItemCount();
            wrapperAdapter.notifyItemInserted(insertPosition);
        } else if (recyclerState != RecyclerState.IDLE && hasLoader) {
            removeFooter(loader);
        }

        if (mRecyclerState != recyclerState) {
            if (mRecyclerState != RecyclerState.IDLE) {// 状态发生变化时,需要移除之前状态的StateView
                View stateView = getStateView(mRecyclerState);
                removeFooter(stateView);
            }

            View stateView = getStateView(recyclerState);
            if (stateView != null) {
                footerList.put(STATE_FOOTER++, stateView);
            }
            mRecyclerState = recyclerState;

            if (wrapperAdapter.getDataCount() == 0) {// 移除除Refresher、Loader、StateView之外的Header、Footer
                for (int i = 0; i < headerList.size(); i++) {
                    View view = headerList.valueAt(i);
                    if (!(view instanceof AbsRefresher)) {
                        headerList.removeAt(i);
                        wrapperAdapter.notifyItemRemoved(i);
                    }
                }
                for (int i = 0; i < footerList.size(); i++) {
                    View view = footerList.valueAt(i);
                    if (!(view instanceof AbsLoader) && !(view instanceof AbsStateView)) {
                        footerList.removeAt(i);
                        wrapperAdapter.notifyItemRemoved(i);
                    }
                }
            }
        }
    }

    public View getStateView(RecyclerState recyclerState) {
        View stateView = null;
        switch (recyclerState) {
            case ERROR:
                stateView = errorView;
                break;
            case EMPTY:
                stateView = emptyView;
                break;
            case REFRESHING:
                stateView = refreshingView;
                break;
        }
        return stateView;
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {
        super.setLayoutManager(layout);
        if (!(layout instanceof LinearLayoutManager) || (layout instanceof GridLayoutManager)) {
            if (mDivider != null) removeItemDecoration(mDivider);
        }
    }

    /**
     * 获取用户配置的刷新头和加载尾
     * 配置的优先级为：代码设置 > SRVConfig配置
     */
    private void applyRecyclerModule() {
        RecyclerModule recyclerModule = RecyclerConfig.getInstance(getContext()).getConfig();
        if (recyclerModule == null) {
            return;
        }
        if (refresher == null) {
            refresher = recyclerModule.getRefresher(getContext());
        }
        if (loader == null) {
            loader = recyclerModule.getLoader(getContext());
        }
        if (refreshingView == null) {
            refreshingView = recyclerModule.getRefreshingView(getContext());
        }
        if (errorView == null) {
            errorView = recyclerModule.getErrorView(getContext());
        }
        if (emptyView == null) {
            emptyView = recyclerModule.getEmptyView(getContext());
        }
        ((AbsStateView) emptyView).setRetryListener(this::refreshStart);
        ((AbsStateView) errorView).setRetryListener(this::refreshStart);
    }

    private void updateRecyclerState() {// TODO: 2020/8/17 gxd
        if (wrapperAdapter.getDataCount() == 0) {
            changeRecyclerState(RecyclerState.EMPTY);
        } else {
            changeRecyclerState(RecyclerState.IDLE);
        }
    }

    public void setDivider(int color, float height, float dividerLeft, float dividerRight) {
        LayoutManager layout = getLayoutManager();
        if (height != 0 && layout instanceof LinearLayoutManager) {// 只对LinearLayoutManager设置分割线
            if (mDivider != null) removeItemDecoration(mDivider);
            LinearLayoutManager manager = (LinearLayoutManager) layout;
            if (manager.getOrientation() == LinearLayoutManager.VERTICAL) {
                mDivider = new RecyclerDivider(LinearLayoutManager.VERTICAL);
                mDivider.initVerticalDivider(height, color, dividerLeft, dividerRight);
                addItemDecoration(mDivider);
            } else if (manager.getOrientation() == LinearLayoutManager.HORIZONTAL) {
                mDivider = new RecyclerDivider(LinearLayoutManager.HORIZONTAL);
                mDivider.initHorizontalDivider(height, color);
                addItemDecoration(mDivider);
            }
        }
    }

    /**
     * Header(Refresher、Header)、Footer(Loader、Footer、StateView)都是SpecialItem
     */
    public boolean isSpecialItemType(View view) {
        return headerList.indexOfValue(view) != -1 || footerList.indexOfValue(view) != -1;
    }

    public void setRefreshLoadingListener(RefreshAndLoadingListener listener) {
        refreshLoadingListener = listener;
    }

    /**********************************************************Header开始**********************************************************/
    public void addHeader(View view) {
        if (view == null) {
            return;
        }
        checkAddView(view);
        headerList.put(HEADER_TYPE++, view);
        if (wrapperAdapter != null) {
            wrapperAdapter.notifyItemInserted(headerList.size() - 1);
            updateRecyclerState();
        }
    }

    public void removeHeader(View view) {
        if (view == null || wrapperAdapter == null) {
            return;
        }
        for (int i = 0; i < headerList.size(); i++) {
            if (view == headerList.valueAt(i)) {
                headerList.removeAt(i);
                wrapperAdapter.notifyItemRemoved(i);
                break;
            }
        }
        updateRecyclerState();
    }

    private int getHeaderCount() {
        return headerList.size();
    }

    /********************************************************Refresher开始********************************************************/

    private void initRefresher() {
        // 只在此方法中仅仅获取一次用户全局配置
        applyRecyclerModule();
        //没有设置刷新头部时，设置默认的刷新头部，否则使用设置的刷新头
        if (refresher == null) {
            refresher = new DefaultRefresher(getContext());
        }
        headerList.put(REFRESHER_HEADER, refresher);
        wrapperAdapter.notifyItemInserted(0);
        refresher.setRefreshListener(() -> refreshLoadingListener.refreshing());
    }

    /**
     * 设置单独的刷新头部
     * 应在setAdapter之前调用才有效
     */
    public void setRefresher(AbsRefresher view) {
        if (view == null || getAdapter() != null) return;
        refresher = view;
    }

    public void refreshStart(boolean isAnim) {
        if (refresher == null || getAdapter() == null) {
            return;
        }
        if (isAnim) {
            scrollToPosition(0);
            refresher.startRefresh();
        } else {
            if (wrapperAdapter != null && refreshingView != null) {
                wrapperAdapter.clearData();
                changeRecyclerState(RecyclerState.REFRESHING);
                refreshLoadingListener.refreshing();
            }
        }
    }

    public void refreshComplete() {
        if (refresher == null) {
            return;
        }
        refresher.endRefresh();
        if (loader == null) {
            return;
        }
        loadComplete();
    }

    public void refreshError() {
        refreshComplete();
        if (wrapperAdapter == null) {
            return;
        }
        wrapperAdapter.clearData();
        changeRecyclerState(RecyclerState.ERROR);
    }

    /*********************************************************Footer开始*********************************************************/

    public void addFooter(View view) {
        if (view == null || wrapperAdapter == null) {
            return;
        }
        checkAddView(view);
        footerList.put(FOOTER_TYPE++, view);
        int insertPosition = wrapperAdapter.getItemCount() - 1;
        if (footerList.get(LOADER_FOOTER) != null) {// 如果有加载尾部，则在尾部之前插入Item，保证加载尾部是最后一个Item
            insertPosition -= 1;
        }
        wrapperAdapter.notifyItemInserted(insertPosition);
        updateRecyclerState();
    }

    public void removeFooterA(View view) {
        if (wrapperAdapter == null) {
            return;
        }
        removeFooter(view);
        updateRecyclerState();
    }

    /**
     * @param view 包括footer、loader、emptyView、loadingView、errorView
     */
    private void removeFooter(View view) {
        if (view == null) return;
        for (int i = 0; i < getFooterCount(); i++) {
            if (view == footerList.valueAt(i)) {
                footerList.removeAt(i);
                removeView(view);
                wrapperAdapter.notifyItemRemoved(getHeaderCount() + wrapperAdapter.getDataCount() + i);
                break;
            }
        }
    }

    private int getFooterCount() {
        return footerList.size();
    }

    /*********************************************************Loader开始*********************************************************/

    private void initLoader() {
        if (loader == null) {
            loader = new DefaultLoader(getContext());
        }
        footerList.put(LOADER_FOOTER, loader);
        int insertPosition = wrapperAdapter.getItemCount();
        wrapperAdapter.notifyItemInserted(insertPosition);
        // 刷新和加载只支持垂直方向的LinearLayoutManager和GridLayoutManager布局
        loader.setTag(false);
        loader.setLoaderListener(this::loadStart);
        addOnScrollListener(new OnScrollListener() {
            /**
             * 数据未超过一页时,滑动结束时触发这里
             * @param newState SCROLL_STATE_IDLE: 滚动结束, SCROLL_STATE_DRAGGING: 开始滚动
             */
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == SCROLL_STATE_IDLE && isSlideUp) {
                    loadStart();
                }
            }

            /**
             * 正在滚动中,数据超过一页时,滑动过程多次触发这里
             * @param dy 上划为正
             */
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    loader.setTag(true);
                    loadStart();
                }
            }
        });
    }

    /**
     * 判断是否开始加载更多，只有滑动到最后一个Item，并且当前有数据时，才会加载更多
     */
    public void loadStart() {
        if (mRecyclerState != RecyclerState.IDLE) {
            return;
        }
        LinearLayoutManager layoutManager = (LinearLayoutManager) getLayoutManager();
        if (layoutManager == null) {
            return;
        }
        int lastPosition = layoutManager.findLastVisibleItemPosition();
        if (!loader.isLoadingState() && lastPosition > 1 && lastPosition == (wrapperAdapter.getItemCount() - 1)) {
            loader.changeLoaderState(AbsLoader.LoaderState.LOADING);
            refreshLoadingListener.loading();
        }
    }

    public void loadComplete() {
        if (loader != null) {
            loader.changeLoaderState(AbsLoader.LoaderState.IDLE);
        }
    }

    public void loadNoMoreData() {
        if (loader != null) {
            loader.changeLoaderState(AbsLoader.LoaderState.NO_MORE);
        }
    }

    public void loadError() {
        if (loader != null) {
            loader.changeLoaderState(AbsLoader.LoaderState.ERROR);
        }
    }

    /**
     * 设置自己的加载尾部
     */
    public void setLoader(AbsLoader view) {
        if (view == null || getAdapter() != null) return;
        loader = view;
    }

    /********************************************************StateView开始********************************************************/

    public void setEmptyView(@LayoutRes int layoutId) {
        setEmptyView(LayoutInflater.from(getContext()).inflate(layoutId, this, false));
    }

    public void setEmptyView(@NonNull View view) {
        emptyView = view;
        updateRecyclerState();
    }

    public void setErrorView(@LayoutRes int layoutId) {
        setErrorView(LayoutInflater.from(getContext()).inflate(layoutId, this, false));
    }

    public void setErrorView(@NonNull View view) {
        errorView = view;
        if (wrapperAdapter == null || wrapperAdapter.getDataCount() != 0) {
            return;
        }
        changeRecyclerState(RecyclerState.ERROR);
    }

    public void setRefreshingView(@LayoutRes int layoutId) {
        setRefreshingView(LayoutInflater.from(getContext()).inflate(layoutId, this, false));
    }

    public void setRefreshingView(View view) {
        refreshingView = view;
    }

    private void checkAddView(View view) {
        if (view.getParent() != null) {
            String msg = "The specified child already has a parent. You must call removeView() on the child's parent first.";
            throw new IllegalStateException(msg);
        }
    }

    enum RecyclerState {
        IDLE,
        REFRESHING,
        EMPTY,
        ERROR
    }

    public interface RefreshAndLoadingListener {
        void refreshing();

        void loading();
    }
}
