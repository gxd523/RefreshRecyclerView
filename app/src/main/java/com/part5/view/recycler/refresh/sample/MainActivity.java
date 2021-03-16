package com.part5.view.recycler.refresh.sample;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.part5.util.ScreenAdapter;
import com.part5.view.recycler.refresh.VerticalRecyclerView;
import com.part5.view.recycler.refresh.sample.util.XFunc1;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity implements MyAdapter.OnSRVAdapterListener, View.OnClickListener {
    private VerticalRecyclerView recyclerView;
    private final List<View> headerList = new ArrayList<>();
    private final List<View> footerList = new ArrayList<>();
    private MyAdapter adapter;
    private boolean refreshError;
    private boolean refreshAnim;
    private boolean refreshEmpty;
    private boolean loadNoMore;
    private boolean loadError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScreenAdapter.setDesignDpWidth(411.43f);
        ScreenAdapter.setCustomDensity(this);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.activity_main_recycler_view);
        findViewById(R.id.test).setOnClickListener(this);

        // 如果设置了加载监听，就是需要刷新加载功能，如果没有设置加载监听，那么就没有下拉与底部加载
        recyclerView.setRefreshLoadingListener(new VerticalRecyclerView.RefreshAndLoadingListener() {
            @Override
            public void refreshing() {
                requestDataAsync(true, list -> {
                    adapter.setDataList(list);
                    adapter.notifyDataSetChanged();
                    recyclerView.refreshComplete();
                });
            }

            @Override
            public void loading() {
                requestDataAsync(false, list -> {
                    if (list == null || list.size() == 0) {
                        recyclerView.loadNoMoreData();
                        return;
                    }
                    List<String> dataList = adapter.getDataList();
                    int size = dataList.size();
                    for (int i = 0; i < list.size(); i++) {
                        list.set(i, String.format("数据  %s", size + i));
                    }
                    dataList.addAll(list);
                    adapter.notifyItemInserted(size);
                    recyclerView.loadComplete();
                });
            }
        });

        // 可以在xml中配置分割线，也可以在代码中设置分割线
        recyclerView.setDivider(0xFFBDBDBD, 4, 20, 20);

        adapter = new MyAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.refreshStart(false);
    }

    private void requestDataAsync(boolean isRefresh, XFunc1<List<String>> xFunc1) {
        Executors.newSingleThreadExecutor().submit(() -> {
            List<String> list = new ArrayList<>();
            try {
                int count = isRefresh ? refreshEmpty ? 0 : 4 : loadNoMore ? 0 : 4;
                for (int i = 0; i < count; i++) {
                    list.add(String.format("数据  %s", i));
                }

                TimeUnit.SECONDS.sleep(1);

                if (isRefresh && refreshError) {
                    throw new Exception();
                } else if (!isRefresh && loadError) {
                    throw new Exception();
                }
            } catch (Exception e) {
                recyclerView.post(() -> {
                    if (isRefresh && refreshError) {
                        adapter.getDataList().clear();
                        recyclerView.refreshError();
                    } else if (!isRefresh && loadError) {
                        recyclerView.loadError();
                    }
                });
                return;
            }
            recyclerView.post(() -> xFunc1.call(list));
        });
    }

    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(getApplication(), "位置：  " + position, Toast.LENGTH_SHORT).show();

        adapter.getDataList().set(position, "这是通知改变的");
        adapter.notifyItemChanged(position);
    }

    @Override
    public void onClick(View v) {
        new ConfigDialog(this, new ConfigDialog.ConfigDialogListener() {
            @Override
            public void addHeader() {
                View header = LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_header, recyclerView, false);
                recyclerView.addHeader(header);
                headerList.add(header);
            }

            @Override
            public void removeHeader() {
                if (headerList.size() == 0) return;
                View header = headerList.get(headerList.size() - 1);
                recyclerView.removeHeader(header);
                headerList.remove(header);
            }

            @Override
            public void addFooter() {
                View footer = LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_footer, recyclerView, false);
                recyclerView.addFooter(footer);
                footerList.add(footer);
            }

            @Override
            public void removeFooter() {
                if (footerList.size() == 0) return;
                View footer = footerList.get(footerList.size() - 1);
                recyclerView.removeFooterA(footer);
                footerList.remove(footer);
            }

            @Override
            public void refresh() {
                refreshError = false;
                refreshEmpty = false;
                refreshAnim = false;
                recyclerView.refreshStart(false);
            }

            @Override
            public void refreshAnim() {
                refreshError = false;
                refreshEmpty = false;
                refreshAnim = true;
                recyclerView.refreshStart(true);
            }

            @Override
            public void refreshEmpty() {
                refreshError = false;
                refreshEmpty = true;
                recyclerView.refreshStart(refreshAnim);
            }

            @Override
            public void refreshError() {
                refreshError = true;
                refreshEmpty = true;
                recyclerView.refreshStart(refreshAnim);
            }

            @Override
            public void refreshNormal() {
                refreshError = false;
                refreshEmpty = false;
                recyclerView.refreshStart(refreshAnim);
            }

            @Override
            public void loadNormal() {
                loadNoMore = false;
                loadError = false;
                recyclerView.loadStart();
            }

            @Override
            public void loadError() {
                loadNoMore = false;
                loadError = true;
                recyclerView.loadStart();
            }

            @Override
            public void loadNoMore() {
                loadNoMore = true;
                loadError = false;
                recyclerView.loadStart();
            }
        }).show();
    }
}
