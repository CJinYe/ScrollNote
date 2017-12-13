package backups;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.text.TextUtils;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;

import bean.NoteBean;
import fragment.NoteFragment;
import view.TuyaViews.TuyaView;

/**
 * @author Administrator
 * @version $Rev$
 * @time 2017-4-27 10:00
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public class PagerAdapterCopy extends FragmentPagerAdapter {
    private static final String TAG = "PagerAdapter1";
    private final Context mContext;
    private final int mScreenWidth;
    private final int mScreenHeight;
    private NoteBean mNoteBean;
    public SparseArray<NoteFragment> mTuyaViewMap = new SparseArray<>();
    private List<NoteFragment> mFragments = new ArrayList<>();
    private List<TuyaView> mTuyaViews = new ArrayList<>();
    private Bitmap mBitmap = null;

    public PagerAdapterCopy(FragmentManager fm, NoteBean noteBean, Context context, int screenWidth, int screenHeight) {
        super(fm);
        mNoteBean = noteBean;
        mContext = context;
        mScreenWidth = screenWidth;
        mScreenHeight = screenHeight;

    }

    @Override
    public Fragment getItem(int position) {
        NoteFragment noteFragment = null;
        switch (position) {
            case 0:
                noteFragment = new NoteFragment();
                if (mNoteBean != null && !TextUtils.isEmpty(mNoteBean.path)) {
                    Bitmap bitmap = BitmapFactory.decodeFile(mNoteBean.path);
                    TuyaView tuyaView = new TuyaView(mContext, bitmap, mScreenWidth, mScreenHeight);
//                    noteFragment.setTuyaView(tuyaView);
                    mTuyaViews.add(tuyaView);
                } else {
                    TuyaView tuyaView = new TuyaView(mContext, mScreenWidth, mScreenHeight);
//                    noteFragment.setTuyaView(tuyaView);
                    mTuyaViews.add(tuyaView);
                }
                mTuyaViewMap.put(position, noteFragment);
                break;
            case 1:
                noteFragment = new NoteFragment();
                mTuyaViewMap.put(position, noteFragment);
                break;
            case 2:
                noteFragment = new NoteFragment();
                TuyaViewThree tuyaView2 = new TuyaViewThree(mContext, mScreenWidth, mScreenHeight);
                noteFragment.setTuyaViewThree(tuyaView2);
                mTuyaViewMap.put(position, noteFragment);
                break;
            case 3:
                noteFragment = new NoteFragment();
                TuyaView tuyaView3 = new TuyaView(mContext, mScreenWidth, mScreenHeight);
//                noteFragment.setTuyaView(tuyaView3);
                mTuyaViewMap.put(position, noteFragment);
                break;

            default:
                break;
        }

        return noteFragment;
    }

    @Override
    public int getCount() {
        return 4;
    }

    public void setPaintSize(int currentItem, int paintSize) {
        switch (currentItem) {
            case 0:
                mTuyaViewMap.get(currentItem).getTuyaView().selectPaintSize(paintSize);
                break;
            case 1:
                break;
            case 2:
                mTuyaViewMap.get(currentItem).mTuyaViewThree.selectPaintSize(paintSize);
                break;
            default:
                break;
        }
    }

    public String saveNote(int currentItem, String time, String content) {

        String path = null;
        switch (currentItem) {
            case 0:
//             path = mTuyaViewMap.get(currentItem).mTuyaView.saveToSDCard(time, content);
//             path = mTuyaViewMap.get(currentItem).getTuyaView().saveToSDCard(time, content);
                break;
            case 1:
                break;
            case 2:
             path = mTuyaViewMap.get(currentItem).mTuyaViewThree.saveToSDCard(time, content);
                break;
            case 3:
//             path = mTuyaViewMap.get(currentItem).getTuyaView().saveToSDCard(time, content);
                break;
            default:
                break;
        }
        return path;
    }

    public void clear(int currentItem) {
        switch (currentItem) {
            case 0:
                mTuyaViewMap.get(currentItem).getTuyaView().redo();
                break;
            case 1:
                break;
            case 2:
                mTuyaViewMap.get(currentItem).mTuyaViewThree.redo();
                break;
            default:
                break;
        }
    }

    public void setPaintColor(int currentItem, int white) {
        switch (currentItem) {
            case 0:
                mTuyaViewMap.get(currentItem).getTuyaView().selectPaintColor(white);
                break;
            case 1:
                break;
            case 2:
                mTuyaViewMap.get(currentItem).mTuyaViewThree.selectPaintColor(white);
                break;
            default:
                break;
        }
    }
}
