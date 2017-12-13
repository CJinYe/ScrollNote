package backups;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import bean.TuyaViewBean;
import constants.DrawPath;
import constants.NoteBeanConf;
import utils.CacheBitmapUtil;
import utils.SDcardUtil;
import utils.TouchMeasureUtil;

public class TuyaViewPageLinShi extends View {

    private final String TAG = "TuyaView";
    private Context context;
    public Bitmap mBitmap = null;
    public Bitmap mInBitmap = null;
    public Bitmap mSaveBitmap = null;
    private Canvas mCanvas;
    private Canvas mSaveCanvas;
    private Path mPath;
    private Paint mBitmapPaint;// 画布的画笔
    public Paint mPaint;// 真实的画笔
    private float mX, mY;// 临时点坐标
    private final float TOUCH_TOLERANCE = 2;
    // 保存Path路径的集合,用List集合来模拟栈
    public List<DrawPath> savePath = new ArrayList<>();
    // 保存已删除Path路径的集合
    private List<DrawPath> deletePath = new ArrayList<>();
    // 记录Path路径的对象
    private DrawPath mDrawPath;
    private int screenWidth, screenHeight;
    public int currentColor = Color.BLACK;//画笔颜色
    public int backgroundColor = Color.WHITE;//画布颜色
    public int currentSize = 5;//画笔大小
    public int currentStyle = 1;//画笔类型,1为正常,0为橡皮擦
    public final int PAINT_STYLE_ERASER = 0;
    public final int PAINT_STYLE_PAINT = 1;
    private boolean isRedo = false;//是否重做

    private float lastTouchX;
    private float lastTouchY;
    private final RectF dirtyRect = new RectF();
    ArrayList<Path> mPaths = new ArrayList<Path>();
    private RelativeLayout.LayoutParams mParams;
    private InputMethodManager mImm;
    private ImageView mEraserView;
    private Bitmap mEraser;
    private long mStartTime;
    private long mEndTime;
    private float startX, startY, endX, endY;
    private Path mPathUp;
    private Path mPathDown;
    private Paint mPaintUp;
    private Paint mPaintDown;
    private float downX;
    private float downY;
    private Activity mNoteActivity;

    //是否更换了背景颜色
    public boolean mIsChangerBackground = false;
    private BitmapShader mBitmapShaderEraser;
    private Matrix mMatrixTemp;


    public TuyaViewPageLinShi(Context context, Bitmap bitmapc, int w, int h) {
        super(context);
        this.context = context;
        screenWidth = w;
        screenHeight = h;
        mInBitmap = bitmapc;

        setBackground(new BitmapDrawable(bitmapc));

        mBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_4444);
        mCanvas = new Canvas(mBitmap);  //所有mCanvas画的东西都被保存在了mBitmap中

