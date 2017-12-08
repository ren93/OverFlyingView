package com.renny.contractgridview;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Renny on 2017/12/7.
 */

public class ContractGridView extends ViewGroup {

    private ViewDragHelper mDragger;
    private View topView, bottomView;
    private openChangeListener mOpenChangeListener;
    private int elevationHeight = 25;//层叠高度
    private int extendHeight = 30;//延伸高度
    private boolean isExpand = true;

    public ContractGridView(@NonNull Context context) {
        this(context, null);
    }

    public ContractGridView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ContractGridView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() != 2) {
            throw new RuntimeException("必须有2个View！");
        }
        if (mDragger == null) {
            topView = getChildAt(0);
            bottomView = getChildAt(1);
            bringChildToFront(topView);
            mDragger = ViewDragHelper.create(this, 1.0f, new ViewDragHelperCallBack());
        }
    }

    //折叠布局
    public void foldLayout() {
        if (topView != null && mDragger != null) {
            isExpand = false;
            mDragger.settleCapturedViewAt(topView.getLeft(), getHeight() - topView.getHeight() - elevationHeight);
        }
    }

    //还原/展开布局
    public void expandLayout() {
        if (topView != null && mDragger != null) {
            mDragger.settleCapturedViewAt(topView.getLeft(), 0);
            isExpand = true;
        }
    }

    //布局是否展开状态
    public boolean isExpand() {
        return isExpand;
    }

    private class ViewDragHelperCallBack extends ViewDragHelper.Callback {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return topView == child;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return 0;
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return 1;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            final int topBound = getPaddingTop();
            final int bottomBound = getHeight() - child.getHeight() - topBound - elevationHeight;
            return Math.min(Math.max(top, topBound), bottomBound);
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            float percent = (float) top / (getHeight() - changedView.getHeight() - elevationHeight);
            Log.d("xxxx", "percent" + percent);
            if (mOpenChangeListener != null) {
                mOpenChangeListener.onScrolling(percent);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                changedView.setElevation(percent * 10);
            }
            bottomView.setScaleX(1 - percent * 0.03f);
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            if (releasedChild == topView) {
                float movePercentage = (float) (releasedChild.getTop()) / (getHeight() - releasedChild.getHeight() - elevationHeight);
                int finalTop = (movePercentage >= .5f) ? getHeight() - releasedChild.getHeight() - elevationHeight : 0;
                mDragger.settleCapturedViewAt(releasedChild.getLeft(), finalTop);
                isExpand = movePercentage < .5f;
                invalidate();
            }
        }
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    public void computeScroll() {
        if (mDragger.continueSettling(true)) {
            invalidate();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return mDragger.shouldInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDragger.processTouchEvent(event);
        return true;
    }

    /**
     * 计算所有ChildView的宽度和高度 然后根据ChildView的计算结果，设置自己的宽和高
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        /**
         * 获得此ViewGroup上级容器为其推荐的宽和高，以及计算模式
         */
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);

        // 计算出所有的childView的宽和高
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        int width = 0;
        int height = 0;

        /**
         * 根据childView计算的出的宽和高，以及设置的margin计算容器的宽和高，主要用于容器是warp_content时
         */
        for (int i = 0; i < getChildCount(); i++) {
            View childView = getChildAt(i);
            MarginLayoutParams cParams = (MarginLayoutParams) childView.getLayoutParams();
            int cWidthWithMargin = childView.getMeasuredWidth() + cParams.leftMargin + cParams.rightMargin;
            int cHeightWithMargin = childView.getMeasuredHeight() + cParams.topMargin + cParams.bottomMargin;

            height = height + cHeightWithMargin;
            width = cWidthWithMargin > width ? cWidthWithMargin : width;
        }
        /**
         * 如果是wrap_content设置为我们计算的值
         * 否则：直接设置为父容器计算的值
         */
        setMeasuredDimension((widthMode == MeasureSpec.EXACTLY) ? sizeWidth
                : width, (heightMode == MeasureSpec.EXACTLY) ? sizeHeight
                : height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        /**
         * 遍历所有childView根据其宽和高，以及margin进行布局
         */
        for (int i = 0; i < getChildCount(); i++) {
            View childView = getChildAt(i);
            int cWidth = childView.getMeasuredWidth();
            int cHeight = childView.getMeasuredHeight();
            MarginLayoutParams cParams = (MarginLayoutParams) childView.getLayoutParams();
            int cl = 0, ct = 0, cr = 0, cb = 0;
            switch (i) {
                case 0:
                    cl = cParams.leftMargin;
                    ct = getHeight() - cHeight - cParams.bottomMargin - extendHeight;
                    cb = cHeight + ct + extendHeight;
                    childView.setPadding(0, extendHeight, 0, 0);
                    cr = cl + cWidth;
                    break;
                case 1:
                    cl = cParams.leftMargin;
                    ct = cParams.topMargin;
                    cb = cHeight + ct;
                    cr = cl + cWidth;
                    break;
            }
            childView.layout(cl, ct, cr, cb);
        }
    }

    public void setExtendHeight(int extendHeight) {
        this.extendHeight = extendHeight;
        requestLayout();
    }

    public void setOpenChangeListener(openChangeListener openChangeListener) {
        mOpenChangeListener = openChangeListener;
    }

    public void setElevationHeight(int elevationHeight) {
        this.elevationHeight = elevationHeight;
        requestLayout();
    }

    public interface openChangeListener {

        void onScrolling(float percent);
    }
}
