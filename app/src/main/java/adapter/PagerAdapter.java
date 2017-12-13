package adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import bean.NoteBean;
import bean.TuyaViewBean;
import constants.DrawPath;
import constants.NoteBeanConf;
import db.NoteDbDao;
import fragment.NoteFragment;
import service.PostilService;
import utils.CacheBitmapUtil;
import utils.ScreenCaptrueUtil;
import view.TuyaViews.TuyaViewPage;

import static constants.NoteBeanConf.nullBitmap;
import static constants.NoteBeanConf.tuyaBeanList;
import static utils.CacheBitmapUtil.createBitmap;
import static utils.SDcardUtil.getSavaPath;
import static utils.Zip4jUtil.AddFilesWithAESEncryption;

/**
 * @author Administrator
 * @version $Rev$
 * @time 2017-4-27 10:00
 * @des ${批注用的adapter}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public class PagerAdapter extends FragmentStatePagerAdapter {
    private static final String TAG = "PagerAdapter1";
    private final Context mContext;
    private final int mScreenWidth;
    private final int mScreenHeight;
    private final boolean mIsPostil;
    private String mZipPath;
    public SparseArray<NoteFragment> mTuyaViewMap = new SparseArray<>();
    private int size;
    public NoteFragment mNoteFragment;
    private File[] mDesFiles;
    private final NoteDbDao mNoteDbDao;
    private RelativeLayout.LayoutParams mParams;
    private ImageView mEraserView;

    public PagerAdapter(FragmentManager fm, Context context,
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
            mDesFiles = desFile.listFiles();
            size = mDesFiles.length;
            if (mDesFiles.length < 1)
                size = 1;
        } else {
            size = 1;
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
        mTuyaViewMap.remove(position);
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }

    @Override
    public Fragment getItem(int position) {
        mNoteFragment = new NoteFragment();
        //在原有的图片上做修改
        if (mDesFiles != null && position < mDesFiles.length) {
            TuyaViewPage tuyaView;
            Bitmap bitmap = createBitmap(mDesFiles[position].getPath());
            if (bitmap != null && !bitmap.isRecycled()) {
                tuyaView = new TuyaViewPage(mContext,
                        NoteBeanConf.createNullBitmap(mScreenWidth, mScreenHeight),
                        NoteBeanConf.createSaveBitmap(mScreenWidth, mScreenHeight),
                        bitmap);
                //设置背景颜色,跟橡皮擦有关联
                //                tuyaView.backgroundColor = getBitmapBackground(bitmap);
                tuyaView.backgroundColor = Color.WHITE;
            } else {
                tuyaView = new TuyaViewPage(mContext, NoteBeanConf.createNullBitmap(mScreenWidth, mScreenHeight),
                        NoteBeanConf.createSaveBitmap(mScreenWidth, mScreenHeight));
            }

            tuyaView.isPostil(mIsPostil);

            setEraserListener(tuyaView);

            mNoteFragment.setTuyaView(tuyaView);

            if (mZipPath != null) {
                NoteBean noteBean = mNoteDbDao.queryNoteZipPaht(mZipPath + ".zip", mDesFiles[position].getName());
                if (noteBean != null && !TextUtils.isEmpty(noteBean.text)) {
                    mNoteFragment.setText(noteBean.text);
                }
            }

            mTuyaViewMap.put(position, mNoteFragment);
            mNoteFragment.setKey(position, mDesFiles[position]);
            return mNoteFragment;
        }

        TuyaViewPage tuyaView = new TuyaViewPage(mContext, NoteBeanConf.createNullBitmap(mScreenWidth, mScreenHeight),
                NoteBeanConf.createSaveBitmap(mScreenWidth, mScreenHeight));
        if (mIsPostil) {
            tuyaView.selectorCanvasColor(Color.TRANSPARENT);
            tuyaView.isPostil(true);
            //如果批注的绘画信息不为空,则显示出来
            if (PostilService.postilList.get(0) != null) {
//                tuyaView.setConstants(PostilService.postilList.get(0));
            }
            mNoteFragment.setPostil(true);
        }

        setEraserListener(tuyaView);
        mNoteFragment.setTuyaView(tuyaView);
        mTuyaViewMap.put(position, mNoteFragment);
        mNoteFragment.setKey(position, null);
        return mNoteFragment;

    }

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

    public String saveNote(int currentItem, final String time, final String content, final String pwd) {

        mNoteFragment.saveBitmap();

        mSavePaths = new ArrayList<>();

        //        if (mDesFiles != null && mDesFiles.length > 0) {
        //            for (int i = 0; i < mDesFiles.length; i++) {
        //                File file = saveBitmap(content, i);
        //                mSavePaths.add(file);
        //            }
        //        }

        //        for (int i = 0; i < tuyaBeanList.size(); i++) {
        //            File file = saveBitmap(content, i);
        //            mSavePaths.add(file);
        //        }


        for (int i = 0; i < size; i++) {
            File file = saveBitmap(content, i);
            mSavePaths.add(file);
        }

        saveZipPath = AddFilesWithAESEncryption(time, content, pwd, mSavePaths, null);

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

    public List<File> saveNote(String content){
        List<File> list = new ArrayList<>();
        for (int i = 0; i < mDesFiles.length; i++) {
            File file = saveBitmap(content, i);
            list.add(file);
        }
        return list;
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

    /**
     * 保存已绘制的图片
     *
     * @param content 标题
     * @param i       索引
     * @return
     * @throws Exception
     */
    public File saveBitmap(String content, int i) {

        TuyaViewBean tuyaBean = null;

        if (tuyaBeanList.size() > i) {
            tuyaBean = NoteBeanConf.tuyaBeanList.get(i);
        }

        Bitmap bitmap = NoteBeanConf.createSaveBitmap(mScreenWidth, mScreenHeight);
        Canvas canvas = new Canvas(bitmap);

        //保存的绘制信息不为空,是在图片上面做操作的
        if (null != tuyaBean && tuyaBean.bitmapPath != null) {
            Bitmap bitmap1 = CacheBitmapUtil.createBitmap(tuyaBean.bitmapPath.getPath());
            //如果背景颜色没有变化,就用之前的bitmap做背景
            if (bitmap1 != null) {
                //没有画布颜色
                if (!tuyaBean.isChangerBackground) {
                    canvas = new Canvas(bitmap);
                    canvas.drawBitmap(bitmap1, 0, 0, null);

                } else {
                    canvas.drawColor(tuyaBean.backgroundColor);
                }
            } else {
                canvas.drawColor(tuyaBean.backgroundColor);
            }
        } else if (mDesFiles != null && mDesFiles.length > i) {
            Bitmap bitmap1 = CacheBitmapUtil.createBitmap(mDesFiles[i].getPath());
            if (bitmap1 != null) {
                canvas = new Canvas(bitmap);
                canvas.drawBitmap(bitmap1, 0, 0, null);
            } else {
                canvas.drawColor(Color.WHITE);
            }

        } else {//如果都未空,则用背景色作为背景
            canvas.drawColor(tuyaBean.backgroundColor);
        }

        if (tuyaBean != null) {
            //将保存起来的绘制数据画到图片上
            for (DrawPath drawPath : tuyaBean.savePaths) {
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

        //        String sdPath = Environment.getExternalStorageDirectory() + "/ScrawlNote";
        //        File f = new File(sdPath);
        //        if (!f.exists()) {
        //            f.mkdirs();
        //        }

        String sdPath = null;
        try {
            sdPath = getSavaPath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String path = sdPath + "/" + content + "第" + (i + 1) + "张.png";
        File file = new File(path);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
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
