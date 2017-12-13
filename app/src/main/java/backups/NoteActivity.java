package backups;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.nightonke.boommenu.Animation.BoomEnum;
import com.nightonke.boommenu.BoomButtons.ButtonPlaceEnum;
import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomButtons.TextOutsideCircleButton;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.ButtonEnum;
import com.nightonke.boommenu.Piece.PiecePlaceEnum;

import java.io.File;

import bean.NoteBean;
import butterknife.ButterKnife;
import butterknife.InjectView;
import db.NoteDbDao;
import dialog.RedoDialog;
import dialog.SaveDialog;
import icox.com.scrawlnote.BaseActivity;
import icox.com.scrawlnote.R;
import utils.DateTimeUtil;
import view.SnailBar;
import view.TuyaViews.TuyaView;

/**
 * @author Administrator
 * @version $Rev$
 * @time 2017-3-30 9:30
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public class NoteActivity extends BaseActivity implements View.OnClickListener, OnBMClickListener {
    private static final String TAG = "NoteActvitit";
    @InjectView(R.id.note_write_tablet_view)
    FrameLayout mNoteWriteTabletView;
    @InjectView(R.id.note_write_edt_text)
    EditText mNoteWriteEdtText;
    @InjectView(R.id.note_bmb)
    BoomMenuButton mNoteBmb;
    @InjectView(R.id.note_sanilBar)
    SnailBar mNoteSanilBar;
    @InjectView(R.id.note_write_but_ok)
    LinearLayout mNoteWriteButOk;
    @InjectView(R.id.note_write_but_clear)
    LinearLayout mNoteWriteButClear;
    @InjectView(R.id.note_write_but_repeal)
    LinearLayout mNoteWriteButRepeal;
    @InjectView(R.id.note_write_but_size)
    LinearLayout mNoteWriteButSize;
    @InjectView(R.id.note_write_but_type)
    LinearLayout mNoteWriteButType;
    @InjectView(R.id.boom_buttons_menus)
    LinearLayout mBoomButtonsMenus;
    @InjectView(R.id.activity_main)
    RelativeLayout mActivityMain;
    @InjectView(R.id.note_write_iv_eraser)
    public ImageView mNoteWriteIvEraser;

    private TuyaView mPaintView;
    private String mImgPath;
    private String mText;
    private boolean isType = false;
    private String mBookLocation;
    private NoteDbDao mNoteDbDao;
    private NoteBean mNoteBean;

    private int[] colors = new int[]{
            Color.parseColor("#000000"),
            Color.parseColor("#ff0000"),
            Color.parseColor("#fffb00"),
            Color.parseColor("#0044ff"),
            Color.parseColor("#00ff08"),
            Color.parseColor("#ff43b7"),
            Color.parseColor("#7300ff"),
            Color.parseColor("#ff8400"),

            Color.parseColor("#00ff7895"),

    };

    private String[] colorsName = new String[]{
            "黑色",
            "红色",
            "黄色",
            "蓝色",
            "绿色",
            "粉红色",
            "紫色",
            "橙色",

            "透明",
    };
    private String mFilePath;
    private InputMethodManager mImm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        setContentView(R.layout.activity_note_copy);
        ButterKnife.inject(this);

        mBookLocation = getIntent().getStringExtra("BookLocation");
        mFilePath = getIntent().getStringExtra("FilePath");
        initData();
        initCanvas();
        initLeftMenu();

        //        initBoomMenu();

        //        initRightMenuBackground();
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


    private void initData() {
        mNoteDbDao = new NoteDbDao(this);
        if (mBookLocation != null) {
            mNoteBean = mNoteDbDao.queryNote(mBookLocation);
        } else if (mFilePath != null) {
            mNoteBean = mNoteDbDao.queryNoteTime(mFilePath);
        } else {
            mNoteBean = null;
        }
    }

    private void initCanvas() {

        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        getWindow().getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);

        int screenWidth = mDisplayMetrics.widthPixels;
        int screenHeight = mDisplayMetrics.heightPixels;


        if (null == mNoteBean || TextUtils.isEmpty(mNoteBean.path)) {
            // 初始化画布
            mPaintView = new TuyaView(this, screenWidth, screenHeight);
        } else {
            //如果有保存的画布,就取出画布
            Bitmap bitmapc = BitmapFactory.decodeFile(mNoteBean.path);
            Log.d(TAG, "bitmapc = " + bitmapc + " file = " + new File(mNoteBean.path).exists());
            mPaintView = new TuyaView(this, bitmapc, screenWidth, screenHeight);
        }
        //添加画布
        mNoteWriteTabletView.addView(mPaintView);
        Log.d(TAG, "screenWidth = " + screenWidth + "  screenHeight = " + screenHeight + "  , " + mNoteWriteTabletView.getWidth() + "  " + mNoteWriteTabletView.getHeight());
        mPaintView.requestFocus();

        if (null != mNoteBean && !TextUtils.isEmpty(mNoteBean.text)) {
            //如果数据库有笔记文本就显示文本
            mNoteWriteEdtText.setVisibility(View.VISIBLE);
            mNoteWriteEdtText.setEnabled(false);
            mNoteWriteEdtText.setText(mNoteBean.text);
        }

        //                mNoteWriteButCancel.setOnClickListener(this);
        //                mNoteWriteButRecover.setOnClickListener(this);
        mNoteWriteButRepeal.setOnClickListener(this);
        mNoteWriteButClear.setOnClickListener(this);
        mNoteWriteButType.setOnClickListener(this);
        mNoteWriteButOk.setOnClickListener(this);
        mNoteWriteButSize.setOnClickListener(this);

        initSizeBar(screenWidth, screenHeight);

    }

    /**
     * 初始化画笔选择大小的选择器
     *
     * @param screenWidth
     * @param screenHeight
     */
    private void initSizeBar(int screenWidth, int screenHeight) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int) (screenWidth / 1.5),
                ViewGroup.LayoutParams.WRAP_CONTENT);

        if (screenWidth <= 900) {
            params.weight = (float) (screenWidth / 1.1);
        } else if (screenWidth <= 2000) {
            params.weight = (float) (screenWidth / 1.6);
        } else if (screenWidth <= 3000) {
            params.weight = (float) (screenWidth / 2.2);
        } else if (screenWidth <= 4000) {
            params.weight = (float) (screenWidth / 3.5);
        } else {
            params.weight = (float) (screenWidth / 5);
        }

        params.weight = screenWidth / 2;
        params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.CENTER_HORIZONTAL;
        mNoteSanilBar.setLayoutParams(params);

        mNoteSanilBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mPaintView.selectPaintSize(progress+10);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void setBoomMenuEnum(PiecePlaceEnum dot93, ButtonPlaceEnum sc91) {
        mNoteBmb.setPiecePlaceEnum(dot93);
        mNoteBmb.setButtonPlaceEnum(sc91);
    }

    /**
     * 左上角菜单
     */
    private void initLeftMenu() {
        mNoteBmb = (BoomMenuButton) findViewById(R.id.note_bmb);
        assert mNoteBmb != null;
        mNoteBmb.setButtonEnum(ButtonEnum.TextOutsideCircle);
        setBoomMenuEnum(PiecePlaceEnum.DOT_9_1, ButtonPlaceEnum.SC_9_2);
        mNoteBmb.setBoomEnum(BoomEnum.RANDOM);
        //        mNoteBmb.setBackgroundEffect(false);
        mNoteBmb.setDimColor(Color.parseColor("#99000000"));
        mNoteBmb.setNormalColor(Color.parseColor("#80ff7c58"));
        //        bmb.addBuilder(BuilderManager.getSimpleCircleButtonBuilder());

        for (int i = 0; i < mNoteBmb.getPiecePlaceEnum().pieceNumber(); i++) {
            addBuilder(i);
        }

    }

    private void addBuilder(int i) {
        if (i == 8) {
            mNoteBmb.addBuilder(new TextOutsideCircleButton.Builder()
                    .normalImageRes(R.drawable.eraser)
                    .normalText("橡皮擦")
                    .normalColor(Color.parseColor("#00ff7895"))
                    .normalTextColor(Color.WHITE)
                    .textSize(15)
                    .listener(this)
            );
        } else {
            addBuilder(colors[i], colorsName[i]);
        }

    }

    private void addBuilder(int red, String text) {
        mNoteBmb.addBuilder(new TextOutsideCircleButton.Builder()
                //                .normalImageRes(BuilderManager.getImageResource())
                .normalColor(red)
                .normalText(text)
                .normalTextColor(Color.WHITE)
                .textSize(15)
                .listener(this)
        );
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //            case R.id.note_tapBarMenu://开关
            //                break;
            //                    case R.id.note_write_but_recover://恢复
            //                        mPaintView.recover();
            //                        break;
            //                    case R.id.note_write_but_cancel://橡皮擦
            //                        mPaintView.selectEraser();
            //                        break;

            case R.id.note_write_but_size://大小
                mNoteSanilBar.setVisibility(
                        mNoteSanilBar.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE
                );
                break;
            case R.id.note_write_but_ok://提交
                clickSave();
                break;
            case R.id.note_write_but_clear://清屏
                clickClear();
                break;
            case R.id.note_write_but_repeal://撤销
                mPaintView.undo();
                break;
            case R.id.note_write_but_type://文字
                isType = !isType;
                if (isType) {
                    mNoteWriteEdtText.setVisibility(View.VISIBLE);
                    mNoteWriteEdtText.setEnabled(true);
                } else {
                    if (!TextUtils.isEmpty(mNoteWriteEdtText.getText().toString())) {
                        mNoteWriteEdtText.setVisibility(View.VISIBLE);
                        mNoteWriteEdtText.setEnabled(false);
                    } else {
                        mNoteWriteEdtText.setVisibility(View.GONE);
                    }
                }
                break;

            default:
                break;
        }
    }

    private void clickClear() {
        RedoDialog redoDialog = new RedoDialog(this) {
            @Override
            public void sure() {
                mPaintView.redo();
            }
        };

        redoDialog.show();
        redoDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                initWindow();
            }
        });
    }

    private void clickSave() {
        final SaveDialog saveDialog = new SaveDialog(this,false) {
            @Override
            public void sure(String content, String pwd) {
                //得到当前时间
                String time = DateTimeUtil.getCurrentTime();
                String path = mPaintView.saveToSDCard(time,content);
                if (mNoteBean != null) {
                    mNoteDbDao.updateNote(mBookLocation, time, path, mNoteWriteEdtText.getText().toString());
                } else {
                    mNoteDbDao.saveNote(mBookLocation, time, path, mNoteWriteEdtText.getText().toString(),null,null);
                }
                finish();
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

    private void changeBackgroundColor() {
        //        int centerX = mNoteWriteButOk.getWidth() / 2;
        //        int centerY = mNoteWriteButOk.getHeight() / 2;
        //        int finalRadius = Math.max(mBoomButtonsMenus.getWidth(), mBoomButtonsMenus.getHeight());
        //        View backgroundColorTemp = new View(this);
        //        mBoomButtonsMenus.addView(backgroundColorTemp);
        //        backgroundColorTemp.setBackgroundColor(colors[(int) (Math.random() * 6)]);
        //        Animator changeBackgroundColor = null;
        //        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
        //            changeBackgroundColor = ViewAnimationUtils.createCircularReveal(backgroundColorTemp, centerX, centerY, 0, finalRadius);
        //            changeBackgroundColor.addListener(new AnimatorListenerAdapter() {
        //                @Override
        //                public void onAnimationEnd(Animator animation) {
        //                    super.onAnimationEnd(animation);
        //                    mBoomButtonsMenus.setBackgroundColor(colors[(int) (Math.random() * 6)]);
        //                }
        //            });
        //        }
        //                    mBoomButtonsMenus.setBackgroundColor(colors[(int) (Math.random() * 6)]);
    }

    @Override
    public void onBoomButtonClick(int index) {

        if (mImm!=null){
            mImm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(),
                    0);
        }

        if (index == 8) {
            mPaintView.selectPaintColor(Color.WHITE);
            mNoteWriteIvEraser.setVisibility(View.VISIBLE);
        } else {
            mPaintView.selectPaintColor(colors[index]);
            mNoteWriteIvEraser.setVisibility(View.GONE);
        }
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
                System.exit(0);
            }

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
