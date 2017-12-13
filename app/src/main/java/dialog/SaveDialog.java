package dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import icox.com.scrawlnote.R;
import utils.EditextUtil;

/**
 * @author Administrator
 * @version $Rev$
 * @time 2017-4-5 14:45
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public abstract class SaveDialog extends Dialog implements View.OnClickListener {

    private final boolean mIsPostil;
    private String mName;
    private File mDesFile;
    private String mCurrentTime;
    private String mTitle;
    private Context mContext;
    public EditText mText;
    public EditText mPwd;

    public SaveDialog(@NonNull Context context, boolean isPostil) {
        super(context, R.style.CustomDialog);
        mContext = context;
        mIsPostil = isPostil;
    }

    public SaveDialog(@NonNull Context context, boolean isPostil, File desFile) {
        super(context, R.style.CustomDialog);
        mContext = context;
        mIsPostil = isPostil;
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
        mText = (EditText) findViewById(R.id.dialog_redo_edt);
        mPwd = (EditText) findViewById(R.id.dialog_redo_edt_pwd);
        mText.setVisibility(View.VISIBLE);
        //        mPwd.setVisibility(View.VISIBLE);

        if (mTitle != null){
            mText.setText(mTitle);
            mText.setSelection(mTitle.length());
        }

        //不允许输入特殊字符
        EditextUtil.setEditTextInhibitInputSpeChat(32, mText);
        EditextUtil.setEditTextLenght(20, mPwd);
        //不自动弹出键盘
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        TextView title = (TextView) findViewById(R.id.dialog_redo_tv_title);
        title.setText("是否确定要保存？");
        sure.setOnClickListener(this);
        cancel.setOnClickListener(this);
    }

    public void hideEdttext() {
        mText.setVisibility(View.GONE);
        mPwd.setVisibility(View.GONE);
        mText.setText("test");
    }

    public void deleteFile() {
        if (mTitle != null) {
            if (mTitle.equals(mText.getText().toString())) {
                if (mDesFile != null) {
                    File file = new File(mDesFile.getPath()+".zip");
                    file.delete();
                }
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.dialog_redo_btn_sure:
                if (TextUtils.isEmpty(mText.getText().toString().trim())) {
                    Toast.makeText(mContext, "名称不能为空！", Toast.LENGTH_LONG).show();
                    return;
                }
                if (mIsPostil) {
                    sure(mText.getText().toString().trim(), mPwd.getText().toString().trim());
                } else {
                    Toast.makeText(mContext, "保存中,请耐心等候！", Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            sure(mText.getText().toString().trim(), mPwd.getText().toString().trim());
                        }
                    }, 300);
                }

                dismiss();
                break;
            case R.id.dialog_redo_btn_cancel:
                dismiss();
                break;
        }
    }

    public abstract void sure(String content, String pwd);
}
