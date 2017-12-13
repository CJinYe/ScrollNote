package adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;

import bean.NoteBean;
import bean.TuyaViewBean;
import constants.DrawPath;
import constants.NoteBeanConf;
import db.NoteDbDao;
import fragment.NoteFragment;
import utils.CacheBitmapUtil;
import utils.ScreenCaptrueUtil;
import view.TuyaViews.TuyaViewPage;

import static constants.NoteBeanConf.nullBitmap;
import static constants.NoteBeanConf.tuyaBeanList;
import static utils.SDcardUtil.getSavaPath;
import static utils.Zip4jUtil.AddFilesWithAESEncryption;

/**
 * @author Administrator
 * @version $Rev$
 * @time 2017-4-27 10:00
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public abstract class PagerAdapterBase extends FragmentStatePagerAdapter {
    private static final String TAG = "PagerAdapter1";
    private final Context mContext;
    private int mScreenWidth;
    private int mScreenHeight;
    private final boolean mIsPostil;
    private NoteBean mNoteBean;
    private String mZipPath;
    public SparseArray<NoteFragment> mTuyaViewMap = new SparseArray<>();
    private int size;
    public NoteFragment mNoteFragment;
    private ArrayList<File> mDesFiles = new ArrayList();
    private final NoteDbDao mNoteDbDao;
    private RelativeLayout.LayoutParams mParams;
    private ImageView mEraserView;
    private int mPosition;

    public PagerAdapterBase(FragmentManager fm, Context context,
                            int screenWidth, int screenHeight,
                            File desFile, boolean isPostil) {
        super(fm);
        mContext = context;
        mNoteDbDao = new NoteDbDao(context);
        mScreenWidth = screenWidth;
        mScreenHeight = screenHeight;
        mIsPostil = isPostil;

        if (desFile != null && desFile.exists()) {
            mZipPath = desFile.getPath();
            File[] desFiles = desFile.listFiles();
            Collections.addAll(mDesFiles, desFiles);
            size = mDesFiles.size();
        } else {
            size = 1;
        }
    }

    public PagerAdapterBase(FragmentManager fm, Context context,
                            int screenWidth, int screenHeight,
                            File desFile, boolean isPostil, NoteBean noteBean) {
        super(fm);
        mContext = context;
        mNoteDbDao = new NoteDbDao(context);
        mScreenWidth = screenWidth;
        mScreenHeight = screenHeight;
        mIsPostil = isPostil;
        mNoteBean = noteBean;
        if (desFile != null && desFile.exists()) {
            mDesFiles.add(desFile);
            size = mDesFiles.size();
        } else {
            size = 1;
        }
        size = 1;

    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
        //        deleteBitmapToCache(mDesFiles[position].getPath());
        mTuyaViewMap.remove(position);
        //        CacheBitmapUtil.deleteBitmapToCache(mDesFiles[position].getPath());
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapterBase.POSITION_NONE;
    }


    @Override
    public Fragment getItem(int position) {
        mNoteFragment = getFragmentItem();
        return mNoteFragment;

    }

    protected abstract NoteFragment getFragmentItem();

    @Override
    public int getCount() {
        return size;
    }

    public int addCount() {
        //        if (Runtime.getRuntime().maxMemory() / 1.13 <= Runtime.getRuntime().totalMemory()) {
        //            Toast.makeText(mContext, "超标了!!!!", Toast.LENGTH_LONG).show();
        //            return 0;
        //        }

        //如果添加图片超过内存值 则不做操作
        if (Runtime.getRuntime().maxMemory() / 1.15 <= nullBitmap.getByteCount() * size / 1.5) {
            Toast.makeText(mContext, "超标了!!!!", Toast.LENGTH_SHORT).show();
            return 0;
        }
        size++;
        notifyDataSetChanged();
        return size;
    }

    private void setEraserListener(TuyaViewPage tuyaViewPage) {
        tuyaViewPage.setOnTouchEraserListener(new TuyaViewPage.onTouchEraser() {
            @Override
            public void onTouchEraserListener(float x, float y) {
                if (mEraserView.getVisibility() == View.VISIBLE) {
                    int x1 = (int) x;
                    int y1 = (int) y;
                    int width = mEraserView.getWidth() / 2;
                    int height = (int) (mEraserView.getHeight() / 1.8);
                    mParams.setMargins(x1 - width, y1 - height, width - x1 - width,
                            height - y1 - height);
                    mEraserView.setLayoutParams(mParams);
                }
            }
        });
    }

    public void setEraserView(ImageView eraserView) {
        ViewGroup.MarginLayoutParams marginLayoutParams =
                new ViewGroup.MarginLayoutParams(eraserView.getLayoutParams());
        mParams = new RelativeLayout.LayoutParams(marginLayoutParams);
        mEraserView = eraserView;
    }

    public void setPaintSize(int currentItem, int paintSize) {
        mTuyaViewMap.get(currentItem).getTuyaView().selectPaintSize(paintSize);
    }


    /**
     * 保存所有图片
     *
     * @param currentItem
     * @param time  时间
     * @param content   标题
     * @param pwd   密码
     * @return
     * @throws Exception
     */
    private String saveZipPath = "";
    private ArrayList<File> mSavePaths;

    public String saveNote(String time, String content, String pwd, String name) {

        mNoteFragment.saveBitmap();

        mSavePaths = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            File file = saveBitmap(content, i);
            mSavePaths.add(file);
        }

        saveZipPath = AddFilesWithAESEncryption(time, content, pwd, mSavePaths, name);

        if (!saveZipPath.contains("保存出错了,错误原因")) {
            for (int i = 0; i < mSavePaths.size(); i++) {
                mSavePaths.get(i).delete();
                NoteBean noteBean = mNoteDbDao.queryNoteZipPaht(saveZipPath, mSavePaths.get(i).getPath());
                //得到每一页对应的文字
                String text;
                if (i >= tuyaBeanList.size()) {
                    text = null;
                } else {
                    text = tuyaBeanList.get(i).text;
                }
                if (noteBean != null) {
                    mNoteDbDao.updateZipNote(null, time, mSavePaths.get(i).getPath(),
                            text, saveZipPath, pwd);
                } else {
                    mNoteDbDao.saveNote(null, time, mSavePaths.get(i).getName(),
                            text, saveZipPath, pwd);
                }
            }
        }

        return saveZipPath;
    }

    public String savePostil(int currentItem, final String time, final String content, final String pwd) throws Exception {

        //        mNoteFragment.saveBitmap();
        ScreenCaptrueUtil screenCaptrueUtil = new ScreenCaptrueUtil(mContext);
        final File file = screenCaptrueUtil.screenCapture();

        if (file == null) {
            return "截屏出错了！";
        }

        String zipPath = "";
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                final ArrayList<File> paths = new ArrayList<>();

                paths.add(file);

                AddFilesWithAESEncryption(time, content, pwd, paths, null);
            }
        }, 500);


        return zipPath;
    }

    public void setWidthHeight(int width, int height) {
        //        mScreenWidth = width;
        //        mScreenHeight = height;
    }

    /**
     * 保存已绘制的图片
     *
     * @param content  标题
     * @param position 索引  @return
     * @throws Exception
     */
    public File saveBitmap(String content, int position) {

        TuyaViewBean tuyaBean = null;

        if (tuyaBeanList.size() > position) {
            tuyaBean = NoteBeanConf.tuyaBeanList.get(position);
        }

        //        Bitmap bitmap = NoteBeanConf.createSaveBitmap(mScreenWidth, mScreenHeight);
        Bitmap bitmap = Bitmap.createBitmap(mScreenWidth, mScreenHeight, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        Bitmap inBitmap = null;

        //保存的绘制信息不为空,是在图片上面做操作的
        if (null != tuyaBean && tuyaBean.bitmapPath != null) {
            inBitmap = CacheBitmapUtil.createBitmap(tuyaBean.bitmapPath.getPath());
            //如果背景颜色没有变化,就用之前的bitmap做背景
            if (inBitmap != null) {
                //没有画布颜色
                if (!tuyaBean.isChangerBackground) {
                    canvas = new Canvas(bitmap);
                    canvas.drawBitmap(inBitmap, 0, 0, null);

                } else {
                    canvas.drawColor(tuyaBean.backgroundColor);
                }
            } else {
                canvas.drawColor(tuyaBean.backgroundColor);
            }
        } else if (mDesFiles != null && mDesFiles.size() > position) {
            inBitmap = CacheBitmapUtil.createBitmap(mDesFiles.get(position).getPath());
            if (inBitmap != null) {
                canvas = new Canvas(bitmap);
                canvas.drawBitmap(inBitmap, 0, 0, null);
            } else {
                canvas.drawColor(Color.WHITE);
            }

        } else {//如果都未空,则用背景色作为背景
            canvas.drawColor(tuyaBean.backgroundColor);
        }

        if (tuyaBean != null) {
//            int rotate = 0;
//            //            if (tuyaBeanList.size() > i && tuyaBeanList.get(i).savePaths.size() > 0)
//            //                rotate = tuyaBeanList.get(i).savePaths.get(0).rotate;
//            //            if (rotate != 0) {
//            if (tuyaBean.savePaths.size() > 0 && tuyaBean.savePaths.get(0).rotate != 0) {
//                rotate = tuyaBean.savePaths.get(0).rotate;
//            } else {
//                if (inBitmap != null && mScreenWidth > inBitmap.getWidth()) {
//                    rotate = 270;
//                } else if (inBitmap != null && mScreenWidth < inBitmap.getWidth()) {
//                    rotate = 90;
//                } else if (mScreenWidth > bitmap.getWidth()) {
//                    rotate = 270;
//                } else if (mScreenWidth < bitmap.getWidth()) {
//                    rotate = 90;
//                }
//            }
//
//            if (rotate != 0 && rotate <= 90) {
//                Matrix matrix = new Matrix();
//                matrix.setRotate(rotate);
//                if (mPosition == position) {
//                    // 围绕原地进行旋转
//                    if (inBitmap != null && inBitmap.getWidth() != width) {
//                        Log.i("tuyaview", "save = inBitmap " + rotate);
//
//                        inBitmap = Bitmap.createBitmap(inBitmap, 0, 0, inBitmap.getWidth(), inBitmap.getHeight(), matrix, false);
//                        canvas = new Canvas(bitmap);
//                        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//                        canvas.drawBitmap(inBitmap, 0, 0, null);
//                    } else if (bitmap.getWidth() != width) {
//                        Log.i("tuyaview", "save =  " + rotate);
//                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
//                        canvas = new Canvas(bitmap);
//                    }
//                }
//            }


            //将保存起来的绘制数据画到图片上
            for (DrawPath drawPath : tuyaBean.savePaths) {

                if (drawPath.rotate != 0 ) {
                    Log.i("rotate","rotate 保存 = "+drawPath.rotate);
                    Matrix matrix = new Matrix();
                    float px = bitmap.getWidth() / 2f, py = bitmap.getHeight() / 2f;
                    //　交换中心点的xy坐标
                    float t = px;
                    px = py;
                    py = t;

                    matrix.postRotate(drawPath.rotate, px, py);
                    if (Math.abs(drawPath.rotate) == 90 || Math.abs(drawPath.rotate) == 270) {
                        matrix.postTranslate((py - px), -(py - px));
                    }

                    drawPath.path.transform(matrix);
                    if (drawPath.pathUp != null && drawPath.pathDown != null) {
                        drawPath.pathUp.transform(matrix);
                        drawPath.pathDown.transform(matrix);
                    }
                }

                canvas.drawPath(drawPath.path, drawPath.paint);
                if (drawPath.pathUp != null && drawPath.pathDown != null) {
                    canvas.drawPath(drawPath.pathUp, drawPath.paintUp);
                    canvas.drawPath(drawPath.pathDown, drawPath.paintDown);
                }
                Paint paint = new Paint();
                paint.setColor(tuyaBean.backgroundColor);
                paint.setStrokeWidth(3);
                canvas.drawPoint(0, 0, paint);
            }
        }

        String sdPath = null;
        try

        {
            sdPath = getSavaPath();
        } catch (
                Exception e
                )

        {
            e.printStackTrace();
        }

        String path = sdPath + "/" + content + "第" + (position + 1) + "张.png";
        File file = new File(path);
        FileOutputStream fos = null;
        try

        {
            fos = new FileOutputStream(file);
        } catch (
                Exception e
                )

        {
            e.printStackTrace();
        }

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        return file;
    }

    /**
     * 保存已绘制的图片
     *
     * @param content 标题
     * @return
     * @throws Exception
     */
    public File saveBitmap(String content, String time) throws Exception {
        String path = mTuyaViewMap.get(0).getTuyaView().saveToSDCard(time, content);
        File file = new File(path);
        return file;
    }

    public void clear(int currentItem) {
        mTuyaViewMap.get(currentItem).getTuyaView().redo();
    }

    public void setPaintColor(int currentItem, int white) {
        mTuyaViewMap.get(currentItem).getTuyaView().selectPaintColor(white);
    }

    public void undo(int currentItem) {
        mTuyaViewMap.get(currentItem).getTuyaView().undo();
    }

    /**
     * 点击文字
     *
     * @param currentItem
     */
    public void onClickType(int currentItem) {
        //        mTuyaViewMap.get(currentItem).clickType();
        mNoteFragment.clickType();
    }


}
