package view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.SeekBar;

import icox.com.scrawlnote.R;

/**
 * @author Administrator
 * @version $Rev$
 * 垂直SeekBar
 */
public class VerticalSeekBar extends SeekBar {

    public VerticalSeekBar(Context context) {
        super(context);
        init();
    }

    public VerticalSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public VerticalSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(h, w, oldh, oldw);
    }

    private void init() {
        this.setMax(100);
        this.setThumbOffset(dip2px(getContext(), 20));
                this.setBackgroundResource(R.drawable.test_seekbar_bg);
        int padding = dip2px(getContext(),(float)20);
        this.setPadding(padding, padding, padding, padding);
//        this.setProgressDrawable(getResources().getDrawable(R.drawable.test_seek));
    }

    public int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
    }

    protected void onDraw(Canvas c) {
        //将SeekBar转转90度
        c.rotate(-90);
        //将旋转后的视图移动回来
        c.translate(-getHeight(),0);
        Log.i("getHeight()",getHeight()+"");
        super.onDraw(c);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                int i=0;
                //获取滑动的距离
                i=getMax() - (int) (getMax() * event.getY() / getHeight());
                //设置进度
                setProgress(i);
                Log.i("Progress",getProgress()+"");
                //每次拖动SeekBar都会调用
                onSizeChanged(getWidth(), getHeight(), 0, 0);
                Log.i("getWidth()",getWidth()+"");
                Log.i("getHeight()",getHeight()+"");
                break;

            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return true;
    }

}
