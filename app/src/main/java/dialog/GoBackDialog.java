package dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import icox.com.scrawlnote.R;

/**
 * @author Administrator
 * @version $Rev$
 * @time 2017-4-5 14:45
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public abstract class GoBackDialog extends Dialog implements View.OnClickListener{

    private Context mContext;

    public GoBackDialog(@NonNull Context context) {
        super(context, R.style.CustomDialog);
        mContext = context;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyCompat();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.dialog_go_back);
        initView();//初始化自定的布局/控件
    }

    protected void applyCompat(){
        if (Build.VERSION.SDK_INT < 19) {
            return;
        }
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
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
        TextView textView = (TextView) findViewById(R.id.dialog_redo_tv_title);
        textView.setText("你还未保存,是否要退出？");
        sure.setOnClickListener(this);
        cancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.dialog_redo_btn_sure:
                sure();
                dismiss();
                break;
            case R.id.dialog_redo_btn_cancel:
                dismiss();
                break;
        }
    }


    private void initWindow() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public abstract void sure();
}
