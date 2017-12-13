package service;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import org.greenrobot.eventbus.EventBus;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;

import bean.NoteBean;
import bean.TuyaViewBean;
import constants.Constants;
import icox.com.scrawlnote.MyApplocation;
import icox.com.scrawlnote.PostilActivity;
import icox.com.scrawlnote.R;
import utils.SpUtils;

import static utils.SDcardUtil.getPicturesPath;
import static utils.SDcardUtil.isRoot;

public class PostilService extends Service {
    private static LinearLayout mFloatLayout = null;
    private WindowManager.LayoutParams wmParams = null;
    private WindowManager mWindowManager = null;
    private LayoutInflater inflater = null;
    private ImageButton mFloatView = null;

    private static final String TAG = "MainActivity123";

    private SimpleDateFormat dateFormat = null;
    private String strDate = null;
    private String pathImage = null;
    private String nameImage = null;

    private MediaProjection mMediaProjection = null;
    private VirtualDisplay mVirtualDisplay = null;

    public int mResultCode = 0;
    public Intent mResultData = null;
    public MediaProjectionManager mMediaProjectionManager1 = null;

    public static SparseArray<TuyaViewBean> postilList = new SparseArray<>();


    private WindowManager mWindowManager1 = null;
    private int windowWidth = 0;
    private int windowHeight = 0;
    private ImageReader mImageReader = null;
    private DisplayMetrics metrics = null;
    private int mScreenDensity = 0;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

        //        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        //            if (Settings.canDrawOverlays(this)) {
        //                createFloatView();
        //            }
        //        } else {
        //        }

        createFloatView();
        SpUtils spUtils = new SpUtils(this);
        boolean isOpen = spUtils.getBoolean(Constants.ON_OFF_POSTIL, true);
        Log.i("setcovi", "is = " + isOpen);
        if (!isOpen) {
            hideView();
        } else {
        }

