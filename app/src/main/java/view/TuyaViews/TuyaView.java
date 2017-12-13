package view.TuyaViews;

import android.content.Context;
import android.graphics.Bitmap;
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
import android.widget.RelativeLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import backups.NoteActivity;

import static android.graphics.Color.BLACK;

public class TuyaView extends View {

    private final String TAG = "TuyaView";
    private Context context;
    private Bitmap mBitmap;
    private Bitmap mInBitmap;
    private Bitmap mSaveBitmap;
    private Canvas mCanvas;
    private Canvas mSaveCanvas;
    private Path mPath;
    private Paint mBitmapPaint;// 画布的画笔
    private Paint mPaint;// 真实的画笔
    private float mX, mY;// 临时点坐标
    private static final float TOUCH_TOLERANCE = 2;
    // 保存Path路径的集合,用List集合来模拟栈
    private static List<DrawPath> savePath = new ArrayList<>();
    // 保存已删除Path路径的集合
    private static List<DrawPath> deletePath = new ArrayList<>();
    // 记录Path路径的对象
    private DrawPath dp;
    private int screenWidth, screenHeight;
    public int currentColor = BLACK;
    private int currentSize = 10;
    private int currentStyle = 1;
    private boolean isRedo = false;

    private float lastTouchX;
    private float lastTouchY;
    private final RectF dirtyRect = new RectF();
    ArrayList<Path> mPaths = new ArrayList<Path>();
    private Bitmap mBitmapEraser;
    private NoteActivity mNoteActivity;
    private RelativeLayout.LayoutParams mParams;
    private InputMethodManager mImm;


    public TuyaView(Context context, Bitmap bitmapc, int w, int h) {
        super(context);
        this.context = context;
        screenWidth = w;
        screenHeight = h;
        mInBitmap = bitmapc;
        initCanvas();

        setBackground(new BitmapDrawable(bitmapc));

        mBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);  //所有mCanvas画的东西都被保存在了mBitmap中

