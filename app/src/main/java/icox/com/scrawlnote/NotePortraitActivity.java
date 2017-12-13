package icox.com.scrawlnote;

import android.view.View;
import android.widget.Toast;

import java.io.File;

import adapter.ScrawlPagerAdapter;
import bean.NoteBean;
import db.NoteDbDao;
import dialog.SaveDialog;
import utils.DateTimeUtil;

/**
 * @author Administrator
 * @version $Rev$
 * @time 2017-4-29 11:18
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public class NotePortraitActivity extends NotePageActivityBase implements View.OnClickListener {
    @Override
    protected void initView() {
        mBoomButtonShare.setVisibility(View.GONE);
        mNoteIbUpPage.setVisibility(View.GONE);
        mNoteLlNextPage.setVisibility(View.GONE);
        mNoteTvPageNumber.setVisibility(View.GONE);
        mBoomLLPageNumber.setVisibility(View.GONE);
    }

    @Override
    protected void saveNote(String content, String pwd) {
        String time = DateTimeUtil.getCurrentTime(System.currentTimeMillis());
        File file = null;
        try {
            file = mPagerAdapter.saveBitmap(mBookLocation, time);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(NotePortraitActivity.this, "请检查你的存储设备是否可用！", Toast.LENGTH_SHORT).show();
            return;
        }

        if (file == null && !file.exists()) {
            Toast.makeText(NotePortraitActivity.this, file.getPath(), Toast.LENGTH_SHORT).show();
            return;
        }

        NoteBean noteBean = mNoteDbDao.queryNote(mBookLocation);
        if (noteBean == null) {
            mNoteDbDao.saveNote(mBookLocation, time, file.getPath(),
                    mPagerAdapter.mTuyaViewMap.get(0).mEditText.getText().toString().trim()
                    , null, null);
        } else {
            mNoteDbDao.updateNote(mBookLocation, time, file.getPath(),
                    mPagerAdapter.mTuyaViewMap.get(0).mEditText.getText().toString().trim()
            );
        }
        Toast.makeText(NotePortraitActivity.this, "保存完成！", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void initData() {
        mNoteDbDao = new NoteDbDao(this);
        if (mBookLocation != null) {
            mNoteBean = mNoteDbDao.queryNote(mBookLocation);
            if (mNoteBean != null) {
                mDesFile = new File(mNoteBean.path);
            }
        } else if (mFilePath != null) {
            mNoteBean = mNoteDbDao.queryNotePaht(mFilePath);
        } else {
            mNoteBean = null;
        }
    }

    @Override
    protected ScrawlPagerAdapter initPagerAdapter() {
        return new ScrawlPagerAdapter(getSupportFragmentManager(), this,
                mScreenWidth, mScreenHeight, mDesFile, false, mNoteBean);
    }

    @Override
    public void initSaveDialogView(SaveDialog saveDialog) {
        saveDialog.hideEdttext();
        super.initSaveDialogView(saveDialog);
    }
}
