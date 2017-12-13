package dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhy.http.okhttp.callback.StringCallback;

import constants.Constants;
import icox.com.scrawlnote.R;
import okhttp3.Call;
import utils.DateTimeUtil;
import utils.QRCodeUtil;

/**
 * @author Administrator
 * @version $Rev$
 * @time 2017-4-5 14:45
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public abstract class PwdDialog extends Dialog implements View.OnClickListener {

    private String mFileName;
    private Context mContext;
    private EditText mText;
    private EditText mPwd;
    private LinearLayout mTopMainLl;
    private LinearLayout mQrCodeLl;
    private ImageView mQrCodeIv;

    public PwdDialog(@NonNull Context context, String fileName) {
        super(context, R.style.CustomDialog);
        mContext = context;
        mFileName = fileName.replaceAll(".zip", "");

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_redo);
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
        //获取子控件
        ImageButton sure = (ImageButton) findViewById(R.id.dialog_redo_btn_sure);
        ImageButton cancel = (ImageButton) findViewById(R.id.dialog_redo_btn_cancel);
        ImageButton forgetPwd = (ImageButton) findViewById(R.id.dialog_redo_btn_forget_pwd);
        mTopMainLl = (LinearLayout) findViewById(R.id.dialog_pwd_top_main);
        mQrCodeLl = (LinearLayout) findViewById(R.id.dialog_pwd_QRCode_main);
        mQrCodeIv = (ImageView) findViewById(R.id.dialog_pwd_QRCode_iv);
        mPwd = (EditText) findViewById(R.id.dialog_redo_edt_pwd);
        mPwd.setVisibility(View.VISIBLE);
        forgetPwd.setVisibility(View.VISIBLE);
        TextView title = (TextView) findViewById(R.id.dialog_redo_tv_title);
        title.setText("请输入密码");
        sure.setOnClickListener(this);
        cancel.setOnClickListener(this);
        forgetPwd.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.dialog_redo_btn_sure:
                sure(mPwd.getText().toString());
                dismiss();
                break;
            case R.id.dialog_redo_btn_cancel:
                dismiss();
                break;
            case R.id.dialog_redo_btn_forget_pwd://找回密码
                String title = mFileName.substring(0, mFileName.indexOf("_"));
                String time = mFileName.substring(mFileName.indexOf("_") + 1, mFileName.lastIndexOf("_"));
                long currentTime = DateTimeUtil.stringToLong(time);

//                String UrlCode = Constants.FIND_PWD_URL_1 + OkHttpUtil.toUTF8(title) + "_" + currentTime + Constants.FIND_PWD_URL_2;
                String UrlCode = Constants.FIND_PWD_URL_1 + currentTime + Constants.FIND_PWD_URL_2;
                Bitmap QRCode = QRCodeUtil.createQRCode(UrlCode, 300, 300);

                mQrCodeLl.setVisibility(View.VISIBLE);
                mTopMainLl.setVisibility(View.GONE);
                mQrCodeIv.setImageBitmap(QRCode);

                //                OkHttpUtil.getHttp(UrlCode,new MyStringCallBack());

                break;
        }
    }

    class MyStringCallBack extends StringCallback {
        @Override
        public void onError(Call call, Exception e, int id) {

        }

        @Override
        public void onResponse(String response, int id) {

        }
    }

    public abstract void sure(String pwd);
}
