package com.hk.goffer.ui.cview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.hk.goffer.R;


/**
 * Created by hankang on 2015/9/26 19:17.
 */

/*
  问题点： 1。关于footerView的添加时机，需要在adapter设置之前
           2。因为每一次的动作都是依靠一对独立的Y值，因此当加载完成一次之后需要将Y值清零，这样也有利于滑动到底部自动加载
           3. ListView是第二个孩子
           4. onLayout只有在子元素为0时调用一次，其他方法下多次调用,而且只有在onLayout下才能正确得到子view，因此
              为了保证在onLayout中做的初始化只执行一次，必须进行判空
 */
public class RefreshLayout extends SwipeRefreshLayout implements AbsListView.OnScrollListener {

    /**
     * 子元素中的ListView
     */
    private ListView mListView;

    /**
     * 用于显示下拉加载（加载更多）的View，作为ListView的footerView
     */

    /**
     * 记录手指的上滑还是下滑
     */
    private int mStartY;
    private int mEndY;

    /**
     * 是否下拉内容正在加载
     */
    private boolean mIsLoading;

    /**
     * 防抖动的距离
     */
    private int mTouchSlop;

    private OnPullUpListener mPullUpListener;

    private ViewGroup mFooterView;
    private View tvLoadMore;
    private View pbLoading;
    private View tvLoadingTip;

    public RefreshLayout(Context context) {
        this(context, null);
    }

    public RefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mFooterView = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.refresh_footer, null, false);
        tvLoadMore = mFooterView.findViewById(R.id.footer_more);
        pbLoading = mFooterView.findViewById(R.id.footer_bar);
        tvLoadingTip = mFooterView.findViewById(R.id.footer_tip); // 用于设置底部提示文字和进度条的可见性逻辑

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        // 因为onLayout会调用多次，因此为了节省不必要的操作消耗，只有在mListView为null的时候才进行查找
        // 这个问题是由于不了解onLayout的机制和addFooter的覆盖而引起的

        if (mListView == null) { // 这个判断是否非常必要的，详细上面的描述
            initListView();
        }

    }

    /**
     * 设置ListView，设置滑动到底部是自动加载
     */
    private void initListView() {
        if (getChildCount() > 0) {
            View childView = getChildAt(0); // 强制必须是ListView,这里应该谨慎一点判断，因为默认有圆形的加载条，因此ListView是第二个孩子而不是肉眼砍刀的第一个孩子
            if (!(childView instanceof ListView)) {
                throw new ExceptionInInitializerError("The child must be listView");
            }
            mListView = (ListView) childView;

            // ListView本身而言，addHeaderView和addFooter必须在setAdapter之前添加，否则会报错，这两种view不算在adapter内
            setFooterViewVisibility(false);
            mListView.addFooterView(mFooterView);

            //设置自动滑动到底部的时候也进行加载
            mListView.setOnScrollListener(this);
        }
    }

    /**
     * 设置底部的可见性
     *
     * @param visible
     */
    private void setFooterViewVisibility(boolean visible) {
        tvLoadMore.setVisibility(visible?INVISIBLE:VISIBLE);
        pbLoading.setVisibility(visible?VISIBLE:INVISIBLE);
        tvLoadingTip.setVisibility(visible?VISIBLE:INVISIBLE);
    }

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {

        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mStartY = (int) ev.getRawY(); // getRawY是获取屏幕中的坐标,get是Y获取View左上角为原点的坐标
                break;
            case MotionEvent.ACTION_MOVE:
                mEndY = (int) ev.getRawY();
                break;
            case MotionEvent.ACTION_UP:
                if (canLoad()) {
                    loadData();
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private void loadData() {
        //处理动画，调用监听器来处理外部事件
        if (mPullUpListener != null) {
            setPullUpLoading(true); // 显示加载动画
            mPullUpListener.onPullUp();
        }
    }

    /**
     * 设置一下加载的动画,或移除加载动画，可供外部调用停止加载动画
     */
    public void setPullUpLoading(boolean isLoading) {
        mIsLoading = isLoading;
        if (isLoading) {
           setFooterViewVisibility(true); // 结合ListView的Adapter，这里
        } else {
           setFooterViewVisibility(false); // 表示加载完成的时候手指触摸的位置Y一定要归0
            mStartY = 0;
            mEndY = 0;
        }
    }

    /**
     * 判断是否可以加载数据了
     *
     * @return d
     */
    private boolean canLoad() {
        // 底部+ 上滑+ 不在加载状态
        return isPullUp() && isBottom() && !mIsLoading;
    }

    private boolean isBottom() {
        return mListView != null && mListView.getLastVisiblePosition() == mListView.getCount() - 1;
    }

    private boolean isPullUp() {
        return mTouchSlop != 0 && mStartY - mEndY > 2*mTouchSlop; // 1倍距离有点短
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        // 滑动过程中来判断是否可以加载更多:上滑+底部
        if (canLoad()) {
            loadData();
        }
    }

    public void setOnPullUpListener(OnPullUpListener listener) {
        mPullUpListener = listener;
    }

    public interface OnPullUpListener {
        void onPullUp();
    }
}
