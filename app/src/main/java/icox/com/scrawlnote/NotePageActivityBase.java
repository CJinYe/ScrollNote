package icox.com.scrawlnote;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import adapter.ScrawlPagerAdapter;
import bean.NoteBean;
import butterknife.ButterKnife;
import butterknife.InjectView;
import constants.Constants;
import constants.NoteBeanConf;
import db.NoteDbDao;
import dialog.SaveDialog;
import dialog.ShareDialog;
import dialog.SharePopupWindow;
import okhttp3.Call;
import okhttp3.Response;
import service.PostilService;
import utils.CacheBitmapUtil;
import utils.DateTimeUtil;
import utils.OkHttpUtil;
import utils.QRCodeUtil;
import utils.SDcardUtil;
import utils.SpUtils;
import view.ChildClickableRelativeLayout;
import view.NoPreloadViewPager;

/**
 * @author Administrator
 * @version $Rev$
 * @time 2017-3-30 9:30
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public abstract class NotePageActivityBase extends NotePageActivitySupperBase implements View.OnClickListener {
    private static final String TAG = "NoteActvitit";
    @InjectView(R.id.note_write_tablet_view)
    NoPreloadViewPager mNoteViewPager;
    //    @InjectView(R.id.note_bmb)
    //    BoomMenuButton mNoteBmb;
    @InjectView(R.id.note_write_but_type)
    LinearLayout mNoteWriteButType;
    @InjectView(R.id.boom_buttons_menus)
    LinearLayout mBoomButtonsMenus;
    @InjectView(R.id.activity_main)
    ChildClickableRelativeLayout mActivityMain;
    @InjectView(R.id.note_write_iv_eraser)
    public ImageView mNoteWriteIvEraser;
    @InjectView(R.id.note_ib_up_page)
    LinearLayout mNoteIbUpPage;
    @InjectView(R.id.note_ll_next_page)
    LinearLayout mNoteLlNextPage;
    @InjectView(R.id.note_ib_next_page)
    ImageView mNoteIbNextPage;
    @InjectView(R.id.note_write_but_ok)
    LinearLayout mNoteWriteButOk;
    @InjectView(R.id.note_write_but_eraser)
    LinearLayout mNoteWriteButEraser;
    @InjectView(R.id.note_write_but_clear)
    LinearLayout mNoteWriteButClear;
    @InjectView(R.id.note_write_but_go_back)
    LinearLayout mNoteWriteButGoBack;
    @InjectView(R.id.note_tv_page_number)
    TextView mNoteTvPageNumber;
    @InjectView(R.id.note_ll_page_number)
    LinearLayout mBoomLLPageNumber;
    @InjectView(R.id.note_write_but_repeal)
    LinearLayout mNoteWriteButRepeal;
    @InjectView(R.id.note_but_hide_button)
    ImageView mNoteButHideButton;
    @InjectView(R.id.note_write_but_size)
    LinearLayout mNoteWriteButSize;
    @InjectView(R.id.boom_button_menus)
    LinearLayout mBoomButtonMenus;
    @InjectView(R.id.note_write_but_share)
    LinearLayout mBoomButtonShare;
    @InjectView(R.id.note_write_but_hide)
    LinearLayout mNoteWriteButHide;
    @InjectView(R.id.note_write_but_show)
    ImageButton mNoteWriteButShow;

    public String mBookLocation;
    public NoteDbDao mNoteDbDao;
    public NoteBean mNoteBean;
    public String mFilePath;
    public InputMethodManager mImm;
    public ScrawlPagerAdapter mPagerAdapter;
    public File mDesFile;
    public boolean mIsHideMenus = false;
    public SharePopupWindow mSharePopupWindow;
    public ShareDialog mShareDialog;
    public SpUtils mSpUtils;
    public int mScreenWidth;
    public int mScreenHeight;
    public SaveDialog mSaveDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        ButterKnife.inject(this);
        mSpUtils = new SpUtils(this);

        mBookLocation = getIntent().getStringExtra("BookLocation");
        mFilePath = getIntent().getStringExtra("FilePath");
        if (mFilePath != null) {
            mDesFile = new File(mFilePath);
        }
        initData();
        initCanvas();
    }

    protected abstract void initView();
    protected abstract void saveNote(String content, String pwd);
    protected abstract void initData();
    protected abstract ScrawlPagerAdapter initPagerAdapter();
    public void initSaveDialogView(SaveDialog saveDialog) {
    }

    private void initCanvas() {

        mScreenWidth = getScreenWidth();
        mScreenHeight = getScreenHeight();

        mPagerAdapter = initPagerAdapter();
        mNoteViewPager.setAdapter(mPagerAdapter);

        mNoteWriteButRepeal.setOnClickListener(this);
        mNoteWriteButClear.setOnClickListener(this);
        mNoteWriteButType.setOnClickListener(this);
        mNoteWriteButOk.setOnClickListener(this);
        mNoteWriteButSize.setOnClickListener(this);
        mNoteIbUpPage.setOnClickListener(this);
        mNoteLlNextPage.setOnClickListener(this);
        mNoteWriteButGoBack.setOnClickListener(this);
        mNoteWriteButEraser.setOnClickListener(this);
        mNoteButHideButton.setOnClickListener(this);
        mBoomButtonShare.setOnClickListener(this);
        mNoteWriteButHide.setOnClickListener(this);
        mNoteWriteButShow.setOnClickListener(this);
        mNoteWriteButHide.setVisibility(View.VISIBLE);

        if (mNoteViewPager.getCurrentItem() == mNoteViewPager.getAdapter().getCount() - 1) {
            mNoteIbNextPage.setImageResource(R.drawable.add_page_button);
        } else {
            mNoteIbNextPage.setImageResource(R.drawable.next);
        }

        mNoteTvPageNumber.setText((mNoteViewPager.getCurrentItem() + 1) + "/" + mPagerAdapter.getCount());

        mPagerAdapter.setEraserView(mNoteWriteIvEraser);

        viewPagerScrollListener(mNoteViewPager, mNoteIbNextPage);

        //把橡皮擦控件传给子类
        mPagerAdapter.setEraserView(mNoteWriteIvEraser);

        initView();
    }

    private boolean isShowPopupWindon = false;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.note_write_but_size://大小
                changerTuyaViewConf(mPagerAdapter.mNoteFragment.getTuyaView()
                        , mNoteWriteIvEraser, false);
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
            case R.id.note_write_but_go_back:
                goBack();
                break;
            case R.id.note_write_but_eraser:
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
                    //                    mBoomButtonsMenus.setAnimation(AnimUtils.getBottomOutAnim(PostilActivity.this));
                    mBoomButtonMenus.setVisibility(View.GONE);
                } else {
                    //                    mBoomButtonsMenus.setAnimation(AnimUtils.getBottomInAnim(PostilActivity.this));
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

    private String savePath = null;

    private void showShareDialog() {

        if (!OkHttpUtil.checkNetworkState(this)) {
            Toast.makeText(this, "请检查你的网络是否已连接！", Toast.LENGTH_LONG).show();
            return;
        }

        mShareDialog = new ShareDialog(NotePageActivityBase.this,mDesFile) {
            @Override
            public String saveFile(final String title, final String pwd, final String name) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        long currentTime = System.currentTimeMillis();
                        String time = DateTimeUtil.getCurrentTime(currentTime);

                        try {
                            mPagerAdapter.setWidthHeight(getScreenWidth(),getScreenHeight());
                            savePath = mPagerAdapter.saveNote(time, title, pwd,name);
                        } catch (Exception e) {
                            e.printStackTrace();
                            threadToast("请检查你的存储设备是否可用！" + e);
                            return;
                        }

                        if (savePath == null || savePath.contains("保存出错了,错误原因")) {
                            threadToast("savePath！" + savePath);
                            return;
                        }

                        File file = new File(savePath);
                        if (!file.exists()) {
                            threadToast("文件不存在 " + file.getPath());
                            return;
                        }
                        Map<String, String> paserm = new HashMap<String, String>();
                        paserm.put("name", OkHttpUtil.toUTF8(title
                                + "_" + currentTime));
                        paserm.put("pwd", OkHttpUtil.toUTF8(pwd.trim()));
                        paserm.put("api", OkHttpUtil.toUTF8("set"));
                        paserm.put("compere", OkHttpUtil.toUTF8(name));
                        paserm.put("time", OkHttpUtil.toUTF8(time.substring(0, time.indexOf("秒") + 1)));
                        paserm.put("MeetingAddr", OkHttpUtil.toUTF8(
                                mSpUtils.getString(Constants.MEETING_ADDR, Constants.MEETING_ADDR_NORMAL)));

                        OkHttpUtil.uploadFile(file, Constants.SHARE_URL, paserm, null, new MyStringCallBack());

                    }
                }).start();


                return savePath;
            }

            @Override
            public void hidePopup() {
                finish();
            }
        };
        mShareDialog.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
        mShareDialog.show();
    }

    private void threadToast(final String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(NotePageActivityBase.this, str, Toast.LENGTH_LONG).show();
            }
        });
    }

    class MyStringCallBack extends StringCallback {
        @Override
        public void onError(Call call, Exception e, int id) {
            mShareDialog.mButtonShare.setVisibility(View.VISIBLE);
            Toast.makeText(NotePageActivityBase.this, "上传出错 e = " + e, Toast.LENGTH_LONG).show();
            if (savePath != null) {
                File file = new File(savePath);
                if (file.exists())
                    file.delete();
            }
            Log.e("MainActity11", "上传出错 e = " + e);
            errorShareDialog();
        }

        @Override
        public void onResponse(String response, int id) {
            Log.i("MainActity11", "响应 response = " + response);
            mShareDialog.mButtonShare.setVisibility(View.VISIBLE);
            if (response.contains("error||") || TextUtils.isEmpty(response)) {
                if (savePath != null) {
                    File file = new File(savePath);
                    if (file.exists())
                        file.delete();
                }
                errorShareDialog();
                Toast.makeText(NotePageActivityBase.this, "参数错误！", Toast.LENGTH_LONG).show();
            } else {
                mShareDialog.SHARE_STATE=mShareDialog.SHARE_SUCCEED;
                mShareDialog.mButtonShare.setImageResource(R.drawable.selector_dialog_share_btn_qr_code_succeed);
                Bitmap QRCode = QRCodeUtil.createQRCode(response, 300, 300);
                mShareDialog.mIvQRCode.setImageBitmap(QRCode);
                mShareDialog.mIvQRCode.setVisibility(View.VISIBLE);
                mShareDialog.mTvTitle.setVisibility(View.VISIBLE);
                mShareDialog.mEdtTitle.setVisibility(View.GONE);
                mShareDialog.mEdtName.setVisibility(View.GONE);
                mShareDialog.EdtPwd.setVisibility(View.GONE);
                mShareDialog.mLoadingView.setVisibility(View.GONE);
                mShareDialog.mTvHint.setText("扫描二维码查看记录");
                mShareDialog.deleteFile();
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

    private void errorShareDialog() {
        mShareDialog.mEdtTitle.setVisibility(View.VISIBLE);
        mShareDialog.EdtPwd.setVisibility(View.VISIBLE);
        mShareDialog.mEdtName.setVisibility(View.VISIBLE);
        mShareDialog.mIvQRCode.setVisibility(View.GONE);
        mShareDialog.mTvHint.setVisibility(View.GONE);
        mShareDialog.mTvTitle.setVisibility(View.GONE);
        mShareDialog.mLoadingView.setVisibility(View.GONE);
        mShareDialog.mButtonShare.setImageResource(R.drawable.dialog_qrcode_btn_error);
        mShareDialog.SHARE_STATE=mShareDialog.SHARE_ERROR;
    }


    private void showSharePopupWindow() {
        if (!isShowPopupWindon) {
            int[] location = new int[2];
            mBoomButtonShare.getLocationOnScreen(location);
            mSharePopupWindow = new SharePopupWindow(NotePageActivityBase.this) {
                @Override
                public String saveFile(String name, String pwd) {
                    String time = DateTimeUtil.getCurrentTime();

                    String path = null;
                    try {
                        path = mPagerAdapter.saveNote(time, name, pwd,null);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(NotePageActivityBase.this, "请检查你的存储设备是否可用！" + e, Toast.LENGTH_LONG).show();
                    }

                    if (path.contains("保存出错了,错误原因")) {
                        Toast.makeText(NotePageActivityBase.this, path, Toast.LENGTH_SHORT).show();
                        return null;
                    }
                    return path;
                }

                @Override
                public void hidePopup() {
                    isShowPopupWindon = false;
                }
            };
            mSharePopupWindow.setSoftInputMode(PopupWindow.INPUT_METHOD_NEEDED);
            mSharePopupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            mSharePopupWindow.showAtLocation(mBoomButtonShare, Gravity.BOTTOM | Gravity.LEFT,
                    location[0] - mBoomButtonShare.getWidth(), mBoomButtonShare.getHeight());

            mSharePopupWindow.setBackgroundDrawable(getDrawable());
            //                    mSharePopupWindow.setBackgroundDrawable(new ColorDrawable(0));
            mSharePopupWindow.setOutsideTouchable(true);
            mSharePopupWindow.setFocusable(true);

            isShowPopupWindon = true;
        } else {
            mSharePopupWindow.dismiss();
            isShowPopupWindon = false;
        }
    }

    /**
     * 生成一个 透明的背景图片
     *
     * @return
     */
    private Drawable getDrawable() {
        ShapeDrawable bgdrawable = new ShapeDrawable(new OvalShape());
        bgdrawable.getPaint().setColor(getResources().getColor(R.color.transparency));
        return bgdrawable;
    }


    private void clickSave() {
        mSaveDialog = new SaveDialog(this, false,mDesFile) {
            @Override
            public void sure(String content, String pwd) {
                saveNote(content,pwd);
            }

        };


        mSaveDialog.show();
        mSaveDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                initWindow();
            }
        });

        initSaveDialogView(mSaveDialog);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PostilService.showView();
        NoteBeanConf.tuyaBeanList.clear();
        if (mDesFile != null) {
            CacheBitmapUtil.destoryCacheBitmap(mDesFile.listFiles());
        }
        SDcardUtil.deleteDirWihtFile(mDesFile);
    }


    long exitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 1500)  //System.currentTimeMillis()无论何时调用，肯定大于2000
            {
                Toast.makeText(getApplicationContext(), "再按一次退出", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
//                System.exit(0);
            }

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 判断软键盘是否弹出
     */
    public boolean isSHowKeyboard(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(context.INPUT_METHOD_SERVICE);
        View view = this.getWindow().peekDecorView();
        if (imm.hideSoftInputFromWindow(view.getWindowToken(), 0)) {
            imm.showSoftInput(view, 0);
            return true;
            //软键盘已弹出
        } else {
            return false;
            //软键盘未弹出
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        PostilService.hideView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        PostilService.showView();
    }
}
