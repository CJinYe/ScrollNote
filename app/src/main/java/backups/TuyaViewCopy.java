package backups;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static android.graphics.Color.BLACK;

public class TuyaViewCopy extends View {
    private Context context;
    private Bitmap mBitmap;
    private Bitmap mInBitmap;
    private Canvas mCanvas;
    private Path mPath;
    private Paint mBitmapPaint;// 画布的画笔
    private Paint mPaint;// 真实的画笔
    private float mX, mY;// 临时点坐标
    private static final float TOUCH_TOLERANCE = 2;
    // 保存Path路径的集合,用List集合来模拟栈
    private static List<DrawPath> savePath;
    // 保存已删除Path路径的集合
    private static List<DrawPath> deletePath;
    // 记录Path路径的对象
    private DrawPath dp;
    private int screenWidth, screenHeight;
    private int currentColor = BLACK;
    private int currentSize = 1;
    private int currentStyle = 1;
    private int[] paintColor;//颜色集合
    private boolean isRedo = false;

    public TuyaViewCopy(Context context, Bitmap bitmapc, int w, int h) {
        super(context);
        this.context = context;
        screenWidth = w;
        screenHeight = h;
        mInBitmap = bitmapc;
        paintColor = new int[]{
                Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, BLACK, Color.GRAY, Color.CYAN, Color.TRANSPARENT
        };
        setLayerType(LAYER_TYPE_SOFTWARE, null);//设置默认样式，去除dis-in的黑色方框以及clear模式的黑线效果
        initCanvas();
        //        mBitmap = bitmapc;
        mBitmap = Bitmap.createBitmap(bitmapc).copy(Bitmap.Config.ARGB_8888, true);

        //        mBitmap.eraseColor(Color.argb(0, 0, 0, 0));
        mCanvas = new Canvas(mBitmap);  //所有mCanvas画的东西都被保存在了mBitmap中
        //        mCanvas.drawColor(Color.WHITE);
        savePath = new ArrayList<DrawPath>();
        deletePath = new ArrayList<DrawPath>();
    }

    private class DrawPath {
        public Path path;// 路径
        public Paint paint;// 画笔
    }

    public TuyaViewCopy(Context context, int w, int h) {
        super(context);
        this.context = context;
        screenWidth = w;
        screenHeight = h;
        paintColor = new int[]{
                Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, BLACK, Color.GRAY, Color.CYAN
        };
        setLayerType(LAYER_TYPE_SOFTWARE, null);//设置默认样式，去除dis-in的黑色方框以及clear模式的黑线效果
        initCanvas();
        mBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
        mBitmap.eraseColor(Color.argb(0, 0, 0, 0));
        mCanvas = new Canvas(mBitmap);  //所有mCanvas画的东西都被保存在了mBitmap中
        //画布颜色
        mCanvas.drawColor(Color.WHITE);
        savePath = new ArrayList<DrawPath>();
        deletePath = new ArrayList<DrawPath>();
    }

    public void initCanvas() {
        setPaintStyle();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        //画布大小
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
    }

    @Override
    public void onDraw(Canvas canvas) {
        //canvas.drawColor(0xFFAAAAAA);
        // 将前面已经画过得显示出来
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        if (mPath != null) {
            // 实时的显示
            canvas.drawPath(mPath, mPaint);
        }
    }

    private void touch_start(float x, float y) {
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touch_move(float x, float y) {
        //        float dx = Math.abs(x - mX);
        //        float dy = Math.abs(mY - y);
        //        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
        //            // 从x1,y1到x2,y2画一条贝塞尔曲线，更平滑(直接用mPath.lineTo也可以)
        //        }
        //            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
        ////            mPath.lineTo(mX,mY);
        //            mX = x;
        //            mY = y;

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
                mBitmapPaint = new Paint(Paint.DITHER_FLAG);
                mBitmap = Bitmap.createBitmap(mInBitmap).copy(Bitmap.Config.ARGB_8888, true);
                mCanvas = new Canvas(mBitmap);  //所有mCanvas画的东西都被保存在了mBitmap中

                Iterator<DrawPath> iter = savePath.iterator();
                while (iter.hasNext()) {
                    DrawPath drawPath1 = iter.next();
                    mCanvas.drawPath(drawPath1.path, drawPath1.paint);
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
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        mBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
        //        mBitmap.eraseColor(Color.argb(0, 0, 0, 0));
        mCanvas = new Canvas(mBitmap);  //所有mCanvas画的东西都被保存在了mBitmap中
        mCanvas.drawColor(Color.WHITE);
        Iterator<DrawPath> iter = savePath.iterator();
        while (iter.hasNext()) {
            DrawPath drawPath = iter.next();
            mCanvas.drawPath(drawPath.path, drawPath.paint);
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 每次down下去重新new一个Path
                mPath = new Path();
                //每一次记录的路径对象是不一样的
                dp = new DrawPath();
                dp.path = mPath;
                dp.paint = mPaint;
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up(x, y);
                invalidate();
                break;
        }
        return true;
    }

    //保存到sd卡
    public String saveToSDCard() {
        //获得系统当前时间，并以该时间作为文件名
        File f = new File(getDiskCacheDir(context));
        if (!f.exists()) {
            f.mkdirs();
        }
            Log.d("TuyaView", "文件 = " + f.exists());

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        String path = getDiskCacheDir(context)+"/" + formatter.format(curDate) + "paint.png";
        File file = new File(path);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("TuyaView", "e = " + e);
        }
        if (mBitmap == null) {
        }
        Log.d("TuyaView", "fos = " + fos + " path = " + path);
        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

        //        ByteArrayOutputStream baos = null;
        //        FileOutputStream fos = null;
        //        String path = null;
        //        File file = null;
        //        try {
        //            path = Environment.getExternalStorageDirectory() + File.separator + formatter.format(curDate) + ".png";
        //            file = new File(path);
        //            fos = new FileOutputStream(file);
        //            baos = new ByteArrayOutputStream();
        //            //如果设置成Bitmap.compress(CompressFormat.JPEG, 100, fos) 图片的背景都是黑色的
        //            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        //            byte[] b = baos.toByteArray();
        //            if (b != null) {
        //                fos.write(b);
        //            }
        //        } catch (IOException e) {
        //            e.printStackTrace();
        //        } finally {
        //            try {
        //                if (fos != null) {
        //                    fos.close();
        //                }
        //                if (baos != null) {
        //                    baos.close();
        //                }
        //            } catch (IOException e) {
        //                e.printStackTrace();
        //            }
        //        }

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
        mPaint.setColor(which);
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
