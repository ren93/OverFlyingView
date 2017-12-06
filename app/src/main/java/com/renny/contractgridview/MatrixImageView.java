package com.renny.contractgridview;

import android.content.Context;
import android.graphics.Matrix;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

/**
 * Created by test on 2017/12/5.
 */

public class MatrixImageView extends AppCompatImageView {
    private AlignType alignType;

    public MatrixImageView(Context context) {
        super(context);
    }

    public MatrixImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MatrixImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }


    /**
     *
     * @param alignType left, top, right or bottom, default is top
     */
    public void setAlignType(AlignType alignType) {
        this.alignType = alignType;
        invalidate();
    }

    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
        boolean changed = super.setFrame(l, t, r, b);
        if (getScaleType() == ScaleType.MATRIX)
            transformMatrix();
        return changed;
    }

    private void transformMatrix() {
        Matrix matrix = getImageMatrix();
        matrix.reset();
        float h = getHeight();
        float w = getWidth();
        float ch = getDrawable().getIntrinsicHeight();
        float cw = getDrawable().getIntrinsicWidth();
        float widthScaleFactor = w / cw;
        float heightScaleFactor = h / ch;
        if (alignType == AlignType.LEFT) {
            matrix.postScale(heightScaleFactor, heightScaleFactor, 0, 0);
        } else if (alignType == AlignType.RIGHT) {
            matrix.postTranslate(w - cw, 0);
            matrix.postScale(heightScaleFactor, heightScaleFactor, w, 0);
        } else if (alignType == AlignType.BOTTOM) {
            matrix.postTranslate(0, h - ch);
            matrix.postScale(widthScaleFactor, widthScaleFactor, 0, h);
        } else { //default is top
            matrix.postScale(widthScaleFactor, widthScaleFactor, 0, 0);
        }
        setImageMatrix(matrix);
    }

    public enum AlignType {
        LEFT(0),
        TOP(1),
        RIGHT(2),
        BOTTOM(3);

        AlignType(int ni) {
            nativeInt = ni;
        }
        final int nativeInt;
    }

}