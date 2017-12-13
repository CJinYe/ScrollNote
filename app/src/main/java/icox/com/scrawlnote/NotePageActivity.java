package icox.com.scrawlnote;

import android.view.View;
import android.widget.Toast;

import adapter.ScrawlPagerAdapter;
import db.NoteDbDao;
import utils.DateTimeUtil;

/**
 * @author Administrator
 * @version $Rev$
 * @time 2017-3-30 9:30
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public class NotePageActivity extends NotePageActivityBase implements View.OnClickListener {
    @Override
    protected void initView() {
    }

    @Override
    protected void saveNote(String content, String pwd) {
        String time = DateTimeUtil.getCurrentTime(System.currentTimeMillis());

        String path = null;
        try {
            mPagerAdapter.setWidthHeight(getScreenWidth(),getScreenHeight());
            path = mPagerAdapter.saveNote(time, content, pwd, null);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(NotePageActivity.this, "请检查你的存储设备是否可用！" + e, Toast.LENGTH_LONG).show();
            return;
        }
        if (path.contains("保存出错了,错误原因")) {
            Toast.makeText(NotePageActivity.this, path, Toast.LENGTH_SHORT).show();
            return;
        }

        mSaveDialog.deleteFile();
        Toast.makeText(NotePageActivity.this, "保存完成！", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void initData() {
        mNoteDbDao = new NoteDbDao(this);
        if (mBookLocation != null) {
            mNoteBean = mNoteDbDao.queryNote(mBookLocation);
        } else if (mFilePath != null) {
            mNoteBean = mNoteDbDao.queryNotePaht(mFilePath);
        } else {
            mNoteBean = null;
        }
    }

    @Override
    protected ScrawlPagerAdapter initPagerAdapter() {
        return new ScrawlPagerAdapter(getSupportFragmentManager(), this,
                mScreenWidth, mScreenHeight, mDesFile, false);
    }
}
