package org.cheerr.align.library;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

/**
 * Created by chenwei on 15/7/3.
 */
public class NAlignTextView extends TextView {

    private int measureWith;
    private TextPaint mPaint;


    private float maxScalePer = 1.4f; //单行最大的拉伸比

    public NAlignTextView(Context context) {
        super(context);
    }

    public NAlignTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NAlignTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        mPaint = getPaint();
        mPaint.setColor(getCurrentTextColor());
        mPaint.drawableState = getDrawableState();
        measureWith = getMeasuredWidth();
        String text = getText().toString();

        Paint.FontMetrics fm = mPaint.getFontMetrics();
        // 计算行高
        Layout layout = getLayout();

        int gravityX = getGravity() & Gravity.HORIZONTAL_GRAVITY_MASK;
        int gravityY = getGravity() & Gravity.VERTICAL_GRAVITY_MASK;

        // layout.getLayout()在4.4.3出现NullPointerException
        // 如果横向要求居中，就不必两端撑满了
        if (layout == null || gravityX == Gravity.CENTER_HORIZONTAL) {
            super.onDraw(canvas);
            return;
        }

        float singleTextHeight = fm.descent - fm.ascent;

        singleTextHeight = singleTextHeight * layout.getSpacingMultiplier()
                + layout.getSpacingAdd();

        float allTextHeight = layout.getLineCount() * singleTextHeight;
        int height = getMeasuredHeight();

        float firstHeight = getPaddingTop() + getTextSize();

        if (gravityY == Gravity.CENTER_VERTICAL) {
            if (height > allTextHeight) {
                firstHeight += (height - allTextHeight) / 2;
            }
        } else if (gravityY == Gravity.BOTTOM) {
            if (height > allTextHeight) {
                firstHeight += height - allTextHeight;
            }
        }

        float mLineY = firstHeight;

        for (int i = 0; i < layout.getLineCount(); i++) {
            int lineStart = layout.getLineStart(i);
            int lineEnd = layout.getLineEnd(i);
            String line = text.substring(lineStart, lineEnd);
            if (needScale(line) && i < layout.getLineCount() - 1) {
                drawScaledText(canvas, line, getPaddingLeft(), mLineY);
            } else {
                if (gravityX == Gravity.RIGHT || gravityX == Gravity.END) {
                    canvas.drawText(line, getPaddingLeft() + getLineSpace(line), mLineY, mPaint);
                } else {
                    canvas.drawText(line, getPaddingLeft(), mLineY, mPaint);
                }
            }
            mLineY += singleTextHeight;
        }
    }


    private float getLineSpace(String line) {
        float textLenght = StaticLayout.getDesiredWidth(line, mPaint);
        return measureWith - getPaddingLeft() - getPaddingRight() - textLenght;
    }

    private boolean needScale(String line) {
        if (line == null || line.length() == 0) {
            return false;
        } else {
            float textLenght = StaticLayout.getDesiredWidth(line, mPaint);
            //如果需要拉升的区间过大，不推荐拉升
            return line.charAt(line.length() - 1) != '\n' && !(textLenght * maxScalePer < (measureWith - getPaddingLeft() - getPaddingRight()) || line.length() < 3);
        }
    }

    private void drawScaledText(Canvas canvas, String line, float mLineX, float mLineY) {

        if (line == null || line.length() < 1) return;
        float textLenght = StaticLayout.getDesiredWidth(line, mPaint);
        float x = mLineX;
        float d = 0;

        if (line.length() > 1)
            d = (measureWith - textLenght - getPaddingLeft() - getPaddingRight()) / (line.length() - 1);

        for (int i = 0; i < line.length(); i++) {
            String c = String.valueOf(line.charAt(i));
            float cw = mPaint.measureText(c);
            canvas.drawText(c, x, mLineY, mPaint);
            x += cw + d;
        }
    }

    public void setMaxScalePer(float maxScalePer) {
        this.maxScalePer = maxScalePer < 1f ? 1f : maxScalePer;
    }

}