        mSaveBitmap = Bitmap.createBitmap(bitmapc).copy(Bitmap.Config.ARGB_4444, true);
        mSaveCanvas = new Canvas(mSaveBitmap);
        initCanvas();
    }

    public TuyaViewPageLinShi(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.context = context;
        initCanvas();
    }

    public void setInBitmap(Bitmap bitmap) {
        mInBitmap = bitmap;
    }

    public void destroyBitmap() {
        if (mInBitmap != null && !mInBitmap.isRecycled()) {
            mInBitmap.recycle();
            mInBitmap = null;
        }
        //        if (null != mBitmap && !mBitmap.isRecycled()) {
        //            mBitmap.recycle();
        //            mBitmap = null;
        //            Log.i(TAG, "destroy mBitmap = " + mBitmap);
        //        }
        //        if (null != mSaveBitmap && !mSaveBitmap.isRecycled()) {
        //            mSaveBitmap.recycle();
        //            mSaveBitmap = null;
        //            Log.i(TAG, "destroy mSaveBitmap = " + mSaveBitmap);
        //        }
    }

    public TuyaViewPageLinShi(Context context, int w, int h) {
        super(context);
        this.context = context;
        screenWidth = w;
        screenHeight = h;

        mBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_4444);
        mCanvas = new Canvas(mBitmap);  //所有mCanvas画的东西都被保存在了mBitmap中
        mSaveBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_4444);
        mSaveCanvas = new Canvas(mSaveBitmap);
        initCanvas();
        mSaveCanvas.drawColor(backgroundColor);
        setBackgroundColor(backgroundColor);
    }

    public TuyaViewPageLinShi(Context context, Bitmap bitmap, Bitmap saveBitmap) {
        super(context);
        this.context = context;
        mBitmap = bitmap;
        mSaveBitmap = saveBitmap;
        initCanvas();
        mCanvas = new Canvas(mBitmap);
        mSaveCanvas = new Canvas(mSaveBitmap);
        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        mSaveCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        mSaveCanvas.drawColor(backgroundColor);

        setBackgroundColor(backgroundColor);
    }

    public TuyaViewPageLinShi(Context context, Bitmap bitmap, Bitmap saveBitmap, Bitmap inBitmap) {
        super(context);
        this.context = context;
        mBitmap = bitmap;
        mInBitmap = inBitmap;
        mSaveBitmap = saveBitmap;
        initCanvas();
        mCanvas = new Canvas(mBitmap);
        mSaveCanvas = new Canvas(mSaveBitmap);
        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        mSaveCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        mSaveCanvas.drawBitmap(inBitmap, 0, 0, null);
        mCanvas.drawBitmap(inBitmap, 0, 0, null);
        //        setBackground(new BitmapDrawable(inBitmap));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        screenWidth = MeasureSpec.getSize(widthMeasureSpec);
        screenHeight = MeasureSpec.getSize(heightMeasureSpec);
        if (mBitmap == null && mInBitmap != null) {
            setBackground(new BitmapDrawable(mInBitmap));
            mBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_4444);
            mBitmap.eraseColor(Color.argb(0, 0, 0, 0));
            mCanvas = new Canvas(mBitmap);  //所有mCanvas画的东西都被保存在了mBitmap中
            mSaveBitmap = Bitmap.createBitmap(mInBitmap).copy(Bitmap.Config.ARGB_4444, true);
            mSaveCanvas = new Canvas(mSaveBitmap);
        } else if (mBitmap == null) {
            mBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_4444);
            mBitmap.eraseColor(Color.argb(0, 0, 0, 0));
            mCanvas = new Canvas(mBitmap);  //所有mCanvas画的东西都被保存在了mBitmap中
        }
    }


    public void initCanvas() {
        mNoteActivity = (Activity) context;
        mImm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (mInBitmap != null) {
            mBitmapShaderEraser = new BitmapShader(mInBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        } else {
            mBitmapShaderEraser = new BitmapShader(mBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        }
        mMatrixTemp = new Matrix();
        setPaintStyle();
    }

    //初始化画笔样式
    private void setPaintStyle() {
        mPaint = new Paint();
        mPaintUp = new Paint();
        mPaintDown = new Paint();
        mPaint = getInitPaint(mPaint);
        if (currentStyle == 1) {
            mPaint.setStrokeWidth(currentSize);
            mPaint.setColor(currentColor);
        } else {//橡皮擦
            mPaint.setColor(backgroundColor);
            mPaint.setStrokeWidth(70);
        }

        mPaintUp = getInitPaint(mPaintUp);
        mPaintDown = getInitPaint(mPaintDown);
        mPaintUp.setStrokeWidth(currentSize);
        mPaintDown.setStrokeWidth(currentSize);
    }

    private Paint getInitPaint(Paint paint) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);// 设置外边缘
        paint.setStrokeCap(Paint.Cap.ROUND);// 形状
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setFilterBitmap(true);
        paint.setStrokeWidth(currentSize);
        paint.setColor(currentColor);
        return paint;
    }


    /**
     * 撤销
     * 撤销的核心思想就是将画布清空，
     * 将保存下来的Path路径最后一个移除掉，
     * 重新将路径画在画布上面。
     */
    public void undo() {
        if (savePath != null && savePath.size() > 0) {
            DrawPath drawPath = savePath.get(savePath.size() - 1);
            deletePath.add(drawPath);
            savePath.remove(savePath.size() - 1);
            if (mInBitmap != null && !isRedo) {
                setPaintStyle();
                mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                mSaveCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                mSaveCanvas.drawBitmap(mInBitmap, 0, 0, null);
                setBackground(new BitmapDrawable(mInBitmap));
                Iterator<DrawPath> iter = savePath.iterator();
                while (iter.hasNext()) {
                    DrawPath drawPath1 = iter.next();
                    recoverPath(drawPath1);
                }
            } else {
                redrawOnBitmap();
            }
            invalidate();// 刷新
        }
    }

    private void recoverPath(DrawPath drawPath) {
        mCanvas.drawPath(drawPath.path, drawPath.paint);
        mSaveCanvas.drawPath(drawPath.path, drawPath.paint);
        if (drawPath.paintUp != null) {
            mCanvas.drawPath(drawPath.pathUp, drawPath.paintUp);
            mSaveCanvas.drawPath(drawPath.pathUp, drawPath.paintUp);
            mCanvas.drawPath(drawPath.pathDown, drawPath.paintDown);
            mSaveCanvas.drawPath(drawPath.pathDown, drawPath.paintDown);
        }
    }

    /**
     * 重做
     */
    public void redo() {
        isRedo = true;
        if (savePath != null && savePath.size() > 0) {
            savePath.clear();
            redrawOnBitmap();
        }

        if (mInBitmap != null) {
            redrawOnBitmap();
        }
    }

    private void redrawOnBitmap() {
        setPaintStyle();

        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        mSaveCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        mSaveCanvas.drawColor(backgroundColor);

        Iterator<DrawPath> iter = savePath.iterator();
        while (iter.hasNext()) {
            DrawPath drawPath = iter.next();
            recoverPath(drawPath);
        }
        invalidate();// 刷新
    }

    private void refreshCanvas() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;
        options.inPreferredConfig = Bitmap.Config.ARGB_4444;
        options.inPurgeable = true;
        options.inInputShareable = true;
    }

    /**
     * 恢复，恢复的核心就是将删除的那条路径重新添加到savapath中重新绘画即可
     */
    public void recover() {
        if (deletePath.size() > 0) {
            //将删除的路径列表中的最后一个，也就是最顶端路径取出（栈）,并加入路径保存列表中
            DrawPath dp = deletePath.get(deletePath.size() - 1);
            savePath.add(dp);
            //将取出的路径重绘在画布上
            recoverPath(dp);
            //将该路径从删除的路径列表中去除
            deletePath.remove(deletePath.size() - 1);
            invalidate();
        }
    }

    private int mode = 0;
    private int longSize;
    private boolean isMulti = false;
    private boolean isWrite = false;

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if ((mCentreTranX + mTransX) != 0) {
            float left = (mCentreTranX + mTransX) / (mPrivateScale * mScale);
            float top = (mCentreTranY + mTransY) / (mPrivateScale * mScale);
            // 画布和图片共用一个坐标系，只需要处理屏幕坐标系到图片（画布）坐标系的映射关系
            if (mPrivateScale * mScale != 0) {
                canvas.scale(mPrivateScale * mScale, mPrivateScale * mScale); // 缩放画布
                //                this.setScaleX(mPrivateScale * mScale);
                //                this.setScaleY(mPrivateScale * mScale);
            }

            if (!Float.isNaN(left) && !Float.isInfinite(left)) {
                canvas.translate(left, top); // 偏移画布
                //                this.setTranslationX(left);
                //                this.setTranslationY(top);
            }
        }

        if (rotate != 0) {
            //            canvas.rotate(rotate,getWidth()/2,getHeight()/2);
            //            drawRotateBitmap(canvas,mPaint,mBitmap,rotate,0,0);
            rotate = 0;
        }

        // 将前面已经画过得显示出来
        canvas.drawBitmap(mBitmap, 0, 0, null);
        //                setBackground(new BitmapDrawable(mSaveBitmap));
        // 实时的显示
        if (mPathUp != null) {
            canvas.drawPath(mPathUp, mPaintUp);
        }
        if (mPathDown != null) {
            canvas.drawPath(mPathDown, mPaintDown);
        }
        if (mPath != null) {
            canvas.drawPath(mPath, mPaint);
        }
    }

    private long firstTouch = 0;
    private int touchType = 0;
    private final int TOUCH_TYPE_CLEAR = 3; //三点触摸,擦除
    private final int TOUCH_TYPE_ONE = 1; //单点触摸,画画
    private final int TOUCH_TYPE_ZOOM = 2; //两点触摸,放大缩小

    private float mNewDist, mOldDist;
    private float mOldScale = 0;
    private float mTouchSlop = 5;
    private final float mMaxScale = 4f; // 最大缩放倍数
    private final float mMinScale = 1f; // 最小缩放倍数
    private float mTouchCentreX, mTouchCentreY, mToucheCentreXOnGraffiti, mToucheCentreYOnGraffiti;

    private float mScale;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

