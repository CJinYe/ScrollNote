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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SVBar;
import com.larswerkman.holocolorpicker.SaturationBar;

import icox.com.scrawlnote.R;
import view.ProgressTextView;
import view.SnailBar;
import view.TuyaViews.TuyaViewPage;

/**
 * @author Administrator
 * @version $Rev$
 * @time 2017-4-28 14:22
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public abstract class ColorPickerDialog extends Dialog {

    private String TAG = "ColorPickerDialog";
    private final TuyaViewPage mTuyaView;
    private ColorPicker mBackgroundPicker;
    private boolean mIsBackgroundColorChange = false;
    private boolean isPaintColorChange = false;
    private boolean isPaintSizeChange = false;
    private ColorPicker mPaintPicker;
    private SnailBar mSeekBar;
    private ProgressTextView mSeekText;
    private LinearLayout mLlBackground;
    private boolean mIsHideBacggroundView;

    public ColorPickerDialog(@NonNull Context context, TuyaViewPage tuyaView) {
        super(context, R.style.CustomDialog);
        mTuyaView = tuyaView;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyCompat();
        setContentView(R.layout.dialog_color_picker);
        initBackgoundView();
        initPaintPicker();
        initView();

    }

    public void hideBackgroundView(boolean isHide){
        mIsHideBacggroundView = isHide;
    }

    private void initView() {
        ImageView button = (ImageView) findViewById(R.id.dialog_color_picker_bt_sure);
        mLlBackground = (LinearLayout) findViewById(R.id.dialog_color_ll_background);

        if (mIsHideBacggroundView)mLlBackground.setVisibility(View.GONE);

        mSeekText = (ProgressTextView) findViewById(R.id.dialog_color_picker_seek_text);
        mSeekBar = (view.SnailBar) findViewById(R.id.dialog_color_picker_seek_bar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                isPaintSizeChange = true;
                mSeekText.setVisibility(View.VISIBLE);
                mSeekText.setProgress(progress,progress+"");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mSeekText.setVisibility(View.GONE);
            }
        });
        mSeekBar.setProgress(mTuyaView.currentSize);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPaintColorChange) {
                    mTuyaView.selectPaintColor(mPaintPicker.getColor());
                    mTuyaView.currentColor = mPaintPicker.getColor();
                }

                if (mIsBackgroundColorChange) {
                    mTuyaView.selectorCanvasColor(mBackgroundPicker.getColor());
                    mTuyaView.backgroundColor = mBackgroundPicker.getColor();
                }

                if (isPaintSizeChange) {
                    mTuyaView.selectPaintSize(mSeekBar.getProgress());
                }

                sureClick(isPaintColorChange);
                dismiss();
            }
        });

        mSeekText.setVisibility(View.GONE);
    }

    public abstract void sureClick(boolean isPaintColorChange);

    @Override
    public void setOnDismissListener(OnDismissListener listener) {
        super.setOnDismissListener(listener);
        if (mSeekText.mBackgroundBit!=null&&mSeekText.mBackgroundBit.isRecycled()){
            mSeekText.mBackgroundBit.recycle();
            mSeekText.mBackgroundBit = null;
        }
    }

    private void initPaintPicker() {
        final TextView paintTv = (TextView) findViewById(R.id.dialog_color_paint_tv);
        mPaintPicker = (ColorPicker) findViewById(R.id.dialog_color_paint_picker);
        SVBar svBar = (SVBar) findViewById(R.id.dialog_color_paint_svbar);
        SaturationBar saturationBar = (SaturationBar) findViewById(R.id.dialog_color_paint_saturationbar);

        mPaintPicker.addSVBar(svBar);
        mPaintPicker.addSaturationBar(saturationBar);

        mPaintPicker.getColor();

        mPaintPicker.setOldCenterColor(mPaintPicker.getColor());
        mPaintPicker.setOnColorChangedListener(new ColorPicker.OnColorChangedListener() {
            @Override
            public void onColorChanged(int color) {
                paintTv.setTextColor(color);
                isPaintColorChange = true;
            }
        });

        //to turn of showing the old color
        mPaintPicker.setShowOldCenterColor(false);

    }

    private void initBackgoundView() {

        final TextView bgTv = (TextView) findViewById(R.id.dialog_color_background_tv);


        mBackgroundPicker = (ColorPicker) findViewById(R.id.dialog_color_background_picker);
        SVBar svBar = (SVBar) findViewById(R.id.dialog_color_background_svbar);
        SaturationBar saturationBar = (SaturationBar) findViewById(R.id.dialog_color_background_saturationbar);


        mBackgroundPicker.addSVBar(svBar);
        mBackgroundPicker.addSaturationBar(saturationBar);

        mBackgroundPicker.getColor();

        mBackgroundPicker.setOldCenterColor(mBackgroundPicker.getColor());
        mBackgroundPicker.setOnColorChangedListener(new ColorPicker.OnColorChangedListener() {
            @Override
            public void onColorChanged(int color) {
                bgTv.setTextColor(color);
//                mTuyaView.setBackgroundColor(color);
                mIsBackgroundColorChange = true;
            }
        });

        //to turn of showing the old color
        mBackgroundPicker.setShowOldCenterColor(false);

        //        //adding onChangeListeners to bars
        //        opacitybar.setOnOpacityChangeListener(new OnOpacityChangeListener …)
        //        valuebar.setOnValueChangeListener(new OnValueChangeListener …)
        //        saturationBar.setOnSaturationChangeListener(new OnSaturationChangeListener …)
    }

    protected void applyCompat() {
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
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

}
