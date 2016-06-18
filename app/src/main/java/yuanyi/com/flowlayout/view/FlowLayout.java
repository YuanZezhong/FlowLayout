package yuanyi.com.flowlayout.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import yuanyi.com.flowlayout.R;
import yuanyi.com.flowlayout.util.DensityUtils;
import yuanyi.com.flowlayout.util.LogUtil;

/**
 * 流式布局
 */
public class FlowLayout extends ViewGroup {
    /* 默认水平间距, 单位dp */
    private static final int DEFAULT_HORIZONTAL_SPACE = 5;
    /* 默认垂直间距, 单位dp */
    private static final int DEFAULT_VERTICAL_SPACE = 5;

    /* FlowGravity 属性值，居中、左对齐、右对齐 */
    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    public static final int CENTER_HORIZONTAL = 2;
    public static final int CENTER_VERTICAL = 4;
    public static final int CENTER = 8;

    private List<Line> mLines;          // 记录所有行
    private int mHorizontalSpace;       // 水平方向上的间距
    private int mVerticalSpace;         // 垂直方向上的间距
    private int mFlowGravity;           // 排列方式
    private int dividerColor;           // 分割线颜色
    private int dividerHeight;          // 分隔线高度

    public FlowLayout(Context context) {
        this(context, null);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mLines = new ArrayList<>();
        TypedArray typedArray =
                context.getTheme()
                        .obtainStyledAttributes(attrs, R.styleable.FlowLayout, defStyleAttr, 0);
        mHorizontalSpace = (int) typedArray.getDimension(R.styleable.FlowLayout_horizontalSpace,
                DensityUtils.dp2px(context, DEFAULT_HORIZONTAL_SPACE));
        mVerticalSpace = (int) typedArray.getDimension(R.styleable.FlowLayout_verticalSpace,
                DensityUtils.dp2px(context, DEFAULT_VERTICAL_SPACE));
        mFlowGravity = typedArray.getInt(R.styleable.FlowLayout_flowGravity, LEFT);
        typedArray.recycle();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int x;
        int y = getPaddingTop();
        /* 判断是否需要垂直居中 */
        if (mFlowGravity == CENTER_VERTICAL || mFlowGravity == CENTER) {
            int contentHeight = getContentHeight();
            /* 判断是否满足可以垂直居中的条件 */
            if ((getVerticalPadding() + contentHeight) < getMeasuredHeight()) {
                y += (getMeasuredHeight() - contentHeight - getVerticalPadding()) / 2;
            }
        }

        for (Line line : mLines) {
            int horizontalSpace = line.getHorizontalSpace();
            switch (mFlowGravity) {
                case RIGHT:
                    x = getPaddingLeft() + (line.getMaxWidth() - line.getLineWidth());
                    break;
                case CENTER_HORIZONTAL:
                case CENTER:
                    x = getPaddingLeft() + (line.getMaxWidth() - line.getLineWidth()) / 2;
                    break;
                case LEFT:
                default:
                    x = getPaddingLeft();
                    break;
            }
            for (int i = 0; i < line.getChildCount(); ++i) {
                View child = line.getChildAt(i);
                MarginLayoutParams params = (MarginLayoutParams) child.getLayoutParams();
                int lMargin = params.leftMargin;
                int rMargin = params.rightMargin;
                int tMargin = params.topMargin;
                child.layout(x += lMargin, y + tMargin, x += child.getMeasuredWidth(), y + tMargin + child.getMeasuredHeight());
                x += rMargin;
                x += horizontalSpace;
            }
            y += line.getLineHeight();
            y += mVerticalSpace;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int wMode = MeasureSpec.getMode(widthMeasureSpec);
        int wSize = MeasureSpec.getSize(widthMeasureSpec);
        int hSize = MeasureSpec.getSize(heightMeasureSpec);
        int hMode = MeasureSpec.getMode(heightMeasureSpec);
        /* 获取内边距(水平方向和垂直方向) */
        int paddingHorizontal = getHorizontalPadding();
        int paddingVertical = getVerticalPadding();

        mLines.clear();
        Line line = new Line(wSize - paddingHorizontal, mHorizontalSpace);
        for (int i = 0; i < getChildCount(); ++i) {
            View child = getChildAt(i);
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
            if (!line.addChild(child)) {
                mLines.add(line);
                line = new Line(wSize - paddingHorizontal, mHorizontalSpace);
                line.addChild(child);
            }
        }

        /* 处理最后一行 */
        if (!mLines.contains(line) && line.getChildCount() > 0) {
            mLines.add(line);
        }

        int resultWidth = 0, resultHeight = 0;
        /*
        * 如果FlowLayout的宽度指定为包裹内容, 那么就取所有行中
        * 最长的一行的宽度加上水平内边距作为FlowLayout的宽度
        * */
        if (wMode == MeasureSpec.AT_MOST) {
            for (Line l : mLines) {
                resultWidth = Math.max(resultWidth, l.getLineWidth());
            }
            resultWidth += paddingHorizontal;
        } else {
            resultWidth = wSize;
        }

        /*
        * 如果FlowLayout的高度指定为包裹内容， 那么就将每一行
        * 的高度相加并加上垂直内边距最为FlowLayout的高度
        * */
        if (hMode == MeasureSpec.AT_MOST) {
            Line l;
            for (int i = 0; i < mLines.size(); ++i) {
                l = mLines.get(i);
                resultHeight += l.getLineHeight();
                if (i > 0) {
                    resultHeight += mVerticalSpace;
                }
            }
            resultHeight += paddingVertical;
        } else {
            resultHeight = hSize;
        }
        setMeasuredDimension(resultWidth, resultHeight);
    }

    /**
     * 获取布局内容共占用的高度
     * */
    private int getContentHeight() {
        int contentHeight = 0;
        for (int i = 0; i < mLines.size(); ++i) {
            Line line = mLines.get(i);
            contentHeight += line.getLineHeight();
            if (i > 0) {
                contentHeight += getVerticalSpace();
            }
        }
        return contentHeight;
    }

    /**
     * 获取当前FlowLayout水平方向上的内边距
     * */
    public int getHorizontalPadding() {
        return getPaddingLeft() + getPaddingRight();
    }

    /**
     * 获取当前FlowLayout垂直方向上的内边距
     * */
    public int getVerticalPadding() {
        return getPaddingBottom() + getPaddingTop();
    }

    /**
     * 获取当前FlowLayout的水平间距
     * */
    public int getHorizontalSpace() {
        return mHorizontalSpace;
    }

    /**
     * 获取当前FlowLayout的垂直间距
     * */
    public int getVerticalSpace() {
        return mVerticalSpace;
    }

    /**
     * 设置水平间距值
     * @param px
     *          新的水平间距值，单位px
     * */
    public void setHorizontalSpace(int px) {
        if (px >= 0
                && px != getHorizontalSpace()
                && px < getMeasuredWidth()) {
            mHorizontalSpace = px;
            requestLayout();
        }
    }

    /**
     * 设置垂直间距
     * @param px
     *          新的垂直间距值，单位px
     * */
    public void setVerticalSpace(int px) {
        if (px >= 0
                && px != getVerticalSpace()
                && px < getMeasuredHeight()) {
            mVerticalSpace = px;
            requestLayout();
        }
    }

    /**
     * 设置FlowLayout的水平和垂直间距
     * @param horizontal
     *          新的水平间距, 单位px
     * @param vertical
     *          新的垂直间距, 单位px
     * */
    public void setSpace(int horizontal, int vertical) {
        if ((horizontal != getHorizontalSpace() || vertical != getVerticalSpace())
                && horizontal >= 0 && horizontal < getMeasuredHeight()
                && vertical >= 0 && vertical < getMeasuredHeight()) {
            mHorizontalSpace = horizontal;
            mVerticalSpace = vertical;
            requestLayout();
        }
    }

    /**
     * 获取当前FlowLayout的排列方式
     * */
    public int getFlowGravity() {
        return mFlowGravity;
    }

    /**
     * 设置FlowLayout的排列方式
     * @param flowGravity
     *          新的排列方式，如果不支持指定的方式则不予理会
     * */
    public void setFlowGravity(int flowGravity) {
        if (flowGravity != getFlowGravity()) {
            switch (flowGravity) {
                case CENTER:
                case CENTER_HORIZONTAL:
                case CENTER_VERTICAL:
                case LEFT:
                case RIGHT:
                    mFlowGravity = flowGravity;
                    requestLayout();
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 代表每一行
     * */
    private static class Line {
        private List<View> children;    // 此行中所有的View
        private int lineWidth;          // 此行已占宽度
        private int lineHeight;         // 此行已占高度
        private int maxWidth;           // 此行规定最大可用宽度
        private int horizontalSpace;    // 此行的水平间距

        public Line(int maxWidth, int horizontalSpace) {
            children = new ArrayList<>();
            this.maxWidth = maxWidth;
            this.horizontalSpace = horizontalSpace;
        }

        private void setLineWidth(int width) {
            this.lineWidth = width;
        }

        private void setLineHeight(int height) {
            this.lineHeight = height;
        }

        public int getLineWidth() {
            return lineWidth;
        }

        public int getLineHeight() {
            return lineHeight;
        }

        public int getHorizontalSpace() {
            return horizontalSpace;
        }

        public int getMaxWidth() {
            return maxWidth;
        }

        /**
         * 添加一个View
         * @param child
         *          需要添加View
         * @return
         *          false 当宽度已经达到上限无法在容纳这个View时返回添加失败
         *          true  添加成功
         * */
        public boolean addChild(View child) {
            if (child != null) {
                /*
                * 获取这个View的所占用宽高包括margin, 如果这个View不是此行的第一个View
                * 则还加上水平间距
                * */
                MarginLayoutParams params = (MarginLayoutParams) child.getLayoutParams();
                int childWidth = child.getMeasuredWidth() + params.leftMargin + params.rightMargin;
                int childHeight = child.getMeasuredHeight() + params.bottomMargin + params.topMargin;
                if (getChildCount() > 0) {
                    childWidth += horizontalSpace;
                }

                /* 计算添加这个View后的此行宽度 */
                int willWidth = getLineWidth() + childWidth;

                /*
                * 如果此行为空并且加入此行后宽度超过指定的最大宽度，则对此View进行宽度调整，
                * 使其适应此行最大宽度,原则是减小这个View的宽度而不改变这个View的margin.
                * */
                if (willWidth > maxWidth && getChildCount() == 0) {
                    params.width = maxWidth - params.leftMargin - params.rightMargin;
                    child.setLayoutParams(params);
                    willWidth = maxWidth;
                }

                /*
                * 判断添加这个View后会不会超过限定的最大宽度，如果没有超过则将其添加到此行
                * */
                if (willWidth <= maxWidth) {
                    children.add(child);
                    setLineWidth(willWidth);
                    setLineHeight(Math.max(getLineHeight(), childHeight));
                    return true;
                }
            }

            return false;
        }

        /**
         * 重新计算此行已占的宽度与高度
         * */
        public void calcHeightAndWidth() {
            lineWidth = lineHeight = 0;

            for (int i = 0; i < getChildCount(); ++i) {
                View child = getChildAt(i);
                MarginLayoutParams params = (MarginLayoutParams) child.getLayoutParams();
                int childWidth = child.getMeasuredWidth() + params.leftMargin + params.rightMargin;
                int childHeight = child.getMeasuredHeight() + params.bottomMargin + params.topMargin;
                if (i > 0) {
                    childWidth += horizontalSpace;
                }
                setLineWidth(getLineWidth() + childWidth);
                setLineHeight(Math.max(getLineHeight(), childHeight));
            }
        }

        /**
         * 返回此行View的个数
         * */
        public int getChildCount() {
            return children.size();
        }

        /**
         * 获取指定位置上的View
         * */
        public View getChildAt(int index) {
            if (index < getChildCount()) {
                return children.get(index);
            }

            return null;
        }

        /**
         * 清空此行
         * */
        public void clear() {
            children.clear();
            lineWidth = lineHeight = 0;
        }
    }


    public static class LayoutParams extends ViewGroup.MarginLayoutParams {

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return super.checkLayoutParams(p) && p instanceof LayoutParams;
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }
}
