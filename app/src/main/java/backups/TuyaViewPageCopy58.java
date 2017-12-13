package backups;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import constants.DrawPath;
import icox.com.scrawlnote.NotePageActivity;

import static icox.com.scrawlnote.R.drawable.eraser;

public class TuyaViewPageCopy58 extends View implements View.OnTouchListener {

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
    private DrawPath dp;
    private int screenWidth, screenHeight;
    public int currentColor = Color.BLACK;
    public int backgroundColor = Color.WHITE;
    public int currentSize = 5;
    public int currentStyle = 1;
    private boolean isRedo = false;

    private float lastTouchX;
    private float lastTouchY;
    private final RectF dirtyRect = new RectF();
    ArrayList<Path> mPaths = new ArrayList<Path>();
    private NotePageActivity mNoteActivity;
    private RelativeLayout.LayoutParams mParams;
    private InputMethodManager mImm;
    private ImageView mEraserView;
    private Bitmap mEraser;


    public TuyaViewPageCopy58(Context context, Bitmap bitmapc, int w, int h) {
        super(context);
        this.context = context;
        screenWidth = w;
        screenHeight = h;
        mInBitmap = bitmapc;
        initCanvas();

        setBackground(new BitmapDrawable(bitmapc));

        mBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_4444);
        mCanvas = new Canvas(mBitmap);  //所有mCanvas画的东西都被保存在了mBitmap中

