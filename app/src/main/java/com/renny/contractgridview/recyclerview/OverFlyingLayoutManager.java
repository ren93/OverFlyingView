package com.renny.contractgridview.recyclerview;

import android.graphics.Rect;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
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
    private @FloatRange(from = 0.01, to = 1.0)
    float edgePercent = 0.5f;//触发边缘动画距离百分比

    private @IntRange(from = 1)
    int slowTimes = 5;//到达此距离后放慢倍数

    private int orientation = OrientationHelper.VERTICAL;

    // 用于保存item的位置信息
    private SparseArray<Rect> allItemRects = new SparseArray<>();
    // 用于保存item是否处于可见状态的信息
    private SparseBooleanArray itemStates = new SparseBooleanArray();

    private int totalHeight = 0;
    private int totalWidth = 0;
    private int verticalScrollOffset;
    private int horizontalScrollOffset;
    //头部是否也要层叠，默认需要
    private boolean topOverFlying;
    private viewEdgeListener mViewEdgeListener;

    public OverFlyingLayoutManager() {
        this(OrientationHelper.VERTICAL);
    }

    public OverFlyingLayoutManager(int orientation) {
        this(orientation, true);
    }

    public OverFlyingLayoutManager(int orientation, boolean topOverFlying) {
        this.orientation = orientation;
        this.topOverFlying = topOverFlying;
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
        totalWidth = 0;
        horizontalScrollOffset = 0;
    }

    private void calculateChildrenSite(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (orientation == OrientationHelper.VERTICAL) {
            calculateChildrenSiteVertical(recycler, state);
        } else {
            calculateChildrenSiteHorizontal(recycler, state);
        }

    }

    private void calculateChildrenSiteVertical(RecyclerView.Recycler recycler, RecyclerView.State state) {
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
            totalHeight += height;
            // 保存ItemView的位置信息
            allItemRects.put(i, mTmpRect);
            // 由于之前调用过detachAndScrapAttachedViews(recycler)，所以此时item都是不可见的
            itemStates.put(i, false);
        }
        detachAndScrapAttachedViews(recycler);
        addAndLayoutViewVertical(recycler, state, 0);
    }

    private void calculateChildrenSiteHorizontal(RecyclerView.Recycler recycler, RecyclerView.State state) {
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
            mTmpRect.set(totalWidth, 0, totalWidth + width, height);
            totalWidth += width;
            // 保存ItemView的位置信息
            allItemRects.put(i, mTmpRect);
            // 由于之前调用过detachAndScrapAttachedViews(recycler)，所以此时item都是不可见的
            itemStates.put(i, false);
        }
        detachAndScrapAttachedViews(recycler);
        addAndLayoutViewHorizontal(recycler, state, 0);
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
        int tempDy = dy;
        if (verticalScrollOffset <= totalHeight - getVerticalSpace()) {
            verticalScrollOffset += dy;
            //将竖直方向的偏移量+travel
        }
        if (verticalScrollOffset > totalHeight - getVerticalSpace()) {
            verticalScrollOffset = totalHeight - getVerticalSpace();
            tempDy = 0;
        } else if (verticalScrollOffset < 0) {
            verticalScrollOffset = 0;
            tempDy = 0;
        }
        detachAndScrapAttachedViews(recycler);
        addAndLayoutViewVertical(recycler, state, verticalScrollOffset); //从新布局位置、显示View
        return tempDy;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {

        int tempDx = dx;
        if (horizontalScrollOffset <= totalWidth - getHorizontalSpace()) {
            horizontalScrollOffset += dx;
            //将竖直方向的偏移量+travel
        }
        if (horizontalScrollOffset > totalWidth - getHorizontalSpace()) {
            horizontalScrollOffset = totalWidth - getHorizontalSpace();
            tempDx = 0;
        } else if (horizontalScrollOffset < 0) {
            horizontalScrollOffset = 0;
            tempDx = 0;
        }
        detachAndScrapAttachedViews(recycler);
        addAndLayoutViewHorizontal(recycler, state, horizontalScrollOffset); //从新布局位置、显示View
        return tempDx;

    }

    private void addAndLayoutViewVertical(RecyclerView.Recycler recycler, RecyclerView.State state, int offset) {
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
                if (topOffset <= 0 && topOverFlying) {//到顶了
                    layoutDecoratedWithMargins(view, 0, 0, width, height);
                } else {
                    layoutDecoratedWithMargins(view, 0, topOffset, width, bottomOffset);
                }
            }

            if (i != getItemCount() - 1) {//除最后一个外的底部慢速动画
                if (displayHeight - bottomOffset <= height * edgePercent) {//到达边界后速度放慢到原来5分之一
                    int edgeDist = (int) (displayHeight - height * edgePercent);//边界触发距离
                    int bottom = edgeDist + (bottomOffset - edgeDist) / slowTimes;
                    if (bottom >= displayHeight) {
                        bottom = displayHeight;
                    }
                    layoutDecoratedWithMargins(view, 0, bottom - height, width, bottom);
                }
            }
            if (i != 0 && topOverFlying) {//除第一个外的顶部黏性动画
                if (topOffset <= height * edgePercent) {
                    int edgeDist = (int) (height * edgePercent);
                    int top = edgeDist - (edgeDist - topOffset) / slowTimes;
                    if (top < 0) {
                        top = 0;
                    }
                    layoutDecoratedWithMargins(view, 0, top, width, top + height);
                }
            }
        }

        Log.e(TAG, "itemCount = " + getChildCount());
    }

    private void addAndLayoutViewHorizontal(RecyclerView.Recycler recycler, RecyclerView.State state, int offset) {
        if (getItemCount() <= 0 || state.isPreLayout()) {
            return;
        }
        int displayWidth = getHorizontalSpace();
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

            int rightOffset = mTmpRect.right - offset;
            int leftOffset = mTmpRect.left - offset;
            if (rightOffset > displayWidth) {//到右边
                layoutDecoratedWithMargins(view, displayWidth - width, 0, displayWidth, height);
            } else {
                if (leftOffset <= 0 && topOverFlying) {//到左边了
                    layoutDecoratedWithMargins(view, 0, 0, width, height);
                } else {
                    layoutDecoratedWithMargins(view, leftOffset, 0, rightOffset, height);
                }
            }

            if (i != getItemCount() - 1) {//除最后一个外的右边缘慢速动画
                if (displayWidth - rightOffset <= width * edgePercent) {//到达边界后速度放慢到原来5分之一
                    int edgeDist = (int) (displayWidth - width * edgePercent);//边界触发距离
                    int right = edgeDist + (rightOffset - edgeDist) / slowTimes;
                    if (right >= displayWidth) {
                        right = displayWidth;
                    }
                    layoutDecoratedWithMargins(view, right - width, 0, right, height);
                }
            }
            if (i != 0 && topOverFlying) {//除第一个外的左边缘慢速动画
                if (leftOffset <= width * edgePercent) {
                    int edgeDist = (int) (width * edgePercent);
                    int left = edgeDist - (edgeDist - leftOffset) / slowTimes;
                    left = Math.max(0, left);
                    if (left < 0) {
                        left = 0;
                    }
                    layoutDecoratedWithMargins(view, left, 0, left + width, height);
                }

            }
        }

        Log.d(TAG, "itemCount = " + getChildCount());
    }

    private int getVerticalSpace() {
        // 计算RecyclerView的可用高度，除去上下Padding值
        return getHeight() - getPaddingBottom() - getPaddingTop();
    }


    private int getHorizontalSpace() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }


    public void setEdgePercent(@FloatRange(from = 0.01, to = 1.0) float edgePercent) {
        this.edgePercent = edgePercent;
    }

    public void setSlowTimes(@IntRange(from = 1) int slowTimes) {
        this.slowTimes = slowTimes;
    }

    public interface viewEdgeListener {
        void onTop(View view, float offsetPercent);

        void onBottom(View view, float offsetPercent);

    }
}

