package com.renny.contractgridview.recyclerview;

import android.graphics.Rect;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by LuckyCrystal on 2017/6/6.
 */

public class OverFlyingLayoutManager extends RecyclerView.LayoutManager {

    private static final String TAG = "aaaa";

    int orientation = OrientationHelper.VERTICAL;

    // 用于保存item的位置信息
    private SparseArray<Rect> allItemRects = new SparseArray<>();
    // 用于保存item是否处于可见状态的信息
    private SparseBooleanArray itemStates = new SparseBooleanArray();

    private int totalHeight = 0;
    private int verticalScrollOffset;
    private viewEdgeListener mViewEdgeListener;

    public OverFlyingLayoutManager() {
        this(OrientationHelper.VERTICAL);
    }

    public OverFlyingLayoutManager(int orientation) {
        if (orientation != OrientationHelper.VERTICAL && orientation != OrientationHelper.HORIZONTAL) {
            throw new RuntimeException("方向必须是VERTICAL或者HORIZONTAL");
        }
        this.orientation = orientation;
    }

    public void setViewEdgeListener(viewEdgeListener viewEdgeListener) {
        mViewEdgeListener = viewEdgeListener;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        super.onLayoutChildren(recycler, state);
        // 先把所有的View先从RecyclerView中detach掉，然后标记为"Scrap"状态，表示这些View处于可被重用状态(非显示中)。
        // 实际就是把View放到了Recycler中的一个集合中。
        reset();
        detachAndScrapAttachedViews(recycler);
        calculateChildrenSite(recycler, state);
    }

    private void reset() {
        totalHeight = 0;
        verticalScrollOffset = 0;
    }

    private void calculateChildrenSite(RecyclerView.Recycler recycler, RecyclerView.State state) {

        for (int i = 0; i < getItemCount(); i++) {
            View view = recycler.getViewForPosition(i);
            addView(view);
            // 我们自己指定ItemView的尺寸。
            measureChildWithMargins(view, 0, 0);
            calculateItemDecorationsForChild(view, new Rect());
            int width = getDecoratedMeasuredWidth(view);
            int height = getDecoratedMeasuredHeight(view);

            Rect mTmpRect = allItemRects.get(i);
            if (mTmpRect == null) {
                mTmpRect = new Rect();
            }
            mTmpRect.set(0, totalHeight, width, totalHeight + height);
            totalHeight = totalHeight + height;
            // 保存ItemView的位置信息
            allItemRects.put(i, mTmpRect);
            // 由于之前调用过detachAndScrapAttachedViews(recycler)，所以此时item都是不可见的
            itemStates.put(i, false);
        }
        detachAndScrapAttachedViews(recycler);
        addAndLayoutView(recycler, state, 0);

    }

    @Override
    public boolean canScrollHorizontally() {
        // 返回true表示可以横向滑动
        return orientation == OrientationHelper.HORIZONTAL;
    }

    @Override
    public boolean canScrollVertically() {
        // 返回true表示可以纵向滑动
        return orientation == OrientationHelper.VERTICAL;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        //列表向下滚动dy为正，列表向上滚动dy为负，这点与Android坐标系保持一致。
        if (verticalScrollOffset <= totalHeight - getVerticalSpace()) {
            verticalScrollOffset += dy;
            //将竖直方向的偏移量+travel
        }
        if (verticalScrollOffset > totalHeight - getVerticalSpace()) {
            verticalScrollOffset = totalHeight - getVerticalSpace();
        } else if (verticalScrollOffset < 0) {
            verticalScrollOffset = 0;
        }
        detachAndScrapAttachedViews(recycler);
        addAndLayoutView(recycler, state, verticalScrollOffset); //从新布局位置、显示View
        return dy;
    }

    private void addAndLayoutView(RecyclerView.Recycler recycler, RecyclerView.State state, int offset) {
        if (getItemCount() <= 0 || state.isPreLayout()) {
            return;
        }
        int displayHeight = getVerticalSpace();
        for (int i = getItemCount() - 1; i >= 0; i--) {
            // 遍历Recycler中保存的View取出来
            View view = recycler.getViewForPosition(i);
            addView(view); // 因为刚刚进行了detach操作，所以现在可以重新添加
            measureChildWithMargins(view, 0, 0); // 通知测量view的margin值
            int width = getDecoratedMeasuredWidth(view); // 计算view实际大小，包括了ItemDecorator中设置的偏移量。
            int height = getDecoratedMeasuredHeight(view);

            Rect mTmpRect = allItemRects.get(i);
            //调用这个方法能够调整ItemView的大小，以除去ItemDecorator。
            calculateItemDecorationsForChild(view, new Rect());

            int bottomOffset = mTmpRect.bottom - offset;
            int topOffset = mTmpRect.top - offset;
            if (bottomOffset > displayHeight) {//到底了
                layoutDecoratedWithMargins(view, 0, displayHeight - height, width, displayHeight);
            } else {
                if (topOffset <= 0) {//到顶了
                    layoutDecoratedWithMargins(view, 0, 0, width, height);
                } else {
                    layoutDecoratedWithMargins(view, 0, topOffset, width, bottomOffset);
                }
            }
            if (i != getItemCount() - 1) {//除最后一个外的黏性慢速动画
                if ((bottomOffset <= displayHeight && displayHeight - bottomOffset <= height / 2)
                        || (bottomOffset > displayHeight && displayHeight + height / 2 >= bottomOffset)) {
                    int bottom = (height / 2 - (displayHeight - bottomOffset)) / 2 + displayHeight - height / 2;
                    layoutDecoratedWithMargins(view, 0, bottom - height, width, bottom);
                    if (mViewEdgeListener != null) {
                        mViewEdgeListener.onBottom(view, (displayHeight - bottom) / (float) height);
                    }
                }
            }
            if (i != 0) {//除第一个外的顶部黏性动画
                if ((topOffset > 0 && height / 2 >= topOffset) || (topOffset <= 0 && height / 2 >= -topOffset)) {
                    int top = height / 2 - (height / 2 - topOffset) / 2;
                    layoutDecoratedWithMargins(view, 0, top, width, top + height);
                    if (mViewEdgeListener != null) {
                        mViewEdgeListener.onTop(view, top / (float) height);
                    }
                }
            }
        }

        Log.e(TAG, "itemCount = " + getChildCount());
    }


    private int getVerticalSpace() {
        // 计算RecyclerView的可用高度，除去上下Padding值
        return getHeight() - getPaddingBottom() - getPaddingTop();
    }


    public int getHorizontalSpace() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }


    public interface viewEdgeListener {
        void onTop(View view, float offsetPercent);

        void onBottom(View view, float offsetPercent);

    }

}

