package backups;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;

/**
 * @author Administrator
 * @version $Rev$
 * @time 2017-4-5 11:00
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public class mLineaerLayout extends LinearLayout {
    private View mTouchTarget;//记录down事件的目标view
    private int mTargetWidth;//目标view的宽度，用于阴影覆盖
    private float mCenterX;//点击的中心x
    private float mCenterY;//点击的中心Y
    private int mRevealRadius = 10;//其实绘制半径
    private Paint mPaint;//画笔
    private MotionEvent event;//延迟分发的up事件

    //这是构造函数
    public mLineaerLayout(Context context) {
        super(context);
        commonInite();
    }

    public mLineaerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        commonInite();

    }

    private void commonInite() {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.parseColor("#66ffffff"));
        mRevealRadius = 10;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        int action = event.getAction();
        View touchTarget = getTouchTarget(this, x, y);
        if (action == MotionEvent.ACTION_DOWN) {

            if (touchTarget != null && touchTarget.isClickable() && touchTarget.isEnabled()) {
                mTouchTarget = touchTarget;
                initParametersForChild(event, touchTarget);
            }
        } else if (action == MotionEvent.ACTION_UP) {
            if (touchTarget == mTouchTarget) {
                postInvalidate();//通知重绘
                this.event = event;//记录事件
                return true;//暂时消费事件
            }
        }

        return super.dispatchTouchEvent(event);
    }

    private void initParametersForChild(MotionEvent event, View touchTarget) {
        /**
         * 相对屏幕的坐标-this的起始坐标=相对this的坐标
         */
        mTargetWidth = touchTarget.getMeasuredWidth();
        int[] mLocation = new int[2];
        this.getLocationOnScreen(mLocation);
        mCenterX = event.getRawX() - mLocation[0];
        mCenterY = event.getRawY() - mLocation[1];


    }

    private View getTouchTarget(View view, int x, int y) {
        View target = null;
        ArrayList<View> TouchableViews = view.getTouchables();
        for (View child : TouchableViews) {
            if (isTouchPointInView(child, x, y)) {
                target = child;
                break;
            }
        }

        return target;
    }

    private boolean isTouchPointInView(View view, int x, int y) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int left = location[0];
        int top = location[1];
        int right = left + view.getMeasuredWidth();
        int bottom = top + view.getMeasuredHeight();
        if (view.isClickable() && y >= top && y <= bottom
                && x >= left && x <= right) {
            return true;
        }
        return false;
    }

    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mTargetWidth <= 0 || mTouchTarget == null) {
            return;
        }
        int[] location = new int[2];
        int[] mLocation = new int[2];
        this.getLocationOnScreen(mLocation);
        mTouchTarget.getLocationOnScreen(location);
        // 计算当前目标view 的 l t r b
        int top = location[1] - mLocation[1];
        int left = location[0] - mLocation[0];
        int right = left + mTouchTarget.getMeasuredWidth();
        int bottom = top + mTouchTarget.getMeasuredHeight();

        canvas.save();
        canvas.clipRect(left, top, right, bottom);//是以this为参照的坐标系
        canvas.drawCircle(mCenterX, mCenterY, mRevealRadius, mPaint);
        canvas.restore();

        if (mRevealRadius < mTargetWidth && mRevealRadius > 0) {
            if (mRevealRadius >= mTargetWidth / 2) {
                mRevealRadius += 30;
            } else
                mRevealRadius += 20;
            postInvalidateDelayed(15, left, top, right, bottom);
            canvas.restore();
        } else if (mRevealRadius >= mTargetWidth) {
            mRevealRadius = 0;
            postInvalidateDelayed(15, left, top, right, bottom);
            canvas.restore();
            over();

        }
        if (mRevealRadius == 0) {
            mRevealRadius = 10;
        }
    }

    private void over() {

        if (mTouchTarget == null || !mTouchTarget.isEnabled()) {
            return;
        }

        if (isTouchPointInView(mTouchTarget, (int) event.getRawX(), (int) event.getRawY())) {
            //分发延迟的up事件
            mTouchTarget.dispatchTouchEvent(event);
            mRevealRadius = 10;
            mTouchTarget = null;
        }
    }
}

