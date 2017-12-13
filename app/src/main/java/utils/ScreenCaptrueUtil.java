package utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;

import icox.com.scrawlnote.MyApplocation;

import static utils.SDcardUtil.getPicturesPath;
import static utils.SDcardUtil.isRoot;

/**
 * @author 陈锦业
 * @version $Rev$
 * @time 2017-5-25 9:47
 * @des ${截屏工具}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public class ScreenCaptrueUtil {

    private static final String TAG = "MainActivity123";
    private final File nameImageFile;
    private final Context mContext;

    private SimpleDateFormat dateFormat = null;
    private String strDate = null;
    private String pathImage = null;
    private String nameImage = null;

    private static MediaProjection mMediaProjection = null;
    private static VirtualDisplay mVirtualDisplay = null;

    public static int mResultCode = 0;
    public static Intent mResultData = null;
    public static MediaProjectionManager mMediaProjectionManager1 = null;

    private WindowManager mWindowManager1 = null;
    private static int windowWidth = 0;
    private static int windowHeight = 0;
    private ImageReader mImageReader = null;
    private DisplayMetrics metrics = null;
    private int mScreenDensity = 0;

    public ScreenCaptrueUtil(Context context) {
        mContext = context;
        dateFormat = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
        strDate = dateFormat.format(new java.util.Date());
        try {
            pathImage = getPicturesPath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        nameImage = pathImage + strDate + ".png";
        nameImageFile = new File(nameImage);
    }

    public ScreenCaptrueUtil(Context context, String path) {
        mContext = context;
        dateFormat = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
        strDate = dateFormat.format(new java.util.Date());
        try {
            pathImage = path;
        } catch (Exception e) {
            e.printStackTrace();
        }
        nameImage = pathImage + "/" + strDate + ".png";
        nameImageFile = new File(nameImage);
    }

    public File screenCapture() throws Exception {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //                if (mMediaProjectionManager1 == null) {
            createVirtualEnvironment();
            //                }
            startVirtual();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startCapture();
                }
            }, 500);


        } else {

            Bitmap bitmap = captureScreen();
            if (bitmap == null) {
                Toast.makeText(MyApplocation.getContext(), "需要Root权限！", Toast.LENGTH_SHORT).show();
                return null;
            }
        }

        return nameImageFile;
    }


    /**
     * 截屏
     *
     * @param
     * @return
     */
    public Bitmap captureScreen() {

        // 获取屏幕大小：
        Log.i(TAG, "created captureScreen()   " + isRoot());
        DisplayMetrics metrics = new DisplayMetrics();

        WindowManager WM = (WindowManager) MyApplocation.getContext().getSystemService(Context.WINDOW_SERVICE);

        Display display = WM.getDefaultDisplay();

        display.getMetrics(metrics);

        int height = metrics.heightPixels; // 屏幕高

        int width = metrics.widthPixels; // 屏幕的宽

        // 获取显示方式

        int pixelformat = display.getPixelFormat();

        PixelFormat localPixelFormat1 = new PixelFormat();

        PixelFormat.getPixelFormatInfo(pixelformat, localPixelFormat1);

        int deepth = localPixelFormat1.bytesPerPixel;// 位深

        byte[] piex = new byte[height * width * deepth];

        try {

            Runtime.getRuntime().exec(

                    new String[]{"/system/bin/su", "-c",

                            "chmod 777 /dev/graphics/fb0"});

        } catch (IOException e) {

            e.printStackTrace();
            Log.e(TAG, "e = " + e);
            return null;
        }

        try {

            // 获取fb0数据输入流

            InputStream stream = new FileInputStream(new File(

                    "/dev/graphics/fb0"));

            DataInputStream dStream = new DataInputStream(stream);

            dStream.readFully(piex);

        } catch (Exception e) {

            e.printStackTrace();
            Log.e(TAG, "e = " + e);
            return null;
        }

        // 保存图片

        int[] colors = new int[height * width];

        for (int m = 0; m < colors.length; m++) {

            int r = (piex[m * 4] & 0xFF);

            int g = (piex[m * 4 + 1] & 0xFF);

            int b = (piex[m * 4 + 2] & 0xFF);

            int a = (piex[m * 4 + 3] & 0xFF);

            colors[m] = (a << 24) + (r << 16) + (g << 8) + b;

        }

        // piex生成Bitmap

        Bitmap bitmap = Bitmap.createBitmap(colors, width, height,

                Bitmap.Config.ARGB_8888);

        FileOutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream(nameImageFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "FileNotFoundException = " + e);
        }
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        return bitmap;

    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void createVirtualEnvironment() {
        mMediaProjectionManager1 = (MediaProjectionManager) mContext.getApplicationContext().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mWindowManager1 = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        mWindowManager1.getDefaultDisplay().getRealSize(point);
        windowWidth = point.x;
        windowHeight = point.y;
        metrics = new DisplayMetrics();
        mWindowManager1.getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        mImageReader = ImageReader.newInstance(windowWidth, windowHeight, 0x1, 2); //ImageFormat.RGB_565  0x1
        Log.e(TAG, "windowWidth = " + windowWidth + "     , windowHeight = " + windowHeight + "    mImageReader =" + mImageReader);
        Log.i(TAG, "prepared the virtual environment");
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void startVirtual() {
        if (mMediaProjection != null) {
            Log.i(TAG, "want to display virtual");
            virtualDisplay();
        } else {
            Log.i(TAG, "start screen capture intent");
            Log.i(TAG, "want to build mediaprojection and display virtual");
            setUpMediaProjection();
            virtualDisplay();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setUpMediaProjection() {
        mResultData = ((MyApplocation) mContext.getApplicationContext()).getIntent();
        mResultCode = ((MyApplocation) mContext.getApplicationContext()).getResult();
        mMediaProjectionManager1 = ((MyApplocation) mContext.getApplicationContext()).getMediaProjectionManager();
        mMediaProjection = mMediaProjectionManager1.getMediaProjection(mResultCode, mResultData);
        Log.i(TAG, "mMediaProjection defined");
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void virtualDisplay() {
        mVirtualDisplay = mMediaProjection.createVirtualDisplay("screen-mirror",
                windowWidth, windowHeight, mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(), null, null);
        Log.i(TAG, "virtual displayed");
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startCapture() {

        Image image = mImageReader.acquireLatestImage();
        int width = image.getWidth();
        int height = image.getHeight();
        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;
        Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
        image.close();
        Log.i(TAG, "image data captured");

        if (bitmap != null) {
            try {
                if (!nameImageFile.exists()) {
                    nameImageFile.createNewFile();
                    Log.i(TAG, "image file created");
                }
                FileOutputStream out = new FileOutputStream(nameImageFile);
                if (out != null) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                    out.close();
                    Intent media = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri contentUri = Uri.fromFile(nameImageFile);
                    media.setData(contentUri);
                    MyApplocation.getContext().sendBroadcast(media);
                    Log.i(TAG, "screen image saved");
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void tearDownMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
        Log.i(TAG, "mMediaProjection undefined");
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void stopVirtual() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        mVirtualDisplay = null;
        Log.i(TAG, "virtual display stopped");
    }
}
