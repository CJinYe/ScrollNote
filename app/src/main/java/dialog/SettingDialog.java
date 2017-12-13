package dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import constants.Constants;
import icox.com.scrawlnote.R;
import service.PostilService;
import utils.EditextUtil;
import utils.SpUtils;
import view.SwitchView;

/**
 * @author Administrator
 * @version $Rev$
 * @time 2017-4-5 14:45
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public abstract class SettingDialog extends Dialog implements View.OnClickListener {

    private String mFileName;
    private Context mContext;
    private EditText mText;
    private EditText mPwd;
    private final SpUtils mSpUtils;

    public SettingDialog(@NonNull Context context) {
        super(context, R.style.CustomDialog);
        mContext = context;
        mSpUtils = new SpUtils(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_setting);
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
        TextView title = (TextView) findViewById(R.id.dialog_redo_tv_title);

        mPwd = (EditText) findViewById(R.id.dialog_redo_edt);
        final SwitchView aSwitch = (SwitchView) findViewById(R.id.dialog_setting_switch);
        String mettingAddr = mSpUtils.getString(Constants.MEETING_ADDR, Constants.MEETING_ADDR_NORMAL);
        mPwd.setText(mettingAddr);
        mPwd.setSelection(mPwd.getText().length());
        EditextUtil.setEditTextLenght(30, mPwd);
        title.setText("修改会议地点");
        sure.setOnClickListener(this);
        cancel.setOnClickListener(this);

//        aSwitch.setOnStateChangedListener(new SwitchView.OnStateChangedListener() {
//            @Override
//            public void toggleToOn(SwitchView view) {
////                mSpUtils.putBoolean(Constants.ON_OFF_POSTIL,false);
////                Intent intent = new Intent(mContext, PostilService.class);
////                mContext.stopPostilService(intent);
//            }
//
//            @Override
//            public void toggleToOff(SwitchView view) {
////                mSpUtils.putBoolean(Constants.ON_OFF_POSTIL,true);
////                Intent intent = new Intent(mContext, PostilService.class);
////                mContext.startActivity(intent);
//            }
//        });
        boolean isOpen = mSpUtils.getBoolean(Constants.ON_OFF_POSTIL, true);
        aSwitch.setOpened(isOpen);
        aSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isOpen = aSwitch.isOpened();
                mSpUtils.putBoolean(Constants.ON_OFF_POSTIL,isOpen);
                if (isOpen){
                    startPostilService();
                    PostilService.showView();
                }else {
                    PostilService.hideView();
                    stopPostilService();
                }
            }
        });
    }

    protected abstract void stopPostilService();
    public abstract void startPostilService();

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.dialog_redo_btn_sure:
                String mettingAddr = mPwd.getText().toString().trim();
                mSpUtils.putString(Constants.MEETING_ADDR,mettingAddr);
                dismiss();
                break;
            case R.id.dialog_redo_btn_cancel:
                dismiss();
                break;
        }
    }

}
