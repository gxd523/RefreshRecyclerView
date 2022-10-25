# RefreshRecyclerView

RecyclerView的下拉刷新、上拉加载封装

* 使用`WrapperAdapter`代理用户设置的`adapter`，`WrapperAdapter`里包含了`Header`、`Footer`及数据`Item`，`WrapperAdapter`
  负责处理`Header`、`Footer`类型的`Holder`，数据`Holder`则由用户设置的`adapter`处理。
* `Header`：包括了`Refresher`、用户可添加`Header`
* `Refresher`：下拉刷新的头部控件，内部包含了头部状态，及动画处理
* `Footer`：包括了`StateView`、用户可添加`Footer`、`Loader`
* `Loader`：加载更多控件