//        if (!isMulti) {
            initEraser(event.getX(), event.getY());

        float x = toX(event.getX());
        float y = toY(event.getY());


        //得到历史的点
        //        int historySize = event.getHistorySize();

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:

                firstTouch = System.currentTimeMillis();

                //                resetDirtyRect(x, y);
                //画的时候如果有软键盘则隐藏起来
                if (mImm != null) {
                    mImm.hideSoftInputFromWindow(mNoteActivity.getWindow().getDecorView().getWindowToken(),
                            0);
                }

                mode = 1;
                mStartTime = System.currentTimeMillis();
                // 每次down下去重新new一个Path
                mPath = new Path();
                //每一次记录的路径对象是不一样的
                mDrawPath = new DrawPath();
                mDrawPath.path = mPath;
                mDrawPath.paint = mPaint;
                mPath.moveTo(x, y);
                //                mPath.lineTo(x + 1, y + 1);
                mX = x;
                mY = y;
                startX = x;
                startY = y;
                downX = x;
                downY = y;
                longSize = 0;
                break;
            case MotionEvent.ACTION_MOVE:

                if (mode != 2) {
                    if (TouchMeasureUtil.ThreeTouchDistance(event)) {
                        threeEraser();
                        canvasPath(x, y);
                    } else if (!isEraser && mode < 2) {
                        oneCanvasLine();
                        canvasPath(x, y);
                    }


                } else {
                    if (touchType == 0) {
                        touchType = TOUCH_TYPE_ZOOM;
                    }
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                //                if (!isMulti) {//不是多点触控
                if (touchType != TOUCH_TYPE_ZOOM) {//不是多点触控
                    float endX2 = (mX + x) / 2;
                    float endY2 = (mY + y) / 2;
                    mPath.quadTo(mX, mY, endX2, endY2);
                    //把路径画到图片中
                    mCanvas.drawPath(mPath, mPaint);
                    mSaveCanvas.drawPath(mPath, mPaint);

                    mEndTime = System.currentTimeMillis();
                    endX = x;
                    endY = y;
                    if (currentStyle == 1) {//不是橡皮擦
                        calculate();
                        PathMeasure pathMeasure = new PathMeasure(mPath, false);
                        if (pathMeasure.getLength() < 60) {
                            mPath.addPath(mPathUp, 1.1f, 1.1f);
                            mPath.addPath(mPathDown, -1f, -1f);
                        }
                        mCanvas.drawPath(mPathUp, mPaintUp);
                        mCanvas.drawPath(mPathDown, mPaintDown);
                        mSaveCanvas.drawPath(mPathUp, mPaintUp);
                        mSaveCanvas.drawPath(mPathDown, mPaintDown);
                    }

                    //把路径保存到集合
                    savePath.add(mDrawPath);

                }

                mPath = null;// 重新置空
                mPathUp = null;
                mPathDown = null;
                lastResult = 0;
                isMulti = false;//不是多点触控
                lastCalculateTime = 0;
                firstTouch = 0;
                touchType = 0;
                mode = 0;

                invalidate();
                break;
            case MotionEvent.ACTION_POINTER_UP:
                //第二点触控抬起
                mode -= 1;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                //第二点触控
                //                if (firstTouch != 0 && System.currentTimeMillis() - firstTouch < 1000) {
                mode += 1;
                //                }
                break;
        }

        //                        invalidate((int) (dirtyRect.left - currentSize),
        //                                (int) (dirtyRect.top - currentSize),
        //                                (int) (dirtyRect.right - currentSize),
        //                                (int) (dirtyRect.bottom - currentSize));

        lastTouchX = x;
        lastTouchY = y;

        return true;
    }

    /**
     * 三点触摸橡皮擦功能
     */
    private void threeEraser() {
        if (currentStyle == 1) {
            currentStyle = 0;
            setPaintStyle();
            mPaint.setStrokeWidth(90);
            //批注的时候用全透明作为橡皮擦色
            if (currentStyle == 0 && isPostil) {
                mPaint.setColor(Color.TRANSPARENT);
                mPaint.setAlpha(0);
                mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            } else {
                //                mPaint.setShader(mBitmapShaderEraser);
            }

            touchType = TOUCH_TYPE_CLEAR;
        }
    }

    /**
     * 一点触摸画线
     */
    private void oneCanvasLine() {
        if (currentStyle == 0 && touchType != TOUCH_TYPE_CLEAR) {
            currentStyle = 1;
            setPaintStyle();
            touchType = TOUCH_TYPE_ONE;
        }
    }

    private void canvasPath(float x, float y) {
        endX = x;
        endY = y;
        mEndTime = System.currentTimeMillis();


        if (currentStyle == 1 && (Math.abs(x - mX) > 3 || Math.abs(y - mY) > 3)) {
            calculate();
        }

        startX = x;
        startY = y;
        mStartTime = System.currentTimeMillis();

        if (Math.abs(x - mX) > 3 || Math.abs(y - mY) > 3) {
            float endX = (mX + x) / 2;
            float endY = (mY + y) / 2;
            mPath.quadTo(mX, mY, endX, endY);
            isWrite = true;
        }

        mX = x;
        mY = y;
    }

    // 计算两个触摸点的中点
    private PointF calMidPoint(MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        return new PointF(x / 2, y / 2);
    }


    private int mPrivateHeight, mPrivateWidth;// 图片在缩放mPrivateScale倍数的情况下，适应屏幕（mScale=1）时的大小（肉眼看到的在屏幕上的大小）
    private float mCentreTranX, mCentreTranY;// 图片在缩放mPrivateScale倍数的情况下，居中（mScale=1）时的偏移（肉眼看到的在屏幕上的偏移）
    private float mTransX = 0, mTransY = 0; // 图片在相对于居中时且在缩放mScale倍数的情况下的偏移量 （ 图片真实偏移量为　(mCentreTranX + mTransX)/mPrivateScale*mScale ）
    private float mPrivateScale = 1f; // 图片适应屏幕（mScale=1）时的缩放倍数

    /**
     * 缩放倍数，图片真实的缩放倍数为 mPrivateScale*mScale
     *
     * @param scale
     */
    public void setScale(float scale) {
        this.mScale = scale;
        judgePosition();
        resetMatrix();
        invalidate();
    }

    public float getScale() {
        return mScale;
    }

    public void setTrans(float transX, float transY) {
        mTransX = transX;
        mTransY = transY;
        judgePosition();
        resetMatrix();
        invalidate();
    }

    /**
     * 将屏幕触摸坐标x转换成在图片中的坐标
     */
    public float toX(float touchX) {
        float touch = (touchX - mCentreTranX - mTransX) / (mPrivateScale * mScale);
        if (Float.isNaN(touch) || Float.isInfinite(touch)) {
            touch = touchX;
        }
        return touch;
    }

    /**
     * 将屏幕触摸坐标y转换成在图片中的坐标
     */
    public float toY(float touchY) {
        float touch = (touchY - mCentreTranY - mTransY) / (mPrivateScale * mScale);
        if (Float.isNaN(touch) || Float.isInfinite(touch)) {
            touch = touchY;
        }
        return touch;
    }

    private void resetMatrix() {
        // 如果使用了自定义的橡皮擦底图，则需要调整矩阵
        if (currentStyle == PAINT_STYLE_ERASER) {
            //            mMatrixTemp.reset();
            //            mMatrixTemp.preScale(mBitmap.getWidth() * 1f, mBitmap.getHeight() * 1f);
            //            mBitmapShaderEraser.getLocalMatrix(mMatrixTemp);
        }
    }

    /**
     * 坐标换算
     * （公式由toX()中的公式推算出）
     *
     * @param touchX    触摸坐标
     * @param graffitiX 在涂鸦图片中的坐标
     * @return 偏移量
     */
    public final float toTransX(float touchX, float graffitiX) {
        return -graffitiX * (mPrivateScale * mScale) + touchX - mCentreTranX;
    }

    public final float toTransY(float touchY, float graffitiY) {
        return -graffitiY * (mPrivateScale * mScale) + touchY - mCentreTranY;
    }

    /**
     * 调整图片位置
     * <p>
     * 明白下面一点很重要：
     * 假设不考虑任何缩放，图片就是肉眼看到的那么大，此时图片的大小width =  mPrivateWidth * mScale ,
     * 偏移量x = mCentreTranX + mTransX，而view的大小为width = getWidth()。height和偏移量y以此类推。
     */
    private void judgePosition() {
        boolean changed = false;
        if (mPrivateWidth * mScale < getWidth()) { // 限制在view范围内
            if (mTransX + mCentreTranX < 0) {
                mTransX = -mCentreTranX;
                changed = true;
            } else if (mTransX + mCentreTranX + mPrivateWidth * mScale > getWidth()) {
                mTransX = getWidth() - mCentreTranX - mPrivateWidth * mScale;
                changed = true;
            }
        } else { // 限制在view范围外
            if (mTransX + mCentreTranX > 0) {
                mTransX = -mCentreTranX;
                changed = true;
            } else if (mTransX + mCentreTranX + mPrivateWidth * mScale < getWidth()) {
                mTransX = getWidth() - mCentreTranX - mPrivateWidth * mScale;
                changed = true;
            }
        }
        if (mPrivateHeight * mScale < getHeight()) { // 限制在view范围内
            if (mTransY + mCentreTranY < 0) {
                mTransY = -mCentreTranY;
                changed = true;
            } else if (mTransY + mCentreTranY + mPrivateHeight * mScale > getHeight()) {
                mTransY = getHeight() - mCentreTranY - mPrivateHeight * mScale;
                changed = true;
            }
        } else { // 限制在view范围外
            if (mTransY + mCentreTranY > 0) {
                mTransY = -mCentreTranY;
                changed = true;
            } else if (mTransY + mCentreTranY + mPrivateHeight * mScale < getHeight()) {
                mTransY = getHeight() - mCentreTranY - mPrivateHeight * mScale;
                changed = true;
            }
        }
        if (changed) {
            resetMatrix();
        }
    }

    private int rotate = 0;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.i("onSizeChanged", "width = " + w + "     h = " + h);
        Log.e("onSizeChanged", "oldw = " + oldw + "     oldh = " + oldh);
        if (oldw != 0) {
            Matrix matrix = new Matrix();
            if (w > oldw) {
                matrix.setRotate(-90);
                rotate = -90;
            } else {
                matrix.setRotate(90);
                rotate = 90;
            }
            mCanvas.drawBitmap(mBitmap, matrix, mPaint);
            mSaveCanvas.drawBitmap(mBitmap, matrix, mPaint);
            // 围绕原地进行旋转
            //            Bitmap newBM = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, false);
            //            mBitmap = newBM;
            //            mSaveBitmap = newBM;
            //            mCanvas = new Canvas(mBitmap);
            //            mSaveCanvas = new Canvas(mSaveBitmap);
        }
        setBG();
    }


    private void setBG() {// 不用resize preview
        int w = mBitmap.getWidth();
        int h = mBitmap.getHeight();
        float nw = w * 1f / getWidth();
        float nh = h * 1f / getHeight();
        if (nw > nh) {
            mPrivateScale = 1 / nw;
            mPrivateWidth = getWidth();
            mPrivateHeight = (int) (h * mPrivateScale);
        } else {
            mPrivateScale = 1 / nh;
            mPrivateWidth = (int) (w * mPrivateScale);
            mPrivateHeight = getHeight();
        }
        // 使图片居中
        mCentreTranX = (getWidth() - mPrivateWidth) / 2f;
        mCentreTranY = (getHeight() - mPrivateHeight) / 2f;

        resetMatrix();
        //        initCanvas();
        invalidate();
    }


    //上一次计算的时间,如果间隔太长就不执行
    private long lastCalculateTime = 0;

    private void calculate() {

        //如果两点相隔时间太长就返回
        if (lastCalculateTime != 0 && (System.currentTimeMillis() - lastCalculateTime) > 500) {
            lastCalculateTime = System.currentTimeMillis();
            mPathDown.moveTo(endX, endY);
            mPathUp.moveTo(endX, endY);
            return;
        }

        float velocity = velocityFrom2();
        float radius = (float) (controlPaint(velocity, currentSize) / 1.8);
        float ratio = (float) screenWidth / 1280;
        radius = radius * ratio;

        // 根据角度算出四边形的四个点
        float offsetX = (float) (radius * Math.sin(Math.atan((endY - startY) / (endX - startX))));
        float offsetY = (float) (radius * Math.cos(Math.atan((endY - startY) / (endX - startX))));

        offsetX = Math.abs(offsetX);
        offsetY = Math.abs(offsetY);
        if (Float.isNaN(offsetX) || Float.isInfinite(offsetX)) {
            offsetX = 1;
        }
        if (Float.isNaN(offsetY) || Float.isInfinite(offsetY)) {
            offsetY = 1;
        }


        float x1 = startX - offsetX;
        float y1 = startY + offsetY;

        float x2 = endX - offsetX;
        float y2 = endY + offsetY;

        float x3 = endX + offsetX;
        float y3 = endY - offsetY;

        float x4 = startX + offsetX;
        float y4 = startY - offsetY;

        if (mPathUp == null) {
            mPathUp = new Path();
            mPathUp.moveTo(x1, y1);
            //            mPathUp.lineTo(x2,y2);
            float endX1 = (x2 + x1) / 2;
            float endY1 = (y2 + y1) / 2;
            mPathUp.quadTo(x1, y1, endX1, endY1);

            mPathDown = new Path();
            mPathDown.moveTo(x4, y4);
            //            mPathDown.lineTo(x3,y3);
            float endX2 = (x3 + x4) / 2;
            float endY2 = (y4 + y3) / 2;
            mPathDown.quadTo(x4, y4, endX2, endY2);

            mDrawPath.paintUp = mPaintUp;
            mDrawPath.pathUp = mPathUp;
            mDrawPath.paintDown = mPaintDown;
            mDrawPath.pathDown = mPathDown;
        } else {
            float endX1 = (x2 + x1) / 2;
            float endY1 = (y2 + y1) / 2;
            mPathUp.quadTo(x1, y1, endX1, endY1);

            float endX2 = (x3 + x4) / 2;
            float endY2 = (y4 + y3) / 2;
            mPathDown.quadTo(x4, y4, endX2, endY2);
        }

        lastCalculateTime = System.currentTimeMillis();

    }

    private final float KEY_PAINT_WIDTH = 2.1f;
    private float lastResult = 0;

    private float controlPaint(double velocity, float paintSize) {
        //余弦函数
        //y=0.5*[cos(x*PI)+1]
        float result = KEY_PAINT_WIDTH * paintSize;
        if (velocity <= 0.2) {
            result = (float) ((float) velocity / 3);
        } else if (velocity < 0.5) {
            result = (float) ((float) velocity / 2);

        } else if (velocity < 0.7) {
            result = (float) ((float) velocity / 1.5);

        } else if (velocity < 0.9) {
            result = (float) ((float) velocity / 1);

        } else if (velocity < 1) {
            result = (float) ((float) velocity * 1.2);

        } else if (velocity <= 2) {
            result = (float) velocity * 1.5f;

        } else if (velocity > 2) {
            result = (float) ((float) velocity * 1.8);

        } else if (velocity > 3) {
            result = (float) (0.12 * paintSize * KEY_PAINT_WIDTH * (Math.cos(velocity * Math.PI) + 1));


        } else if (velocity > 4) {
            result = (float) (0.09 * paintSize * KEY_PAINT_WIDTH * (Math.cos(velocity * Math.PI) + 1));


        } else if (velocity > 5) {
            result = (float) (0.08 * paintSize * KEY_PAINT_WIDTH * (Math.cos(velocity * Math.PI) + 1));


        } else if (velocity > 6) {
            result = (float) (0.07 * paintSize * KEY_PAINT_WIDTH * (Math.cos(velocity * Math.PI) + 1));


        } else if (velocity > 7) {
            result = (float) (0.06 * paintSize * KEY_PAINT_WIDTH * (Math.cos(velocity * Math.PI) + 1));


        } else {
            result = (float) (0.05 * paintSize * KEY_PAINT_WIDTH * (Math.cos(velocity * Math.PI) + 1));

        }

        if (result > paintSize / 1.1) {
            result = (float) (paintSize / 1.1);
        }

        if (lastResult != 0) {
            if (lastResult > result * 3) {
                result = ((float) (lastResult / 3 > result * 2 ? lastResult / 3 : result * 2));
            } else if (lastResult < result / 3) {
                result = ((float) (lastResult * 3 < result / 2 ? result / 2 : lastResult * 3));
            }
        }
        lastResult = result;
        return result;
    }

    /**
     * 计算笔画的速度
     *
     * @return 速度
     */
    public float velocityFrom2() {
        float distanceTo = (float) Math.sqrt(Math.pow(startX - endX, 2) + Math.pow(startY - endY, 2));
        float velocity = distanceTo / (mEndTime - mStartTime);
        //        if (velocity != velocity)
        //            return 0f;
        return velocity;
    }

    /**
     * 更新橡皮擦的位置
     *
     * @param x
     * @param y
     */
    public void initEraser(float x, float y) {

        if (currentStyle == 0) {
            mOnTouchEraser.onTouchEraserListener(x, y);
        }

    }


    public interface onTouchEraser {
        void onTouchEraserListener(float x, float y);
    }

    private onTouchEraser mOnTouchEraser;

    public void setOnTouchEraserListener(onTouchEraser onTouchEraser) {
        mOnTouchEraser = onTouchEraser;
    }


    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private void expandDirtyRect(float historicalX, float historicalY) {
        if (historicalX < dirtyRect.left) {
            dirtyRect.left = historicalX;
        } else if (historicalX > dirtyRect.right) {
            dirtyRect.right = historicalX;
        }
        if (historicalY < dirtyRect.top) {
            dirtyRect.top = historicalY;
        } else if (historicalY > dirtyRect.bottom) {
            dirtyRect.bottom = historicalY;
        }
    }

    /**
     * Resets the dirty region when the motion event occurs.
     */
    private void resetDirtyRect(float eventX, float eventY) {
        // The lastTouchX and lastTouchY were set when the ACTION_DOWN
        // motion event occurred.
        dirtyRect.left = Math.min(lastTouchX, eventX);
        dirtyRect.right = Math.max(lastTouchX, eventX);
        dirtyRect.top = Math.min(lastTouchY, eventY);
        dirtyRect.bottom = Math.max(lastTouchY, eventY);
    }

    //保存到sd卡
    public String saveToSDCard(String time, String content) throws Exception {
        mPaint.setColor(backgroundColor);
        mPaint.setStrokeWidth(3);
        mSaveCanvas.drawPoint(0, 0, mPaint);
        String sdPath = SDcardUtil.getBookNoteSavaPath();
        File f = new File(sdPath);
        if (!f.exists()) {
            f.mkdirs();
        }
        String path = sdPath + "/" + content + "__" + time + ".png";
        File file = new File(path);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mSaveBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        return file.getPath();
    }

    //保存到sd卡
    public File saveToSDCard(int count, String content) {
        mPaint.setColor(backgroundColor);
        mPaint.setStrokeWidth(3);
        mSaveCanvas.drawPoint(0, 0, mPaint);
        String sdPath = Environment.getExternalStorageDirectory() + "/ScrawlNote";
        File f = new File(sdPath);
        if (!f.exists()) {
            f.mkdirs();
        }
        String path = sdPath + "/" + content + "第" + count + "张.png";
        File file = new File(path);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mSaveBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        return file;
    }

    /**
     * 选择画布的颜色
     *
     * @param color
     */
    public void selectorCanvasColor(int color) {
        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        mSaveCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        if (!isPostil) {
            mSaveCanvas.drawColor(color);
            Iterator<DrawPath> iter = savePath.iterator();
            while (iter.hasNext()) {
                DrawPath drawPath = iter.next();
                if (drawPath.paint.getColor() == backgroundColor || drawPath.paint.getColor() == Color.TRANSPARENT) {
                    drawPath.paint.setColor(color);
                }
                recoverPath(drawPath);
            }
            invalidate();// 刷新

            backgroundColor = color;
            setBackgroundColor(color);
            mIsChangerBackground = true;
        }
    }

    //选择画笔大小
    public void selectPaintSize(int which) {
        if (which < 1)
            which = 1;
        currentSize = which;
        setPaintStyle();
        mPaint.setStrokeWidth(which);
        mPaintUp.setStrokeWidth(which);
        mPaintDown.setStrokeWidth(which);
    }


    //设置画笔颜色
    public void selectPaintColor(int which) {
        currentStyle = 1;
        currentColor = which;
        mPaint = new Paint();
        mPaintUp = new Paint();
        mPaintDown = new Paint();
        setPaintStyle();

    }

    private boolean isEraser = false;
    public boolean isPostil = false;

    //设置橡皮擦
    public void selectEraser() {

        isEraser = !isEraser;
        //        if (currentStyle == 1) {
        //            currentStyle = 0;
        //        } else {
        //            currentStyle = 1;
        //            mPaint = new Paint();
        //        }

        if (isEraser) {
            currentStyle = 0;
        } else {
            currentStyle = 1;
        }
        setPaintStyle();

        //// TODO: 2017-5-24 暂时考虑用背景颜色做橡皮擦
        //批注的时候用全透明作为橡皮擦色
        if (currentStyle == 0 && isPostil) {
            mPaint.setColor(Color.TRANSPARENT);
            mPaint.setAlpha(0);
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        }

    }

    public void isPostil(boolean isPostil) {
        this.isPostil = isPostil;
    }

    /**
     * 根据缓存的信息,配置画布
     *
     * @param tuyaViewBean
     */
    public void setConstants(TuyaViewBean tuyaViewBean) {
        this.currentColor = tuyaViewBean.currentColor;
        this.backgroundColor = tuyaViewBean.backgroundColor;
        this.currentSize = tuyaViewBean.currentSize;
        this.currentStyle = tuyaViewBean.currentStyle;
        this.savePath = tuyaViewBean.savePaths;
        isRedo = false;
        initCanvas();
        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        mSaveCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        if (tuyaViewBean.bitmapPath != null) {
            mInBitmap = CacheBitmapUtil.createBitmap(tuyaViewBean.bitmapPath.getPath());
            if (mInBitmap == null) {
                mInBitmap = NoteBeanConf.createNullBitmap(screenWidth, screenHeight);
            }

            //如果是更换了背景,就全部变成背景色
            if (tuyaViewBean.isChangerBackground) {
                mSaveCanvas.drawColor(backgroundColor);
                setBackgroundColor(backgroundColor);
            } else {
                setBackground(new BitmapDrawable(mInBitmap));
                mSaveCanvas.drawBitmap(mInBitmap, 0, 0, null);
            }

        } else {
            mSaveCanvas.drawColor(backgroundColor);
            setBackgroundColor(backgroundColor);
        }

        Iterator<DrawPath> iter = savePath.iterator();
        while (iter.hasNext()) {
            DrawPath drawPath = iter.next();
            if (drawPath.paint.getColor() == backgroundColor) {
                drawPath.paint.setColor(backgroundColor);
            }
            recoverPath(drawPath);
        }
        invalidate();// 刷新
    }

    /**
     * 新建画布时找到之前的信息
     *
     * @param bgColor
     * @param paintColor
     * @param paintSize
     * @param paintStyle
     */
    public void setConstants(int bgColor, int paintColor, int paintSize, int paintStyle) {
        if (paintColor != 0)
            this.currentColor = paintColor;
        if (bgColor != 0)
            this.backgroundColor = bgColor;
        if (paintSize != 0)
            this.currentSize = paintSize;
        if (paintStyle != 0)
            this.currentStyle = paintStyle;
        isRedo = false;
        initCanvas();
        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        mSaveCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        mSaveCanvas.drawColor(backgroundColor);
        setBackgroundColor(backgroundColor);
        invalidate();// 刷新
    }
}
