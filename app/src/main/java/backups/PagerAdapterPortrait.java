package backups;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import bean.NoteBean;
import constants.NoteBeanConf;
import db.NoteDbDao;
import fragment.NoteFragment;
import service.PostilService;
import utils.ScreenCaptrueUtil;
import view.TuyaViews.TuyaViewPage;

import static constants.NoteBeanConf.nullBitmap;
import static constants.NoteBeanConf.tuyaBeanList;
import static utils.CacheBitmapUtil.createBitmap;
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
public class PagerAdapterPortrait extends FragmentStatePagerAdapter {
    private static final String TAG = "PagerAdapter1";
    private final Context mContext;
    private final int mScreenWidth;
    private final int mScreenHeight;
    private final boolean mIsPostil;
    private final NoteBean mNoteBean;
    private String mZipPath;
    public SparseArray<NoteFragment> mTuyaViewMap = new SparseArray<>();
    private int size;
    public NoteFragment mNoteFragment;
    private File mDesFiles;
    private final NoteDbDao mNoteDbDao;
    private RelativeLayout.LayoutParams mParams;
    private ImageView mEraserView;

    public PagerAdapterPortrait(FragmentManager fm, Context context,
                                int screenWidth, int screenHeight,
                                File desFile, boolean isPostil, NoteBean noteBean) {
        super(fm);
        mContext = context;
        mNoteDbDao = new NoteDbDao(context);
        mScreenWidth = screenWidth;
        mScreenHeight = screenHeight;
        mIsPostil = isPostil;
        mNoteBean = noteBean;
        mDesFiles = desFile;
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
        return PagerAdapterPortrait.POSITION_NONE;
    }

    @Override
    public Fragment getItem(int position) {
        mNoteFragment = new NoteFragment();
        //在原有的图片上做修改
        if (mDesFiles != null && mDesFiles.exists()) {
            TuyaViewPage tuyaView;
            Bitmap bitmap = createBitmap(mDesFiles.getPath());
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

            setEraserListener(tuyaView);

            mNoteFragment.setTuyaView(tuyaView);

            NoteBean noteBean = mNoteDbDao.queryNotePaht(mDesFiles.getPath());
            if (noteBean != null && !TextUtils.isEmpty(noteBean.text)) {
                mNoteFragment.setText(noteBean.text);
            }

            mTuyaViewMap.put(position, mNoteFragment);
            mNoteFragment.setKey(position, mDesFiles);
            return mNoteFragment;
        }

        TuyaViewPage tuyaView = new TuyaViewPage(mContext, NoteBeanConf.createNullBitmap(mScreenWidth, mScreenHeight),
                NoteBeanConf.createSaveBitmap(mScreenWidth, mScreenHeight));
        if (mIsPostil) {
            tuyaView.selectorCanvasColor(Color.TRANSPARENT);
            tuyaView.isPostil(true);
            //如果批注的绘画信息不为空,则显示出来
            if (PostilService.postilList.get(0) != null) {
                Activity activity = (Activity) mContext;
                DisplayMetrics metrics = new DisplayMetrics();
                activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
                tuyaView.setConstants(PostilService.postilList.get(0),metrics.widthPixels,metrics.heightPixels);
            }
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

    public String saveNote(int currentItem, final String time, final String pwd, final String content) throws Exception {

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


        saveZipPath = AddFilesWithAESEncryption(time, content, pwd, mSavePaths,null);
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

                AddFilesWithAESEncryption(time, content, pwd, paths,null);
            }
        }, 500);


        return zipPath;
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
