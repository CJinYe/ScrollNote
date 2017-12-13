package dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
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

import icox.com.scrawlnote.R;
import okhttp3.Call;
import okhttp3.Response;
import utils.EditextUtil;

/**
 * @author 陈锦业
 * @version $Rev$
 * @time 2017-5-31 14:45
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public abstract class SharePostilDialog extends Dialog implements View.OnClickListener {

    private final InputMethodManager mImm;
    private Context mContext;
    public EditText mText;
    public EditText mPwd;
    public EditText EdtPwd;
    public EditText mEdtTitle;
    public ImageButton mButtonShare;
    public boolean isClickShare = false;
    public ImageView mIvQRCode;
    public TextView mTvHint;
    public TextView mTvTitle;
    public EditText mEdtName;
    private ImageButton mbutClose;
    private String mSavePath;
    public int SHARE_STATE = -10; //状态
    public final int SHARE_ERROR = -1;//错误
    public final int SHARE_LOADING = 0;//错误
    public final int SHARE_SUCCEED = 1;//成功

    public SharePostilDialog(@NonNull Context context) {
        super(context, R.style.CustomDialog);
        mContext = context;
        mImm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
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
        mButtonShare = (ImageButton) findViewById(R.id.pop_but_share);
        mIvQRCode = (ImageView) findViewById(R.id.pop_iv_share_QR_Code);
        mTvHint = (TextView) findViewById(R.id.pop_tv_share_hint);
        mTvTitle = (TextView) findViewById(R.id.pop_tv_share_title);
        mEdtName = (EditText) findViewById(R.id.pop_edt_name);
        mbutClose = (ImageButton) findViewById(R.id.dialog_share_close);

        EditextUtil.setEditTextInhibitInputSpeChat(32, mEdtTitle);
        EditextUtil.setEditTextLenght(20, EdtPwd);
        EditextUtil.setEditTextLenght(10, mEdtName);
        //不自动弹出键盘
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mButtonShare.setOnClickListener(this);
        mbutClose.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.pop_but_share:
                if (SHARE_STATE == SHARE_LOADING) {
                    return;
                }
                if (SHARE_STATE == SHARE_SUCCEED) {
                    SHARE_STATE = SHARE_LOADING;
                    hidePopup();
                    dismiss();
                } else {
                    SHARE_STATE = SHARE_LOADING;
                    if (TextUtils.isEmpty(mEdtTitle.getText().toString().trim())) {
                        Toast.makeText(mContext, "名称不能为空！", Toast.LENGTH_LONG).show();
                        SHARE_STATE = SHARE_ERROR;
                        return;
                    }
                    //关闭键盘
                    if (mImm != null) {
                        mImm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                    }

                    hide();

                    mSavePath = saveFile(mEdtTitle.getText().toString().trim(),
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

    class MyStringCallBack extends StringCallback {
        @Override
        public void onError(Call call, Exception e, int id) {
            Toast.makeText(mContext, "上传 e = " + e, Toast.LENGTH_LONG).show();
            Log.e("MainActity11", "上传出错 e = " + e);
        }

        @Override
        public void onResponse(String response, int id) {
            Log.i("MainActity11", "上传中 response = " + response);
        }

        @Override
        public void inProgress(float progress, long total, int id) {
            Log.i("MainActity11", "上传中 progress = " + progress);
        }

        @Override
        public boolean validateReponse(Response response, int id) {
            Log.i("MainActity11", response.isSuccessful() + "上传中 validateReponse = " + response);
            return super.validateReponse(response, id);
        }
    }
}