        mSaveBitmap = Bitmap.createBitmap(bitmapc).copy(Bitmap.Config.ARGB_8888, true);
        mSaveCanvas = new Canvas(mSaveBitmap);
    }

    private class DrawPath {
        public Path path;// 路径
        public Paint paint;// 画笔
    }

    public TuyaView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.context = context;
        initDraftCanvas();
    }

    public void setInBitmap(Bitmap bitmap) {
        mInBitmap = bitmap;
    }


    public TuyaView(Context context, int w, int h) {
        super(context);
        this.context = context;
        screenWidth = w;
        screenHeight = h;
        initCanvas();
        setBackgroundColor(Color.WHITE);
        mBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
        mBitmap.eraseColor(Color.argb(0, 0, 0, 0));
        mCanvas = new Canvas(mBitmap);  //所有mCanvas画的东西都被保存在了mBitmap中

        mSaveBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
        mSaveCanvas = new Canvas(mSaveBitmap);
        mSaveCanvas.drawColor(Color.WHITE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        screenWidth = MeasureSpec.getSize(widthMeasureSpec);
        screenHeight = MeasureSpec.getSize(heightMeasureSpec);
        if (mBitmap == null && mInBitmap != null) {
            setBackground(new BitmapDrawable(mInBitmap));
            mBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
            mBitmap.eraseColor(Color.argb(0, 0, 0, 0));
            mCanvas = new Canvas(mBitmap);  //所有mCanvas画的东西都被保存在了mBitmap中
            mSaveBitmap = Bitmap.createBitmap(mInBitmap).copy(Bitmap.Config.ARGB_8888, true);
            mSaveCanvas = new Canvas(mSaveBitmap);
        } else if (mBitmap == null) {
            mBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
            mBitmap.eraseColor(Color.argb(0, 0, 0, 0));
            mCanvas = new Canvas(mBitmap);  //所有mCanvas画的东西都被保存在了mBitmap中
        }
    }

    private void initDraftCanvas() {
        mImm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        setPaintStyle();
        mBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
    }

    public void initCanvas() {
        initDraftCanvas();
        mNoteActivity = (NoteActivity) context;
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
            mPaint.setColor(Color.WHITE);
            mPaint.setStrokeWidth(50);
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

                mBitmap = Bitmap.createBitmap(mInBitmap).copy(Bitmap.Config.ARGB_8888, true);
                mCanvas = new Canvas(mBitmap);  //所有mCanvas画的东西都被保存在了mBitmap中
                mSaveBitmap = Bitmap.createBitmap(mInBitmap).copy(Bitmap.Config.ARGB_8888, true);
                mSaveCanvas = new Canvas(mSaveBitmap);

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
        /*mBitmap = Bitmap.createBitmap(screenWidth, screenHeight,
                Bitmap.Config.RGB_565);
        mCanvas.setBitmap(mBitmap);// 重新设置画布，相当于清空画布*/

        setPaintStyle();
        mBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);  //所有mCanvas画的东西都被保存在了mBitmap中
        mSaveBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
        mSaveCanvas = new Canvas(mSaveBitmap);

        Iterator<DrawPath> iter = savePath.iterator();
        while (iter.hasNext()) {
            DrawPath drawPath = iter.next();
            mCanvas.drawPath(drawPath.path, drawPath.paint);
            mSaveCanvas.drawPath(drawPath.path, drawPath.paint);
        }
        invalidate();// 刷新
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
    private float oldDist;
    private boolean isMulti = false;
    private boolean isWrite = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float x = event.getX();
        float y = event.getY();
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
                initEraser(x, y);
                break;
            case MotionEvent.ACTION_MOVE:

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
                    Log.i(TAG, "mode = " + mode);
                } else {
                    if (!isMulti) {
                        Log.i(TAG, "mode = " + mode);
                        if (Math.abs(x - mX) > 4 || Math.abs(y - mY) > 4) {
                            float endX = (mX + x) / 2;
                            float endY = (mY + y) / 2;
                            mPath.quadTo(mX, mY, endX, endY);
                            //                            mCanvas.drawPath(mPath, mPaint);
                            isWrite = true;
                            initEraser(x, y);
                        }
                    }
                }

                mX = x;
                mY = y;

                break;
            case MotionEvent.ACTION_UP:
                Log.i(TAG, "ACTION_UP = " + MotionEvent.ACTION_UP);
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
                    mSaveCanvas.drawPath(mPath,mPaint);
                    savePath.add(dp);
                }
                mPath = null;// 重新置空
                isMulti = false;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                Log.i(TAG, "ACTION_POINTER_UP = " + MotionEvent.ACTION_POINTER_UP);
                mode -= 1;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                Log.i(TAG, "ACTION_POINTER_DOWN = " + MotionEvent.ACTION_POINTER_DOWN);
                if (isWrite) {
                    float endX2 = (mX + x) / 2;
                    float endY2 = (mY + y) / 2;
                    mPath.quadTo(mX, mY, endX2, endY2);
                    mCanvas.drawPath(mPath, mPaint);
                    mSaveCanvas.drawPath(mPath,mPaint);
                    savePath.add(dp);
                    isWrite = false;
                }
                isMulti = true;
                oldDist = spacing(event);
                mode += 1;
                break;
        }

        invalidate((int) (dirtyRect.left - currentSize),
                (int) (dirtyRect.top - currentSize),
                (int) (dirtyRect.right - currentSize),
                (int) (dirtyRect.bottom - currentSize));


        lastTouchX = x;
        lastTouchY = y;

        return true;
    }

    private void initEraser(float x, float y) {

        if (currentColor == Color.WHITE) {
            int x1 = (int) x;
            int y1 = (int) y;
            int width = mNoteActivity.mNoteWriteIvEraser.getWidth() / 2;
            int height = mNoteActivity.mNoteWriteIvEraser.getHeight() / 2;
            mParams.setMargins(x1 - width, y1 - height, width - x1 + width,
                    height - y1 + height);
            mNoteActivity.mNoteWriteIvEraser.setLayoutParams(mParams);
        }

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

    public String getDiskCacheDir(Context context) {
        String cachePath = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return cachePath;
    }

    public void selectorCanvasColor(int color) {
        switch (color) {
            case Color.GREEN:
                mCanvas.drawColor(Color.GREEN);
                break;
            case Color.WHITE:
                mCanvas.drawColor(Color.WHITE);
                break;
            case Color.BLACK:
                mCanvas.drawColor(Color.BLACK);
                break;
            case Color.YELLOW:
                mCanvas.drawColor(Color.YELLOW);
                break;
            case Color.BLUE:
                mCanvas.drawColor(Color.BLUE);
                break;
            default:
                break;
        }

        for (int i = 0; i < savePath.size(); i++) {
            DrawPath drawPath = savePath.get(i);
            mCanvas.drawPath(drawPath.path, drawPath.paint);
        }
        invalidate();
    }

    //选择画笔大小
    public void selectPaintSize(int which) {
        //int size = Integer.parseInt(this.getResources().getStringArray(R.array.paintsize)[which]);
        currentSize = which;
        currentStyle = 1;
        setPaintStyle();
        mPaint.setStrokeWidth(which);

    }


    //设置画笔颜色
    public void selectPaintColor(int which) {
        currentStyle = 1;
        currentColor = which;
        setPaintStyle();
        mPaint.setColor(currentColor);
        if (which == Color.WHITE) {
            mPaint.setStrokeWidth(80);
        }

    }

    //设置橡皮擦
    public void selectEraser() {
        if (currentStyle == 0) {
            currentStyle = 1;
            setPaintStyle();
        } else {
            currentStyle = 0;
            setPaintStyle();
        }
    }

}