        mSaveBitmap = Bitmap.createBitmap(bitmapc).copy(Bitmap.Config.ARGB_4444, true);
        mSaveCanvas = new Canvas(mSaveBitmap);
    }


    public TuyaViewPageCopy58(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.context = context;
        initDraftCanvas();
    }


    public void setInBitmap(Bitmap bitmap) {
        mInBitmap = bitmap;
    }

    public void destroyBitmap() {
        if (mInBitmap != null && !mInBitmap.isRecycled()) {
            mInBitmap.recycle();
            mInBitmap = null;
            Log.i(TAG,"destroy mInBitmap = "+mInBitmap.isRecycled());
        }
        if (null != mBitmap && !mBitmap.isRecycled()) {
            mBitmap.recycle();
            mBitmap = null;
            Log.i(TAG,"destroy mBitmap = "+mBitmap);
        }
        if (null != mSaveBitmap && !mSaveBitmap.isRecycled()) {
            mSaveBitmap.recycle();
            mSaveBitmap = null;
            Log.i(TAG,"destroy mSaveBitmap = "+mSaveBitmap);
        }
    }


    public TuyaViewPageCopy58(Context context, int w, int h) {
        super(context);
        Log.i(TAG,"new 新的");
        this.context = context;
        screenWidth = w;
        screenHeight = h;

        mEraserView = new ImageView(context);
        mEraserView.setImageResource(eraser);
        mEraser = BitmapFactory.decodeResource(context.getResources(), eraser);

        initCanvas();
        mBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_4444);
        mCanvas = new Canvas(mBitmap);  //所有mCanvas画的东西都被保存在了mBitmap中
        mSaveBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_4444);
        mSaveCanvas = new Canvas(mSaveBitmap);
        mSaveCanvas.drawColor(backgroundColor);
        setBackgroundColor(backgroundColor);
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

    private void initDraftCanvas() {
        mImm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        setPaintStyle();
        mBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);

        this.setOnTouchListener(this);
    }

    public void initCanvas() {
        initDraftCanvas();
        mNoteActivity = (NotePageActivity) context;
        ViewGroup.MarginLayoutParams marginLayoutParams =
                new ViewGroup.MarginLayoutParams(mNoteActivity.mNoteWriteIvEraser.getLayoutParams());
        mParams = new RelativeLayout.LayoutParams(marginLayoutParams);
    }

    //初始化画笔样式
    private void setPaintStyle() {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);// 设置外边缘
        mPaint.setStrokeCap(Paint.Cap.ROUND);// 形状
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setFilterBitmap(true);
        if (currentStyle == 1) {
            mPaint.setStrokeWidth(currentSize);
            mPaint.setColor(currentColor);
        } else {//橡皮擦
            //            mPaint.setAlpha(0);
            //            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
            mPaint.setColor(backgroundColor);
            mPaint.setStrokeWidth(70);
        }


        //        // 设置光源的方向
        //        float[] direction = new float[]{1, 1, 1};
        //        //设置环境光亮度
        //        float light = 0.4f;
        //        // 选择要应用的反射等级
        //        float specular = 6;
        //        // 向mask应用一定级别的模糊
        //        float blur = 4f;
        //
        //
        //        EmbossMaskFilter maskFilter = new EmbossMaskFilter(direction,
        //                light, specular, blur);
        //        mPaint.setMaskFilter(maskFilter);

    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 将前面已经画过得显示出来
        canvas.drawBitmap(mBitmap, 0, 0, null);
        // 实时的显示
        if (mPath != null) {
            canvas.drawPath(mPath, mPaint);
        }
    }

    private void touch_start(float x, float y) {
        mPath.moveTo(x, y);
        mPath.lineTo(x + 1, y + 1);
        mX = x;
        mY = y;
    }

    private void touch_move(float x, float y) {

        float endX = (mX + x) / 2;
        float endY = (mY + y) / 2;
        mPath.quadTo(mX, mY, endX, endY);
        //        mCanvas.drawLine(mX,mY,x,y,mPaint);
        mX = x;
        mY = y;
    }

    private void touch_up(float x, float y) {
        float endX = (mX + x) / 2;
        float endY = (mY + y) / 2;
        mPath.quadTo(mX, mY, endX, endY);
        mCanvas.drawPath(mPath, mPaint);
        //将一条完整的路径保存下来(相当于入栈操作)
        savePath.add(dp);
        Log.d("TuyaView", dp.paint + "   " + dp.path);
        mPath = null;// 重新置空
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

                mBitmap.recycle();
                mBitmap = null;
                mSaveBitmap.recycle();
                mSaveBitmap = null;

                mBitmap = Bitmap.createBitmap(mInBitmap).copy(Bitmap.Config.ARGB_4444, true);
                mCanvas = new Canvas(mBitmap);  //所有mCanvas画的东西都被保存在了mBitmap中
                mSaveBitmap = Bitmap.createBitmap(mInBitmap).copy(Bitmap.Config.ARGB_4444, true);
                mSaveCanvas = new Canvas(mSaveBitmap);
                mSaveCanvas.drawColor(backgroundColor);
                Iterator<DrawPath> iter = savePath.iterator();
                while (iter.hasNext()) {
                    DrawPath drawPath1 = iter.next();
                    mCanvas.drawPath(drawPath1.path, drawPath1.paint);
                    mSaveCanvas.drawPath(drawPath1.path, drawPath1.paint);
                }
            } else {
                redrawOnBitmap();
            }
            invalidate();// 刷新
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

        mBitmap.recycle();
        mBitmap = null;
        mSaveBitmap.recycle();
        mSaveBitmap = null;


        mBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_4444);
        mCanvas = new Canvas(mBitmap);  //所有mCanvas画的东西都被保存在了mBitmap中
        mSaveBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_4444);
        mSaveCanvas = new Canvas(mSaveBitmap);
        mSaveCanvas.drawColor(backgroundColor);

        Iterator<DrawPath> iter = savePath.iterator();
        while (iter.hasNext()) {
            DrawPath drawPath = iter.next();
            mCanvas.drawPath(drawPath.path, drawPath.paint);
            mSaveCanvas.drawPath(drawPath.path, drawPath.paint);
        }
        invalidate();// 刷新
    }

    private void refreshCanvas(){
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
            mCanvas.drawPath(dp.path, dp.paint);
            //将该路径从删除的路径列表中去除
            deletePath.remove(deletePath.size() - 1);
            invalidate();
        }
    }

    private int mode = 0;
    private int longSize;
    private boolean isMulti = false;
    private boolean isWrite = false;

    private float downX;
    private float downY;

    private int count = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        float x = event.getX();
        float y = event.getY();

        if (!isMulti) {
            initEraser(x, y);
        }

        int historySize = event.getHistorySize();
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mode = 1;
                resetDirtyRect(x, y);
                // 每次down下去重新new一个Path
                mPath = new Path();
                //每一次记录的路径对象是不一样的
                dp = new DrawPath();
                dp.path = mPath;
                dp.paint = mPaint;
                mPath.moveTo(x, y);
                mX = x;
                mY = y;
                downX = x;
                downY = y;
                longSize = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                //                initEraser(event.getRawX(), event.getRawY());

                if (mImm != null) {
                    mImm.hideSoftInputFromWindow(mNoteActivity.getWindow().getDecorView().getWindowToken(),
                            0);
                }

                for (int i = 0; i < historySize; i++) {
                    float historicalX = event.getHistoricalX(i);
                    float historicalY = event.getHistoricalY(i);
                    expandDirtyRect(historicalX, historicalY);
                }

                if (mode >= 2) {

                } else {
                    if (!isMulti) {

                        if (Math.abs(x - mX) > 3 || Math.abs(y - mY) > 3) {
                            float endX = (mX + x) / 2;
                            float endY = (mY + y) / 2;
                            mPath.quadTo(mX, mY, endX, endY);
                            isWrite = true;
                        }


                        if (longSize > 80 && currentStyle == 0) {
                            mPath.moveTo(mX, mY);
                            longSize = 0;
                        }
                        longSize += 2;
                    }
                }

                mX = x;
                mY = y;

                break;
            case MotionEvent.ACTION_UP:
                mode = 0;
                for (int i = 0; i < historySize; i++) {
                    float historicalX = event.getHistoricalX(i);
                    float historicalY = event.getHistoricalY(i);
                    expandDirtyRect(historicalX, historicalY);
                }

                if (!isMulti) {
                    float endX2 = (mX + x) / 2;
                    float endY2 = (mY + y) / 2;
                    mPath.quadTo(mX, mY, endX2, endY2);
                    mCanvas.drawPath(mPath, mPaint);
                    mSaveCanvas.drawPath(mPath, mPaint);
                    savePath.add(dp);
                }
                mPath = null;// 重新置空
                isMulti = false;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mode -= 1;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (isWrite) {
                    float endX2 = (mX + x) / 2;
                    float endY2 = (mY + y) / 2;
                    mPath.quadTo(mX, mY, endX2, endY2);
                    mCanvas.drawPath(mPath, mPaint);
                    mSaveCanvas.drawPath(mPath, mPaint);
                    savePath.add(dp);
                    isWrite = false;
                }
                isMulti = true;
                mode += 1;
                break;
        }

        postInvalidate((int) (dirtyRect.left - currentSize),
                (int) (dirtyRect.top - currentSize),
                (int) (dirtyRect.right - currentSize),
                (int) (dirtyRect.bottom - currentSize));

        lastTouchX = x;
        lastTouchY = y;
        return true;
    }

    public void initEraser(float x, float y) {

//        mOnTouchEraser.onTouchEraserListener(x, y);
        if (currentStyle == 0) {
            int x1 = (int) x;
            int y1 = (int) y;
            int width = mNoteActivity.mNoteWriteIvEraser.getWidth() / 2;
            int height = (int) (mNoteActivity.mNoteWriteIvEraser.getHeight() / 1.8);
            mParams.setMargins(x1 - width, y1 - height, width - x1 - width,
                    height - y1 - height);
            mNoteActivity.mNoteWriteIvEraser.setLayoutParams(mParams);
            //            int dx = (int) (x - mX);
            //            int dy = (int) (y - mY);
            //            int mL = mEraserView.getLeft() + dx;
            //            int mB = mEraserView.getBottom() + dy;
            //            int mR = mEraserView.getRight() + dx;
            //            int mT = mEraserView.getTop() + dy;
            //            mNoteActivity.mNoteWriteIvEraser.layout(mL,mT,mR,mB);
            //            mEraserView.layout(mL,mT,mR,mB);
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
    public String saveToSDCard(String time, String content) {

        //        for (int i = 0; i < savePath.size(); i++) {
        //            mSaveCanvas.drawPath(savePath.get(i).path, savePath.get(i).paint);
        //        }
        mPaint.setColor(backgroundColor);
        mPaint.setStrokeWidth(3);
        mSaveCanvas.drawPoint(0, 0, mPaint);

        Log.d("TuyaView", "savePath.size() = " + savePath.size());
        String sdPath = Environment.getExternalStorageDirectory() + "/ScrawlNote";

        //获得系统当前时间，并以该时间作为文件名
        File f = new File(sdPath);
        if (!f.exists()) {
            f.mkdirs();
        }
        Log.d("TuyaView", "文件 = " + f.exists());

        String path = sdPath + "/" + content + "__" + time + ".png";
        File file = new File(path);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("TuyaView", "e = " + e);
        }
        Log.d("TuyaView", "fos = " + fos + " path = " + path);
        mSaveBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

        return file.getPath();
    }

    public void selectorCanvasColor(int color) {
        mBitmap.recycle();
        mBitmap = null;
        mSaveBitmap.recycle();
        mSaveBitmap = null;

        mBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_4444);
        mCanvas = new Canvas(mBitmap);  //所有mCanvas画的东西都被保存在了mBitmap中
        mSaveBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_4444);
        mSaveCanvas = new Canvas(mSaveBitmap);
        mSaveCanvas.drawColor(color);

        Iterator<DrawPath> iter = savePath.iterator();
        while (iter.hasNext()) {
            DrawPath drawPath = iter.next();
            if (drawPath.paint.getColor() == backgroundColor) {
                drawPath.paint.setColor(color);
            }
            mCanvas.drawPath(drawPath.path, drawPath.paint);
            mSaveCanvas.drawPath(drawPath.path, drawPath.paint);
        }
        invalidate();// 刷新

        backgroundColor = color;
        setBackgroundColor(color);
    }

    //选择画笔大小
    public void selectPaintSize(int which) {
        currentSize = which;
        setPaintStyle();
        mPaint.setStrokeWidth(which);

    }


    //设置画笔颜色
    public void selectPaintColor(int which) {
        currentStyle = 1;
        currentColor = which;
        setPaintStyle();

    }

    private boolean isEraser = false;

    //设置橡皮擦
    public void selectEraser() {
        isEraser = !isEraser;
        if (currentStyle == 1) {
            currentStyle = 0;
        } else {
            currentStyle = 1;
        }
        setPaintStyle();
        //        mPaint.setColor(Color.TRANSPARENT);
        //        mPaint.setAlpha(0);
        //        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    public void setConstants(List<DrawPath> savePaths,int currentColor,int backgroundColor,int currentSize,int currentStyle){
        this.currentColor = currentColor;
        this.backgroundColor = backgroundColor;
        this.currentSize = currentSize;
        this.currentStyle = currentStyle;
        this.savePath = savePaths;

        destroyBitmap();
        initCanvas();
        mBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_4444);
        mCanvas = new Canvas(mBitmap);  //所有mCanvas画的东西都被保存在了mBitmap中
        mSaveBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_4444);
        mSaveCanvas = new Canvas(mSaveBitmap);
        mSaveCanvas.drawColor(backgroundColor);

        Iterator<DrawPath> iter = savePath.iterator();
        while (iter.hasNext()) {
            DrawPath drawPath = iter.next();
            if (drawPath.paint.getColor() == backgroundColor) {
                drawPath.paint.setColor(backgroundColor);
            }
            mCanvas.drawPath(drawPath.path, drawPath.paint);
            mSaveCanvas.drawPath(drawPath.path, drawPath.paint);
        }
        invalidate();// 刷新
        setBackgroundColor(backgroundColor);
    }

}
