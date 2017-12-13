package backups;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import bean.NoteBean;
import bean.TuyaViewBean;
import constants.DrawPath;
import constants.NoteBeanConf;
import db.NoteDbDao;
import fragment.NoteFragment;
import utils.CacheBitmapUtil;
import view.TuyaViews.TuyaViewPage;

import static constants.NoteBeanConf.tuyaBeanList;
import static utils.CacheBitmapUtil.getBitmapBackground;
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
public class PostilPagerAdapter extends FragmentStatePagerAdapter {
    private static final String TAG = "PagerAdapter1";
    private final Context mContext;
    private final int mScreenWidth;
    private final int mScreenHeight;
    private String mZipPath;
    public SparseArray<NoteFragment> mTuyaViewMap = new SparseArray<>();
    private int size;
    public NoteFragment mNoteFragment;
    private File[] mDesFiles;
    private final NoteDbDao mNoteDbDao;

    public PostilPagerAdapter(FragmentManager fm, Context context,
                              int screenWidth, int screenHeight) {
        super(fm);
        mContext = context;
        mNoteDbDao = new NoteDbDao(context);
        mScreenWidth = screenWidth;
        mScreenHeight = screenHeight;

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
        return PostilPagerAdapter.POSITION_NONE;
    }

    @Override
    public Fragment getItem(int position) {
        mNoteFragment = new NoteFragment();
        TuyaViewPage tuyaView = new TuyaViewPage(mContext, NoteBeanConf.createNullBitmap(mScreenWidth, mScreenHeight),
                NoteBeanConf.createSaveBitmap(mScreenWidth, mScreenHeight));
        tuyaView.selectorCanvasColor(Color.TRANSPARENT);
        mNoteFragment.setTuyaView(tuyaView);
        mTuyaViewMap.put(position, mNoteFragment);
        mNoteFragment.setKey(position, null);
        return mNoteFragment;

    }

    @Override
    public int getCount() {
        return size;
    }


    public void setPaintSize(int currentItem, int paintSize) {
        mTuyaViewMap.get(currentItem).getTuyaView().selectPaintSize(paintSize);
    }

    public String saveNote(final String time, final String content, final String pwd) throws Exception {

        mNoteFragment.saveBitmap();

        final ArrayList<File> paths = new ArrayList<>();

        if (mDesFiles != null && mDesFiles.length > 0) {
            for (int i = 0; i < mDesFiles.length; i++) {
                File file = saveBitmap(content, i);
                paths.add(file);
            }
        } else {
            for (int i = 0; i < tuyaBeanList.size(); i++) {
                File file = saveBitmap(content, i);
                paths.add(file);
            }
        }

        String zipPath = AddFilesWithAESEncryption(time, content, pwd, paths,null);

        if (!zipPath.contains("保存出错了,错误原因")) {
            for (int i = 0; i < paths.size(); i++) {
                paths.get(i).delete();
                NoteBean noteBean = mNoteDbDao.queryNoteZipPaht(zipPath, paths.get(i).getPath());
                if (noteBean != null) {
                    mNoteDbDao.updateZipNote(null, time, paths.get(i).getPath(),
                            tuyaBeanList.get(i).text, zipPath, pwd);
                } else {
                    mNoteDbDao.saveNote(null, time, paths.get(i).getName(),
                            tuyaBeanList.get(i).text, zipPath, pwd);
                }
            }
        }


        return zipPath;
    }

    private File saveBitmap(String content, int i) throws Exception {

        TuyaViewBean tuyaBean = NoteBeanConf.tuyaBeanList.get(i);
        Bitmap bitmap = NoteBeanConf.createSaveBitmap(mScreenWidth, mScreenHeight);
        Canvas canvas = new Canvas(bitmap);
        if (tuyaBean != null && tuyaBean.bitmapPath != null) {
            Bitmap bitmap1 = CacheBitmapUtil.createBitmap(tuyaBean.bitmapPath.getPath());
            //如果背景颜色没有变化,就用之前的bitmap做背景
            if (bitmap1 != null) {
                if (getBitmapBackground(bitmap1) == tuyaBean.backgroundColor) {
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
                canvas.drawBitmap(bitmap1, 0, 0, null);
            } else {
                canvas.drawColor(Color.WHITE);
            }

        } else {
            canvas.drawColor(tuyaBean.backgroundColor);
        }

        if (tuyaBean != null) {
            for (DrawPath drawPath : tuyaBean.savePaths) {
                canvas.drawPath(drawPath.path, drawPath.paint);
                Paint paint = new Paint();
                paint.setColor(tuyaBean.backgroundColor);
                paint.setStrokeWidth(3);
                canvas.drawPoint(0, 0, paint);
            }
        }


        String sdPath = getSavaPath();
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
        mNoteFragment.clickType();
    }


}
