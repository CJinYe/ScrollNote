package icox.com.scrawlnote;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.icox.updateapp.defined_dialog.DownloadApp;
import com.icox.updateapp.service.UpdateAppService;
import com.icox.updateapp.utils.AES;
import com.icox.updateapp.utils.Util;
import com.nightonke.boommenu.BoomMenuButton;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.util.ArrayList;

import adapter.NoteGridAdapter;
import backups.NoteActivity;
import butterknife.ButterKnife;
import butterknife.InjectView;
import db.NoteDbDao;
import dialog.DeleteNoteDialog;
import dialog.PwdDialog;
import dialog.SettingDialog;
import service.PostilService;
import utils.PermissionUtil;
import utils.SDcardUtil;
import utils.Zip4jUtil;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    @InjectView(R.id.main_btn_note)
    Button mMainBtnNote;
    @InjectView(R.id.activity_main)
    RelativeLayout mActivityMain;
    @InjectView(R.id.main_btn_note_portrait)
    ImageButton mMainBtnNotePortrait;
    @InjectView(R.id.main_btn_note_demo)
    Button mMainBtnNoteDemo;
    @InjectView(R.id.main_note_gv)
    GridView mMainNoteGv;
    @InjectView(R.id.main_btn_go_back)
    ImageButton mMainBtnGoBack;
    @InjectView(R.id.layout)
    LinearLayout mLayout;
    @InjectView(R.id.main_btn_setting)
    ImageButton mMainBtnSetting;
    @InjectView(R.id.main_btn_help)
    ImageButton mMainBtnHelp;

    private String TAG = "MainActivity";
    private BoomMenuButton bmb;
    private ArrayList<Pair> piecesAndButtons = new ArrayList<>();
    private NoteGridAdapter mAdapter;
    private NoteDbDao mNoteDbDao;
    private MediaProjectionManager mMediaProjectionManager;
    private int REQUEST_MEDIA_PROJECTION = 1;
    private int result;
    private Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateApp();

        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        initView();
        mNoteDbDao = new NoteDbDao(this);
        //6.0系统申请权限
        PermissionUtil.intPermission(this);
        //开启截屏服务
        Intent intent = new Intent(getApplicationContext(), PostilService.class);
        startService(intent);

        mMainBtnNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NoteActivity.class);
                ComponentName componentName = new ComponentName("icox.com.scrawlnote",
                        "icox.com.scrawlnote.NotePortraitActivity");
                intent.setComponent(componentName);
                intent.putExtra("BookLocation", "test");
                startActivity(intent);
            }
        });

        mMainBtnNotePortrait.setOnClickListener(this);
        mMainBtnGoBack.setOnClickListener(this);
        mMainBtnSetting.setOnClickListener(this);
        mMainBtnHelp.setOnClickListener(this);

        try {
            SDcardUtil.getPicturesCacheFile();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected void updateApp() {
        AES aes = new AES();
        String packageName = aes.stringFromJNICheck(this, this.getPackageName());
        if (!packageName.equals(this.getPackageName())) {
            Toast.makeText(this, "该软件为盗版软件,请安装正版软件！", Toast.LENGTH_LONG).show();
        }

        Intent intentService = new Intent(this, UpdateAppService.class);
        this.startService(intentService);
        SharedPreferences mCheckSettings = this.getSharedPreferences("UpdateAppService", 0);
        int appUpdate = mCheckSettings.getInt("appUpdate", 0);
        String versionName = mCheckSettings.getString("versionName", "");
        if (!versionName.equals(Util.getAPPVersionName(this)) && appUpdate == 2) {
            Intent intentUpdate = new Intent(this, DownloadApp.class);
            this.startActivity(intentUpdate);
            this.finish();
        }

    }


    private void initView() {
        //        String sdPath = Environment.getExternalStorageDirectory() + "/ScrawlNote";
        //        File file = new File(sdPath);
        //        File[] Files = file.listFiles();
        mAdapter = new NoteGridAdapter(this);
        mMainNoteGv.setAdapter(mAdapter);

        mMainNoteGv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final File file = mAdapter.mFiles.get(position);
                final String fileName = file.getPath().replaceAll(".zip", "");
                final Intent intent = new Intent(MainActivity.this, NotePageActivity.class);

                try {
                    final ZipFile zipFile = new ZipFile(file);
                    //判断文件是否加密
                    if (zipFile.isEncrypted()) {
                        PwdDialog pwdDialog = new PwdDialog(MainActivity.this, file.getName()) {
                            @Override
                            public void sure(String pwd) {
                                try {
                                    File fileUn = Zip4jUtil.unzip(file, fileName, pwd);
                                    intent.putExtra("FilePath", fileUn.getPath());
                                    startActivity(intent);
                                } catch (ZipException e) {
                                    e.printStackTrace();
                                    Log.i(TAG, "解压出错 , e = " + e);
                                    Toast.makeText(MainActivity.this, "密码有误！", Toast.LENGTH_SHORT).show();
                                    File file1 = new File(fileName);
                                    if (file1.exists()) {
                                        file1.delete();
                                        refreshAdapter();
                                    }
                                }
                            }
                        };
                        pwdDialog.show();
                    } else {
                        Log.i(TAG, "无密码解压");
                        File fileUn = Zip4jUtil.unzip(file, fileName, "");
                        intent.putExtra("FilePath", fileUn.getPath());
                        startActivity(intent);
                    }
                } catch (ZipException e) {
                    Log.i(TAG, "无密码解压出错 e = " + e);
                    e.printStackTrace();
                    File file1 = new File(fileName);
                    if (file1.exists()) {
                        file1.delete();
                        refreshAdapter();
                    }
                }
            }
        });

        mMainNoteGv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final File file = mAdapter.mFiles.get(position);
                DeleteNoteDialog deleteNoteDialog = new DeleteNoteDialog(MainActivity.this) {
                    @Override
                    public void sure() {
                        file.delete();
                        mNoteDbDao.deleteZipItem(file.getPath());
                        refreshAdapter();
                    }
                };
                deleteNoteDialog.show();
                return true;
            }
        });
    }

    private void refreshAdapter() {
        if (mMainNoteGv != null) {
            mAdapter = new NoteGridAdapter(this);
            mMainNoteGv.setAdapter(mAdapter);
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

    //    @Override
    //    public void onWindowFocusChanged(boolean hasFocus) {
    //        super.onWindowFocusChanged(hasFocus);
    //        new Handler().postDelayed(new Runnable() {
    //            @Override
    //            public void run() {
    //                refreshAdapter();
    //                System.gc();
    //            }
    //        }, 2000);
    //    }

    @Override
    protected void onStart() {
        super.onStart();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshAdapter();
                System.gc();
            }
        }, 2000);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_btn_note_portrait:
                Intent intent = new Intent(MainActivity.this, NotePageActivity.class);
                //                intent.putExtra("ImgPath","/mnt/sdcard/20170331153532paint.png");
                startActivity(intent);
                break;
            case R.id.main_btn_go_back:
                finish();
                break;
            case R.id.main_btn_setting:
                SettingDialog settingDialog = new SettingDialog(MainActivity.this) {
                    @Override
                    protected void stopPostilService() {
                        Intent intent = new Intent(MainActivity.this,PostilService.class);
                        stopService(intent);
                    }

                    @Override
                    public void startPostilService() {
                        Intent intent = new Intent(MainActivity.this,PostilService.class);
                        startService(intent);
                    }
                };
                settingDialog.show();
                break;
            case R.id.main_btn_help:

                break;

            default:
                break;
        }
    }
}
