package com.renny.contractgridview.recyclerview;

import android.graphics.Rect;
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
    /**
     * 用于保存item的位置信息
     */
    private SparseArray<Rect> allItemRects = new SparseArray<>();
    /**
     * 用于保存item是否处于可见状态的信息
     */
    private SparseBooleanArray itemStates = new SparseBooleanArray();

    private int totalHeight = 0;
    private int verticalScrollOffset;

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
        detachAndScrapAttachedViews(recycler);
        calculateChildrenSite(recycler, state);
    }

    private void calculateChildrenSite(RecyclerView.Recycler recycler, RecyclerView.State state) {
        totalHeight = 0;

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
        recycleAndFillView(recycler, state, 0);

    }


    @Override
    public boolean canScrollVertically() {
        // 返回true表示可以纵向滑动
        return true;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        //列表向下滚动dy为正，列表向上滚动dy为负，这点与Android坐标系保持一致。
        //实际要滑动的距离
        //每次滑动时先释放掉所有的View，因为后面调用recycleAndFillView()时会重新addView()。
        Log.d(TAG, "dy = " + dy + "  " + verticalScrollOffset + "   " + (totalHeight - getVerticalSpace()));
        detachAndScrapAttachedViews(recycler);
        if (verticalScrollOffset <= totalHeight - getVerticalSpace()) {
            verticalScrollOffset += dy;
            //将竖直方向的偏移量+travel
        }
        if (verticalScrollOffset > totalHeight - getVerticalSpace()) {
            verticalScrollOffset = totalHeight - getVerticalSpace();
        } else if (verticalScrollOffset < 0) {
            verticalScrollOffset = 0;
        }
        recycleAndFillView(recycler, state, verticalScrollOffset);
        // 调用该方法通知view在y方向上移动指定距离
        //  offsetChildrenVertical(-travel);
        // recycleAndFillView(recycler, state); //回收并显示View
        return dy;
    }

    private void recycleAndFillView(RecyclerView.Recycler recycler, RecyclerView.State state, int offset) {
        if (getItemCount() <= 0 || state.isPreLayout()) {
            return;
        }
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

            if (mTmpRect.bottom - offset > getVerticalSpace()) {
                layoutDecorated(view, 0, getVerticalSpace() - height, width, getVerticalSpace());
            } else {
                if (mTmpRect.top - offset <= 0) {
                    layoutDecorated(view, 0, 0, width, height);
                } else {
                    layoutDecorated(view, 0, mTmpRect.top - offset, width, mTmpRect.bottom - offset);
                }
            }
        }

        Log.e(TAG, "itemCount = " + getChildCount());
    }


    private int getVerticalSpace() {
        // 计算RecyclerView的可用高度，除去上下Padding值
        return getHeight() - getPaddingBottom() - getPaddingTop();
    }

    @Override
    public boolean canScrollHorizontally() {
        // 返回true表示可以横向滑动
        return super.canScrollHorizontally();
    }


    public int getHorizontalSpace() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }
}

