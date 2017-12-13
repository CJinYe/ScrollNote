package dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;

import bean.NoteBean;
import db.NoteDbDao;
import icox.com.scrawlnote.R;
import okhttp3.Call;
import okhttp3.Response;
import utils.EditextUtil;
import utils.QRCodeUtil;
import view.loading.LoadingView;

/**
 * @author 陈锦业
 * @version $Rev$
 * @time 2017-5-31 14:45
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public abstract class ShareDialog extends Dialog implements View.OnClickListener {

    private final InputMethodManager mImm;
    private File mDesFile;
    private Context mContext;
    private EditText mText;
    private EditText mPwd;
    public EditText EdtPwd;
    public EditText mEdtTitle;
    public ImageButton mButtonShare;
    public ImageView mIvQRCode;
    public TextView mTvHint;
    public String mFilePath;
    public TextView mTvTitle;
    public LoadingView mLoadingView;
    private ImageButton mbutClose;
    public EditText mEdtName;
    public int SHARE_STATE = -10; //状态
    public final int SHARE_ERROR = -1;//错误
    public final int SHARE_LOADING = 0;//错误
    public final int SHARE_SUCCEED = 1;//成功
    private NoteDbDao mNoteDbDao;
    private String mTitle;
    private String mName;
    private String mCurrentTime;

    public ShareDialog(@NonNull Context context, File desFile) {
        super(context, R.style.CustomDialog);
        mContext = context;
        mNoteDbDao = new NoteDbDao(mContext);
        mImm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (desFile != null && desFile.exists()) {
            mTitle = desFile.getName().substring(0, desFile.getName().indexOf("_"));
            mCurrentTime = desFile.getName().substring(desFile.getName().indexOf("_") + 1, desFile.getName().lastIndexOf("_"));
            mName = desFile.getName().substring(desFile.getName().lastIndexOf("_") + 1, desFile.getName().length());
            //            mCurrentTime = DateTimeUtil.stringToLong(time);
            mDesFile = desFile;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popuwindow_share);
        initView();//初始化自定的布局/控件
        Window window = this.getWindow();
        if (window != null) {
            WindowManager.LayoutParams attr = window.getAttributes();
            if (attr != null) {
                attr.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                attr.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                attr.gravity = Gravity.CENTER;//设置dialog 在布局中的位置
            }
        }
    }

    private void initView() {
        EdtPwd = (EditText) findViewById(R.id.pop_edt_pwd);
        mEdtTitle = (EditText) findViewById(R.id.pop_edt_title);
        mIvQRCode = (ImageView) findViewById(R.id.pop_iv_share_QR_Code);
        mTvHint = (TextView) findViewById(R.id.pop_tv_share_hint);
        mTvTitle = (TextView) findViewById(R.id.pop_tv_share_title);
        mEdtName = (EditText) findViewById(R.id.pop_edt_name);
        mButtonShare = (ImageButton) findViewById(R.id.pop_but_share);
        mLoadingView = (LoadingView) findViewById(R.id.pop_loading_view);
        mbutClose = (ImageButton) findViewById(R.id.dialog_share_close);

        //规定输入的长度和字符
        EditextUtil.setEditTextInhibitInputSpeChat(32, mEdtTitle);
        EditextUtil.setEditTextLenght(20, EdtPwd);
        EditextUtil.setEditTextLenght(10, mEdtName);
        mButtonShare.setOnClickListener(this);
        mbutClose.setOnClickListener(this);

        if (mName != null && !mName.equals("null")){
            mEdtName.setText(mName);
            mEdtName.setSelection(mName.length());
        }
        if (mTitle != null){
            mEdtTitle.setText(mTitle);
            mEdtTitle.setSelection(mTitle.length());
        }
        if (mCurrentTime != null) {
            NoteBean noteBean = mNoteDbDao.queryNoteTime(String.valueOf(mCurrentTime));
            if (noteBean != null && noteBean.password != null){
                EdtPwd.setText(noteBean.password);
                EdtPwd.setSelection(noteBean.password.length());
            }
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.pop_but_share:

                if (TextUtils.isEmpty(mEdtTitle.getText().toString().trim())) {
                    Toast.makeText(mContext, "名称不能为空！", Toast.LENGTH_LONG).show();
                    return;
                }

                //关闭键盘
                if (mImm != null) {
                    mImm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                }
                if (SHARE_STATE == SHARE_LOADING) {
                    return;
                }

                mEdtTitle.setVisibility(View.GONE);
                EdtPwd.setVisibility(View.GONE);
                mEdtName.setVisibility(View.GONE);
                mTvHint.setVisibility(View.VISIBLE);
                mLoadingView.setVisibility(View.VISIBLE);
                mIvQRCode.setVisibility(View.GONE);
                mButtonShare.setVisibility(View.GONE);
                mTvHint.setText("正在分享,请稍等片刻！");

                mTvTitle.setText(mEdtTitle.getText().toString().trim());

                if (SHARE_STATE == SHARE_SUCCEED) {
                    SHARE_STATE = SHARE_LOADING;
                    hidePopup();
                    dismiss();
                } else {
                    SHARE_STATE = SHARE_LOADING;
                    if (mFilePath != null) {
                        File file = new File(mFilePath);
                        if (file.exists())
                            file.delete();
                    }
                    mFilePath = saveFile(mEdtTitle.getText().toString().trim(),
                            EdtPwd.getText().toString().trim()
                            , mEdtName.getText().toString().trim());
                }

                break;

            case R.id.dialog_share_close:
                dismiss();
                break;
            default:
                break;
        }
    }


    public abstract String saveFile(String title, String pwd, String name);

    public abstract void hidePopup();

    public void deleteFile() {
        if (mTitle != null) {
            if (mTitle.equals(mEdtTitle.getText().toString())) {
                if (mDesFile != null) {
                    File file = new File(mDesFile.getPath()+".zip");
                    boolean is = file.delete();
                }
            }
        }
    }

    class MyStringCallBack extends StringCallback {
        @Override
        public void onError(Call call, Exception e, int id) {
            mButtonShare.setImageResource(R.drawable.dialog_qrcode_btn_error);
            Toast.makeText(mContext, "上传 e = " + e, Toast.LENGTH_LONG).show();
            if (mFilePath != null) {
                File file = new File(mFilePath);
                if (file.exists())
                    file.delete();
            }
        }

        @Override
        public void onResponse(String response, int id) {
            if (response.contains("error||") || TextUtils.isEmpty(response)) {
                mButtonShare.setImageResource(R.drawable.dialog_qrcode_btn_error);
                if (mFilePath != null) {
                    File file = new File(mFilePath);
                    if (file.exists())
                        file.delete();
                }
                Toast.makeText(mContext, "参数错误！", Toast.LENGTH_LONG).show();
            } else {
                mButtonShare.setImageResource(R.drawable.selector_dialog_share_btn_qr_code_succeed);
                Bitmap QRCode = QRCodeUtil.createQRCode(response, 200, 200);
                mIvQRCode.setImageBitmap(QRCode);
                mIvQRCode.setVisibility(View.VISIBLE);
                mTvHint.setVisibility(View.VISIBLE);
                mTvTitle.setVisibility(View.VISIBLE);
                mEdtTitle.setVisibility(View.GONE);
                EdtPwd.setVisibility(View.GONE);
            }
        }

        @Override
        public void inProgress(float progress, long total, int id) {
        }

        @Override
        public boolean validateReponse(Response response, int id) {
            //            mButtonShare.setProgress(-1);
            //            mButtonShare.setProgress(-1);
            return super.validateReponse(response, id);
        }
    }
}
