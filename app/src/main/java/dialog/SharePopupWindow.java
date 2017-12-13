package dialog;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import icox.com.scrawlnote.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import utils.OkHttpUtil;

/**
 * @author 陈锦业
 * @version $Rev$
 * @time 2017-5-31 17:26
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public abstract class SharePopupWindow extends PopupWindow implements View.OnClickListener {

    private final LayoutInflater mLayoutInflater;
    private final EditText mEdtPwd;
    private final EditText mEdtTitle;
    private final ImageButton mButtonShare;
    private final Context mContext;
    private final InputMethodManager mImm;

    public SharePopupWindow(Context context) {
        super(context);
        mContext = context;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = mLayoutInflater.inflate(R.layout.popuwindow_share, null);
        setContentView(view);

        mImm = (InputMethodManager) mContext.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);


        mEdtPwd = (EditText) view.findViewById(R.id.pop_edt_pwd);
        mEdtTitle = (EditText) view.findViewById(R.id.pop_edt_title);
        mButtonShare = (ImageButton) view.findViewById(R.id.pop_but_share);

        mButtonShare.setOnClickListener(this);
        mEdtPwd.setOnClickListener(this);
        mEdtTitle.setOnClickListener(this);

        this.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);

        // 设置PopUpWindow弹出的相关属性
        //        setTouchable(true);
        //        setOutsideTouchable(true);
        //        setFocusable(true);
        //        setBackgroundDrawable(new BitmapDrawable(context.getResources()));
        //        update();

        //        getContentView().setFocusableInTouchMode(true);
        //        getContentView().setFocusable(true);
        setAnimationStyle(R.style.AnimationPreview);
        setBackgroundDrawable(getDrawable());//设置背景透明以便点击外部消失

    }

    /**
     * 生成一个 透明的背景图片
     *
     * @return
     */
    private Drawable getDrawable() {
        ShapeDrawable bgdrawable = new ShapeDrawable(new OvalShape());
        bgdrawable.getPaint().setColor(mContext.getResources().getColor(R.color.transparency));
        return bgdrawable;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.pop_but_share:
//                if (mButtonShare.getProgress() <= 0) {
                    String filePath = saveFile(mEdtTitle.getText().toString(),
                            OkHttpUtil.toUtf8(mEdtPwd.getText().toString()));

                    //            File file = new File(Environment.getExternalStorageDirectory() + "/ScrawlNote/test.zip");
                    File file = new File(filePath);
                    if (!file.exists()) {
                        Toast.makeText(mContext, "文件不存在 " + file.getPath(), Toast.LENGTH_LONG).show();
                        return;
                    }
                    Map<String, String> paserm = new HashMap<String, String>();
                    paserm.put("name", OkHttpUtil.toUtf8("刘志邦大帅哥,HelloWord！"));
                    String url = "http://icoxtech.cn/1WeChatEnterprise/DrawShare/getDrawing.ashx";
                    OkHttpUtil.uploadFile(file, url, paserm, null, new MyStringCallBack());
                    //            postFile(url,paserm,file);
//                } else if (mButtonShare.getProgress() == 100) {
                    hidePopup();
                    dismiss();
//                }
                break;
            case R.id.pop_edt_pwd:
                mEdtPwd.setFocusable(true);
                mImm.toggleSoftInput(1000, InputMethodManager.HIDE_NOT_ALWAYS);
                break;
            case R.id.pop_edt_title:
                mEdtTitle.setFocusable(true);
                mImm.toggleSoftInput(1000, InputMethodManager.HIDE_NOT_ALWAYS);
                break;

            default:
                break;
        }


    }


    public abstract String saveFile(String name, String pwd);

    public abstract void hidePopup();

    class MyStringCallBack extends StringCallback {
        @Override
        public void onError(Call call, Exception e, int id) {
//            mButtonShare.setProgress(-1);
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
//            mButtonShare.setProgress((int) (progress * 100));
        }

        @Override
        public boolean validateReponse(Response response, int id) {
            Log.i("MainActity11", response.isSuccessful() + "上传中 validateReponse = " + response);
            return super.validateReponse(response, id);
        }
    }

    private void postFile(String url, Map<String, String> paserm, File file) {
        OkHttpClient okHttpClient = new OkHttpClient();
        MultipartBody.Builder requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
        if (file != null) {
            RequestBody body = RequestBody.create(MediaType.parse("application/zip"), file);
            requestBody.addFormDataPart("zipImage", file.getName(), body);
        }

        if (paserm != null) {
            for (String key : paserm.keySet()) {
                requestBody.addFormDataPart(key, paserm.get(key));
            }
        }

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody.build())
                .tag(mContext)
                .build();

        okHttpClient.newBuilder()
                .readTimeout(5000, TimeUnit.MILLISECONDS)
                .build()
                .newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.i("MainActity11", "onFailure IOException = " + e);
                        Log.i("MainActity11", "onFailure IOException = " + call.request().toString());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Log.i("MainActity11", "onResponse response = " + response.toString());
                    }


                });
    }

}