        //        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        //            createVirtualEnvironment();
        //        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    public static void hideView() {
        //        if (mFloatView != null)
        //            mFloatView.setVisibility(View.GONE);
        if (mFloatLayout != null)
            mFloatLayout.setVisibility(View.GONE);
    }

    public static void showView() {
        if (mFloatLayout != null)
            mFloatLayout.setVisibility(View.VISIBLE);
        //        if (mFloatView != null)
        //            mFloatView.setVisibility(View.VISIBLE);
    }

    private void createFloatView() {
        wmParams = new WindowManager.LayoutParams();
        mWindowManager = (WindowManager) getApplication().getSystemService(getApplication().WINDOW_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            wmParams.type = LayoutParams.TYPE_TOAST;
        } else {
            wmParams.type = LayoutParams.TYPE_SYSTEM_ERROR;
        }

        wmParams.format = PixelFormat.RGBA_8888;
        wmParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE;
        wmParams.gravity = Gravity.LEFT | Gravity.TOP;
        wmParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
        wmParams.x = 0;
        wmParams.y = 0;
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        inflater = LayoutInflater.from(getApplication());
        mFloatLayout = (LinearLayout) inflater.inflate(R.layout.float_layout, null);
        mWindowManager.addView(mFloatLayout, wmParams);
        mFloatView = (ImageButton) mFloatLayout.findViewById(R.id.float_id);

        mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
                .makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        mFloatView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                wmParams.x = (int) event.getRawX() - mFloatView.getMeasuredWidth() / 2;
                wmParams.y = (int) event.getRawY() - mFloatView.getMeasuredHeight() / 2 - 25;
                mWindowManager.updateViewLayout(mFloatLayout, wmParams);
                return false;
            }
        });

        mFloatView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // hide the button
                //                mFloatView.setVisibility(View.INVISIBLE);


                if (!PostilActivity.isCreate) {
                    //                    Intent intent = new Intent(getApplicationContext(), NoteLandscapeActivity.class);

                    Intent intent = new Intent(getApplicationContext(), PostilActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    startActivity(intent);
                }
                //点击的时候给批注发信息
                EventBus.getDefault().post(new NoteBean());

                //                Handler handler1 = new Handler();
                //                handler1.postDelayed(new Runnable() {
                //                    public void run() {
                //                        //start virtual
                ////                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ////
                ////                            try {
                ////                                startVirtual();
                ////                            } catch (Exception e) {
                ////                                e.printStackTrace();
                ////                                Toast.makeText(getApplication(), "请检查设备权限！", Toast.LENGTH_SHORT).show();
                ////                            }
                ////
                ////                        } else {
                ////                            Bitmap bitmap = captureScreen();
                ////                            if (bitmap == null) {
                ////                                Toast.makeText(getApplication(), "需要Root权限！", Toast.LENGTH_SHORT).show();
                ////                            }
                ////                        }
                //
                //                            Bitmap bitmap = captureScreen();
                //                            if (bitmap == null) {
                //                                Toast.makeText(getApplication(), "需要Root权限！", Toast.LENGTH_SHORT).show();
                //                            }
                //                        Handler handler2 = new Handler();
                //                        handler2.postDelayed(new Runnable() {
                //                            public void run() {
                //                                //capture the screen
                //                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ////                                    startCapture();
                //                                } else {
                //                                }
                //                                Handler handler3 = new Handler();
                //                                handler3.postDelayed(new Runnable() {
                //                                    public void run() {
                //                                        mFloatView.setVisibility(View.VISIBLE);
                //                                        //stopVirtual();
                //                                    }
                //                                }, 500);
                //                            }
                //                        }, 500);
                //                    }
                //                }, 500);
            }
        });

        Log.i(TAG, "created the float sphere view");
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

        WindowManager WM = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

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

        String pathImage = null;
        try {
            pathImage = getPicturesPath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
        String strDate = dateFormat.format(new java.util.Date());
        String nameImage = pathImage + strDate + ".png";
        File file = new File(nameImage);
        FileOutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "FileNotFoundException = " + e);
        }
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        return bitmap;

    }

    private void createVirtualEnvironment() {
        dateFormat = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
        strDate = dateFormat.format(new java.util.Date());
        pathImage = Environment.getExternalStorageDirectory().getPath() + "/Pictures/";
        nameImage = pathImage + strDate + ".png";
        mMediaProjectionManager1 = (MediaProjectionManager) getApplication().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mWindowManager1 = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        windowWidth = mWindowManager1.getDefaultDisplay().getWidth();
        windowHeight = mWindowManager1.getDefaultDisplay().getHeight();
        metrics = new DisplayMetrics();
        mWindowManager1.getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        mImageReader = ImageReader.newInstance(windowWidth, windowHeight, ImageFormat.RGB_565, 2); //ImageFormat.RGB_565  0x1

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
        mResultData = ((MyApplocation) getApplication()).getIntent();
        mResultCode = ((MyApplocation) getApplication()).getResult();
        mMediaProjectionManager1 = ((MyApplocation) getApplication()).getMediaProjectionManager();
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
        strDate = dateFormat.format(new java.util.Date());
        nameImage = pathImage + strDate + ".png";

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
                File fileImage = new File(nameImage);
                if (!fileImage.exists()) {
                    fileImage.createNewFile();
                    Log.i(TAG, "image file created");
                }
                FileOutputStream out = new FileOutputStream(fileImage);
                if (out != null) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                    out.close();
                    Intent media = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri contentUri = Uri.fromFile(fileImage);
                    media.setData(contentUri);
                    this.sendBroadcast(media);
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
    private void tearDownMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
        Log.i(TAG, "mMediaProjection undefined");
    }

    private void stopVirtual() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        mVirtualDisplay = null;
        Log.i(TAG, "virtual display stopped");
    }

    @Override
    public void onDestroy() {
        // to remove mFloatLayout from windowManager
        super.onDestroy();
        if (mFloatLayout != null) {
            mWindowManager.removeView(mFloatLayout);
        }
        tearDownMediaProjection();
        Log.i(TAG, "application destroy");
    }
}