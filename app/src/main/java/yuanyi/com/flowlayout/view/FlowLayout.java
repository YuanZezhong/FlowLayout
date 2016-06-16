package yuanyi.com.flowlayout.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * 流式布局
 */
public class FlowLayout extends ViewGroup {
    private List<Line> mLines;          // 记录所有行
    private int horizontalSpace = 16;   // 水平方向上的间距
    public FlowLayout(Context context) {
        this(context, null);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mLines = new ArrayList<>();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int x = getPaddingLeft();
        int y = getPaddingTop();

        for (Line line : mLines) {
            for (int i = 0; i < line.getChildCount(); ++i) {
                View child = line.getChildAt(i);
                MarginLayoutParams params = (MarginLayoutParams) child.getLayoutParams();
                int left = params.leftMargin;
                int right = params.rightMargin;
                int top = params.topMargin;
                child.layout(x += left, y + top, x += child.getMeasuredWidth(), y + top + child.getMeasuredHeight());
                x += right;
            }
            x = getPaddingLeft();
            y += line.getLineHeight();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int wMode = MeasureSpec.getMode(widthMeasureSpec);
        int wSize = MeasureSpec.getSize(widthMeasureSpec);
        int hSize = MeasureSpec.getSize(heightMeasureSpec);
        int hMode = MeasureSpec.getMode(heightMeasureSpec);
        int paddingHorizontal = getPaddingLeft() + getPaddingRight();
        int paddingVertical = getPaddingBottom() + getPaddingTop();

        mLines.clear();
        Line line = new Line(wSize - paddingHorizontal, horizontalSpace);
        for (int i = 0; i < getChildCount(); ++i) {
            View child = getChildAt(i);
            measureChildWithMargins(child, widthMeasureSpec,
                    paddingHorizontal, heightMeasureSpec, paddingVertical);
            if (!line.addChild(child)) {
                mLines.add(line);
                line = new Line(wSize - paddingHorizontal, horizontalSpace);
                line.addChild(child);
            }
        }
        if (!mLines.contains(line) && line.getChildCount() > 0) {
            mLines.add(line);
        }

        int resultWidth = 0, resultHeight = 0;
        if (wMode == MeasureSpec.AT_MOST) {
            for (Line l : mLines) {
                resultWidth = Math.max(resultWidth, l.getLineWidth());
            }
            resultWidth += paddingHorizontal;
        } else {
            resultWidth = wSize;
        }

        if (hMode == MeasureSpec.AT_MOST) {
            for (Line l : mLines) {
                resultHeight += l.getLineHeight();
            }
            resultHeight += paddingVertical;
        } else {
            resultHeight = hSize;
        }
        setMeasuredDimension(resultWidth, resultHeight);
    }

    /**
     * 代表每一行
     * */
    private static class Line {
        private List<View> children;    // 此行中所有的View
        private int lineWidth;          // 此行已占宽度
        private int lineHeight;         // 此行已占高度
        private int maxWidth;           // 此行规定最大宽度
        private int horizontalSpace;    // 水平间距

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

        /**
         * 添加一个View
         * @param child
         *          需要添加View
         *
         * @return
         *          false 当宽度已经达到上限无法在容纳这个View时返回添加失败
         *          true  添加成功
         * */
        public boolean addChild(View child) {
            if (child != null) {
                // 获取View的边距
                MarginLayoutParams params = (MarginLayoutParams) child.getLayoutParams();
                int childWidth = child.getMeasuredWidth() + params.leftMargin + params.rightMargin;
                int childHeight = child.getMeasuredHeight() + params.bottomMargin + params.topMargin;

                // 判断此行能否在容纳下这个View
                int temp = getLineWidth() + childWidth;
                if (temp <= maxWidth) {
                    children.add(child);
                    setLineWidth(temp);
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

            if (getChildCount() > 0) {
                for (int i = 0; i < getChildCount(); ++i) {
                    View child = getChildAt(i);
                    MarginLayoutParams params = (MarginLayoutParams) child.getLayoutParams();
                    int childWidth = child.getMeasuredWidth() + params.leftMargin + params.rightMargin;
                    int childHeight = child.getMeasuredHeight() + params.bottomMargin + params.topMargin;
                    setLineWidth(getLineWidth() + childWidth);
                    setLineHeight(Math.max(getLineHeight(), childHeight));
                }
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
