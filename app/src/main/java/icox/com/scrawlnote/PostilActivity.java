package icox.com.scrawlnote;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import adapter.PagerAdapter;
import bean.NoteBean;
import butterknife.ButterKnife;
import butterknife.InjectView;
import constants.Constants;
import constants.NoteBeanConf;
import dialog.SaveDialog;
import dialog.SharePostilDialog;
import okhttp3.Call;
import okhttp3.Response;
import service.PostilService;
import utils.CacheBitmapUtil;
import utils.DateTimeUtil;
import utils.OkHttpUtil;
import utils.QRCodeUtil;
import utils.SDcardUtil;
import utils.ScreenCaptrueUtil;
import utils.SpUtils;
import view.NoPreloadViewPager;

import static utils.Zip4jUtil.AddFilesWithAESEncryption;

/**
 * @author 陈锦业
 * @version $Rev$
 * @time 2017-5-24 9:31
 * @des 批注的界面
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public class PostilActivity extends NotePageActivitySupperBase implements View.OnClickListener {
    private static final int REQUEST_MEDIA_PROJECTION = 1;
    private static final String TAG = "PostilActivity";
    @InjectView(R.id.note_write_tablet_view)
    NoPreloadViewPager mNoteViewPager;
    @InjectView(R.id.note_write_iv_eraser)
    public ImageView mNoteWriteIvEraser;
    @InjectView(R.id.note_ib_up_page)
    LinearLayout mNoteIbUpPage;
    @InjectView(R.id.note_tv_page_number)
    TextView mNoteTvPageNumber;
    @InjectView(R.id.note_ll_next_page)
    LinearLayout mNoteLlNextPage;
    @InjectView(R.id.note_ib_next_page)
    ImageView mNoteIbNextPage;
    @InjectView(R.id.note_write_but_ok)
    LinearLayout mNoteWriteButOk;
    @InjectView(R.id.note_write_but_clear)
    LinearLayout mNoteWriteButClear;
    @InjectView(R.id.note_write_but_repeal)
    LinearLayout mNoteWriteButRepeal;
    @InjectView(R.id.note_write_but_size)
    LinearLayout mNoteWriteButSize;
    @InjectView(R.id.note_write_but_eraser)
    LinearLayout mNoteWriteButEraser;
    @InjectView(R.id.note_write_but_type)
    LinearLayout mNoteWriteButType;
    @InjectView(R.id.note_write_but_go_back)
    LinearLayout mNoteWriteButGoBack;
    @InjectView(R.id.boom_buttons_menus)
    LinearLayout mBoomButtonsMenus;
    @InjectView(R.id.activity_main)
    RelativeLayout mActivityMain;
    @InjectView(R.id.note_but_hide_button)
    ImageView mNoteButHideButton;
    @InjectView(R.id.boom_button_menus)
    LinearLayout mBoomButtonMenus;
    @InjectView(R.id.note_write_but_share)
    LinearLayout mBoomButtonShare;
    @InjectView(R.id.note_write_but_show)
    ImageButton mNoteWriteButShow;
    @InjectView(R.id.note_write_but_hide)
    LinearLayout mNoteWriteButHide;
    @InjectView(R.id.note_ll_page_number)
    LinearLayout mNoteLlPageNumber;
    private String mFilePath;
    private File mDesFile;
    private PagerAdapter mPagerAdapter;
    private RelativeLayout.LayoutParams mParams;
    private boolean mIsHideMenus = false;
    private MediaProjectionManager mMediaProjectionManager;
    private Intent intent;
    private int result;
    private SharePostilDialog mShareDialog;
    private File mShareFile;
    public static boolean isCreate = false;
    private SpUtils mSpUtils;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postil);
        ButterKnife.inject(this);
        EventBus.getDefault().register(this);
        mSpUtils = new SpUtils(this);

        mFilePath = getIntent().getStringExtra("FilePath");
        try {
            mFilePath = SDcardUtil.getPicturesCachePath();
            if (mFilePath != null) {
                mDesFile = new File(mFilePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        isCreate = true;
        initView();

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mMediaProjectionManager = (MediaProjectionManager) getApplication().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            }
            startIntent();
        } catch (Exception e) {

        }

    }

    private void startIntent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (intent != null && result != 0) {
                ((MyApplocation) getApplication()).setResult(result);
                ((MyApplocation) getApplication()).setIntent(intent);
            } else {
                startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
                ((MyApplocation) getApplication()).setMediaProjectionManager(mMediaProjectionManager);
            }

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MEDIA_PROJECTION) {

            try {
                if (resultCode != Activity.RESULT_OK) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
                        ((MyApplocation) getApplication()).setMediaProjectionManager(mMediaProjectionManager);
                    }
                    return;
                } else if (data != null && resultCode != 0) {
                    //PostilService.mResultCode = resultCode;
                    //PostilService.mResultData = data;
                    ((MyApplocation) getApplication()).setResult(resultCode);
                    ((MyApplocation) getApplication()).setIntent(data);
                    Intent intent = new Intent(getApplicationContext(), PostilService.class);
                    startService(intent);
                }
            } catch (Exception e) {
                Toast.makeText(this, "您的设备截屏不可用!", Toast.LENGTH_SHORT).show();
            }

            initWindow();
        }
    }

    private void initView() {

        ViewGroup.MarginLayoutParams marginLayoutParams =
                new ViewGroup.MarginLayoutParams(mNoteWriteIvEraser.getLayoutParams());
        mParams = new RelativeLayout.LayoutParams(marginLayoutParams);

        int screenWidth = getScreenWidth();
        int screenHeight = getScreenHeight();

        mPagerAdapter = new PagerAdapter(getSupportFragmentManager(), this,
                screenWidth, screenHeight, mDesFile, true);
        //        mPagerAdapter = new ViewPagerAdapter(mFilePath, this,
        //                mNoteBean, screenWidth, screenHeight);
        mNoteViewPager.setAdapter(mPagerAdapter);

        mNoteWriteButRepeal.setOnClickListener(this);
        mNoteWriteButClear.setOnClickListener(this);
        mNoteWriteButType.setOnClickListener(this);
        mNoteWriteButOk.setOnClickListener(this);
        mNoteWriteButSize.setOnClickListener(this);
        mNoteIbUpPage.setOnClickListener(this);
        mNoteWriteButGoBack.setOnClickListener(this);
        mNoteWriteButEraser.setOnClickListener(this);
        mNoteLlNextPage.setOnClickListener(this);
        mNoteButHideButton.setOnClickListener(this);
        mBoomButtonShare.setOnClickListener(this);
        mNoteWriteButHide.setOnClickListener(this);
        mNoteWriteButShow.setOnClickListener(this);
        mNoteWriteButHide.setVisibility(View.VISIBLE);

        if (mDesFile.listFiles().length > 0)
            mNoteViewPager.setCurrentItem(mDesFile.list().length - 1);

        if (mNoteViewPager.getCurrentItem() == mNoteViewPager.getAdapter().getCount() - 1) {
            mNoteIbNextPage.setImageResource(R.drawable.add_page_button);
        } else {
            mNoteIbNextPage.setImageResource(R.drawable.next);
        }


        viewPagerScrollListener(mNoteViewPager, mNoteIbNextPage);

        mPagerAdapter.setEraserView(mNoteWriteIvEraser);

        mNoteTvPageNumber.setText((mNoteViewPager.getCurrentItem() + 1) + "/" + mPagerAdapter.getCount());

        //        mNoteWriteButType.setVisibility(View.GONE);
        //        mNoteLlNextPage.setVisibility(View.GONE);
        //        mNoteLlPageNumber.setVisibility(View.GONE);
        //        mNoteIbUpPage.setVisibility(View.GONE);
        //        mBoomButtonsMenus.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.note_write_but_size://大小
                changerTuyaViewConf(mPagerAdapter.mNoteFragment.getTuyaView()
                        , mNoteWriteIvEraser, true);
                break;
            case R.id.note_write_but_ok://提交
                clickSave();
                break;
            case R.id.note_write_but_clear://清屏
                clickClear(mPagerAdapter, mNoteViewPager);
                break;
            case R.id.note_write_but_repeal://撤销
                mPagerAdapter.undo(mNoteViewPager.getCurrentItem());
                break;
            case R.id.note_write_but_type://文字
                mPagerAdapter.onClickType(mNoteViewPager.getCurrentItem());
                break;
            case R.id.note_write_but_go_back://退出
                goBack();
                break;
            case R.id.note_write_but_eraser://橡皮擦
                clickEraser(mPagerAdapter.mNoteFragment.getTuyaView(), mNoteWriteIvEraser);
                break;
            case R.id.note_ib_up_page:
                clickUpPage(mNoteIbNextPage, mNoteTvPageNumber, mNoteViewPager, mPagerAdapter, mNoteWriteIvEraser);
                break;
            case R.id.note_ll_next_page:
                clickNextPage(mNoteIbNextPage, mNoteTvPageNumber, mNoteViewPager, mPagerAdapter, mNoteWriteIvEraser);
                break;
            case R.id.note_but_hide_button:
                mIsHideMenus = !mIsHideMenus;
                if (mIsHideMenus) {
                    mBoomButtonMenus.setVisibility(View.GONE);
                } else {
                    mBoomButtonMenus.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.note_write_but_share:
                showShareDialog();
                break;
            case R.id.note_write_but_hide:
                mBoomButtonMenus.setVisibility(View.GONE);
                mNoteWriteButShow.setVisibility(View.VISIBLE);
                break;
            case R.id.note_write_but_show:
                mBoomButtonMenus.setVisibility(View.VISIBLE);
                mNoteWriteButShow.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    private void showShareDialog() {

        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                //                mMediaProjectionManager = (MediaProjectionManager) getApplication().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                Toast.makeText(PostilActivity.this, "请检查你的截屏设备是否可用！", Toast.LENGTH_LONG).show();
                return;
            }
            //            startIntent();
        } catch (Exception e) {
        }

        mShareDialog = new SharePostilDialog(PostilActivity.this) {
            @Override
            public String saveFile(final String title, final String pwd, final String name) {
                mBoomButtonsMenus.setVisibility(View.GONE);

                long currentTime = System.currentTimeMillis();
                final String time = DateTimeUtil.getCurrentTime(currentTime);

                ScreenCaptrueUtil screenCaptrueUtil = new ScreenCaptrueUtil(PostilActivity.this);
                File file = null;
                try {
                    file = screenCaptrueUtil.screenCapture();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(PostilActivity.this, "请检查你的存储设备是否可用！" + e, Toast.LENGTH_LONG).show();
                    return "";
                }

                final File finalFile = file;
                final long finalCurrentTime = currentTime;

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        final ArrayList<File> paths = new ArrayList<>();

                        if (finalFile != null || mDesFile.listFiles().length > 0) {

                            if (finalFile != null && finalFile.exists())
                                paths.add(finalFile);

                            for (File f : mPagerAdapter.saveNote(title)) {
                                if (f.exists())
                                    paths.add(f);
                            }
                            String path = AddFilesWithAESEncryption(time, title, pwd, paths, name);
                            mShareFile = new File(path);
                            if (!mShareFile.exists()) {
                                Toast.makeText(PostilActivity.this, "文件不存在 " + mShareFile.getPath(), Toast.LENGTH_LONG).show();
                                return;
                            }

                            Map<String, String> paserm = new HashMap<String, String>();
                            paserm.put("name", OkHttpUtil.toUTF8(title + "_" + finalCurrentTime));
                            paserm.put("pwd", OkHttpUtil.toUTF8(pwd));
                            paserm.put("api", "set");
                            paserm.put("compere", OkHttpUtil.toUTF8(name));
                            paserm.put("time", OkHttpUtil.toUTF8(time.substring(0, time.indexOf("秒") + 1)));
                            paserm.put("MeetingAddr", OkHttpUtil.toUTF8(
                                    mSpUtils.getString(Constants.MEETING_ADDR, Constants.MEETING_ADDR_NORMAL)));

                            mShareDialog.mTvTitle.setText(pwd);
                            OkHttpUtil.uploadFile(mShareFile, Constants.SHARE_URL, paserm, null, new MyStringCallBack());

                            for (File f : mDesFile.listFiles()) {
                                if (f.exists())
                                    f.delete();
                            }
                        }
                        mShareDialog.show();
                        mBoomButtonsMenus.setVisibility(View.VISIBLE);
                    }
                }, 550);


                if (file == null) {
                    return "";
                }
                return file.getPath();
            }

            @Override
            public void hidePopup() {
                finish();
            }
        };
        mShareDialog.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
        mShareDialog.show();
    }

    class MyStringCallBack extends StringCallback {
        @Override
        public void onError(Call call, Exception e, int id) {
            mShareDialog.SHARE_STATE = mShareDialog.SHARE_ERROR;
            mShareDialog.mButtonShare.setImageResource(R.drawable.dialog_qrcode_btn_error);
            Toast.makeText(PostilActivity.this, "上传 e = " + e, Toast.LENGTH_LONG).show();
            mShareDialog.isClickShare = false;
            if (mShareFile != null) {
                if (mShareFile.exists())
                    mShareFile.delete();
            }
            Log.e("MainActity11", "上传出错 e = " + e);
        }

        @Override
        public void onResponse(String response, int id) {
            Log.i("MainActity11", "上传中 response = " + response);
            mShareDialog.SHARE_STATE = mShareDialog.SHARE_ERROR;
            mShareDialog.mButtonShare.setImageResource(R.drawable.dialog_qrcode_btn_error);
            if (response.contains("error||")) {
                mShareDialog.isClickShare = false;
                if (mFilePath != null) {
                    File file = new File(mFilePath);
                    if (file.exists())
                        file.delete();
                }
                Toast.makeText(PostilActivity.this, "参数错误！", Toast.LENGTH_LONG).show();
            } else {
                mShareDialog.SHARE_STATE = mShareDialog.SHARE_SUCCEED;
                mShareDialog.mButtonShare.setImageResource(R.drawable.selector_dialog_share_btn_qr_code_succeed);
                if (TextUtils.isEmpty(response)) {
                    response = "null";
                }
                Bitmap QRCode = QRCodeUtil.createQRCode(response, 200, 200);
                mShareDialog.mIvQRCode.setImageBitmap(QRCode);
                mShareDialog.mIvQRCode.setVisibility(View.VISIBLE);
                mShareDialog.mTvHint.setVisibility(View.VISIBLE);
                mShareDialog.mTvTitle.setVisibility(View.VISIBLE);
                mShareDialog.mEdtTitle.setVisibility(View.GONE);
                mShareDialog.EdtPwd.setVisibility(View.GONE);
                mShareDialog.mEdtName.setVisibility(View.GONE);
            }
        }

        @Override
        public void inProgress(float progress, long total, int id) {
        }

        @Override
        public boolean validateReponse(Response response, int id) {
            return super.validateReponse(response, id);
        }
    }

    private void clickSave() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mMediaProjectionManager = (MediaProjectionManager) getApplication().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            }
            startIntent();
        } catch (Exception e) {
            Toast.makeText(PostilActivity.this, "请检查你的截屏设备是否可用！" + e, Toast.LENGTH_LONG).show();
            return;
        }

        final SaveDialog saveDialog = new SaveDialog(this, true) {

            private String mSavePath;

            @Override
            public void sure(final String content, final String pwd) {

                mBoomButtonsMenus.setVisibility(View.GONE);
                final String time = DateTimeUtil.getCurrentTime();

                mSavePath = null;
                try {

                    ScreenCaptrueUtil screenCaptrueUtil = new ScreenCaptrueUtil(PostilActivity.this);
                    final File file = screenCaptrueUtil.screenCapture();
                    //500毫秒之后再去加密文件,因为截屏需要一点时间
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            final ArrayList<File> paths = new ArrayList<>();

                            if (file != null || mDesFile.listFiles().length > 0) {

                                if (file != null && file.exists())
                                    paths.add(file);

                                for (File f : mPagerAdapter.saveNote(content)) {
                                    if (f.exists())
                                        paths.add(f);
                                }
                            }

                            mSavePath = AddFilesWithAESEncryption(time, content, pwd, paths, null);

                            for (File f : mDesFile.listFiles()) {
                                if (f.exists())
                                    f.delete();
                            }
                        }
                    }, 500);

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(PostilActivity.this, "请检查你的存储设备是否可用！" + e, Toast.LENGTH_LONG).show();
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mSavePath != null && new File(mSavePath).exists()) {
                            Toast.makeText(PostilActivity.this, "保存完成！", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                        mBoomButtonsMenus.setVisibility(View.VISIBLE);
                    }
                }, 550);

            }
        };


        saveDialog.show();
        saveDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                initWindow();
            }
        });
    }

    //批注图标点击,销毁隐藏界面
    @Subscribe
    public void onMessage(NoteBean noteBean) {
        if (isCreate) {
            mBoomButtonsMenus.setVisibility(View.GONE);
            if (mDesFile.listFiles().length < 1 || mNoteViewPager.getCurrentItem() > (mDesFile.listFiles().length - 1)) {
                try {

                    ScreenCaptrueUtil screenCaptrueUtil = new ScreenCaptrueUtil(PostilActivity.this,
                            SDcardUtil.getPicturesCachePath());

                    final File file = screenCaptrueUtil.screenCapture();
                    //500毫秒之后再去加密文件,因为截屏需要一点时间
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                            mBoomButtonsMenus.setVisibility(View.VISIBLE);
                        }
                    }, 500);

                } catch (Exception e) {
                    e.printStackTrace();
                    finish();
                    mBoomButtonsMenus.setVisibility(View.VISIBLE);
                }
            } else {
                finish();
                mBoomButtonsMenus.setVisibility(View.VISIBLE);
            }

            //                                    finish();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        isCreate = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isCreate = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NoteBeanConf.tuyaBeanList.clear();
        if (mDesFile != null) {
            CacheBitmapUtil.destoryCacheBitmap(mDesFile.listFiles());
        }
        //        SDcardUtil.deleteDirWihtFile(mDesFile);

        EventBus.getDefault().unregister(this);
        isCreate = false;

    }
}
