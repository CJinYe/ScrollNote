package icox.com.scrawlnote.NotePostil;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Toast;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;

import adapter.NoteGridAdapter;
import butterknife.ButterKnife;
import butterknife.InjectView;
import db.NoteDbDao;
import dialog.DeleteNoteDialog;
import dialog.PwdDialog;
import dialog.SettingDialog;
import icox.com.scrawlnote.BaseActivity;
import icox.com.scrawlnote.NotePageActivity;
import icox.com.scrawlnote.R;
import service.PostilService;
import utils.PermissionUtil;
import utils.SDcardUtil;
import utils.Zip4jUtil;

/**
 * @author 陈锦业
 * @version $Rev$
 * @time 2017-11-10 14:37
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public class NotePostilMain extends BaseActivity implements View.OnClickListener {
    @InjectView(R.id.main_btn_note_portrait)
    ImageButton mMainBtnNotePortrait;
    @InjectView(R.id.main_note_gv)
    GridView mMainNoteGv;
    @InjectView(R.id.main_btn_go_back)
    ImageButton mMainBtnGoBack;
    @InjectView(R.id.main_btn_setting)
    ImageButton mMainBtnSetting;
    @InjectView(R.id.main_btn_help)
    ImageButton mMainBtnHelp;

    private String TAG = "MainActivity";
    private NoteGridAdapter mAdapter;
    private NoteDbDao mNoteDbDao;
    private Context mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        mContext = this;
        initView();
        mNoteDbDao = new NoteDbDao(this);
        //6.0系统申请权限
        PermissionUtil.intPermission(this);

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

    private void initView() {
        mAdapter = new NoteGridAdapter(this);
        mMainNoteGv.setAdapter(mAdapter);

        mMainNoteGv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final File file = mAdapter.mFiles.get(position);
                final String fileName = file.getPath().replaceAll(".zip", "");
                final Intent intent = new Intent(mContext, NotePageActivity.class);

                try {
                    final ZipFile zipFile = new ZipFile(file);
                    //判断文件是否加密
                    if (zipFile.isEncrypted()) {
                        PwdDialog pwdDialog = new PwdDialog(mContext, file.getName()) {
                            @Override
                            public void sure(String pwd) {
                                try {
                                    File fileUn = Zip4jUtil.unzip(file, fileName, pwd);
                                    intent.putExtra("FilePath", fileUn.getPath());
                                    startActivity(intent);
                                } catch (ZipException e) {
                                    e.printStackTrace();
                                    Log.i(TAG, "解压出错 , e = " + e);
                                    Toast.makeText(mContext, "密码有误！", Toast.LENGTH_SHORT).show();
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
                DeleteNoteDialog deleteNoteDialog = new DeleteNoteDialog(mContext) {
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
                Intent intent = new Intent(mContext, NotePageActivity.class);
                startActivity(intent);
                break;
            case R.id.main_btn_go_back:
                finish();
                break;
            case R.id.main_btn_setting:
                SettingDialog settingDialog = new SettingDialog(mContext) {
                    @Override
                    protected void stopPostilService() {
                        Intent intent = new Intent(mContext,PostilService.class);
                        stopService(intent);
                    }

                    @Override
                    public void startPostilService() {
                        Intent intent = new Intent(mContext,PostilService.class);
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
