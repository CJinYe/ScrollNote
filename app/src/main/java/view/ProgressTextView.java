package view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;

import icox.com.scrawlnote.R;

import static android.graphics.BitmapFactory.decodeResource;

/**
 * @author Administrator
 * @version $Rev$
 *          SeekBar顶部数字
 */
public class ProgressTextView extends View {
    private static final String TAG = ProgressTextView.class.getSimpleName();

    private int mTextColor = Color.WHITE;
    private int mHeight;
    private int mWidth;
    private double mOneProgressWidth;
    private int mCurProgress = 0;
    private String mProgressText = "";
    private int mMaxProgress = 70;
    private Paint mPaint;
    private float mThumbOffset;
    private int mTextSize = 36;
    public Bitmap mBackgroundBit;

    public ProgressTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (null != context && attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ProgressTextView);
            mTextColor = array.getColor(R.styleable.ProgressTextView_ptv_textColor, Color.WHITE);
            mTextSize = array.getDimensionPixelSize(R.styleable.ProgressTextView_ptv_thumWidth, 36);
            float thumWidth = array.getDimension(R.styleable.ProgressTextView_ptv_thumWidth, 20);
            mThumbOffset = (thumWidth + 3) / 2;
        }
        initObserver();
        mBackgroundBit = decodeResource(context.getResources(), R.drawable.paopao);
    }

    private void initObserver() {
        ViewTreeObserver vto = getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

            @Override
            public boolean onPreDraw() {
                mHeight = getMeasuredHeight();
                mWidth = getMeasuredWidth();
                initPaint();
                initData();
                return true;
            }
        });
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setTextSize(mTextSize);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setColor(mTextColor);
    }

    private void initData() {
        mOneProgressWidth = (double) (mWidth - 4 * mThumbOffset) / (mMaxProgress);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawText(canvas);
        super.onDraw(canvas);
    }

    //设置字体居中显示
    private void drawText(Canvas canvas) {
        float x = (float) (mCurProgress * mOneProgressWidth);
        float textWidth = mPaint.measureText(mProgressText);
        float textOffset = textWidth / 2;
        //        if (x + textOffset+20 > mWidth - mThumbOffset) {//超过view的右边
        //            float exWidth = x + textOffset - (mWidth - mThumbOffset);
        //            x -= exWidth;//避免超过右边
        //        }
        if (x + mThumbOffset < textOffset) {//超过左边
            float exWidth = textOffset - (x + mThumbOffset);
            x += exWidth;//避免超过左边
        }

        if (x + 20 > mWidth) {
            x -= 20;
        }


        canvas.drawBitmap(mBackgroundBit, x - 5, 0, mPaint);
        //        canvas.translate(mThumbOffset, 0);
        canvas.drawText(mProgressText, x - 5 + mBackgroundBit.getWidth() / 2, mBackgroundBit.getHeight() / 2, mPaint);
    }

    //设置显示的进度位置和字符串
    public void setProgress(int progress, String showText) {
        mCurProgress = progress;
        mProgressText = showText;
        invalidate();
    }
}
