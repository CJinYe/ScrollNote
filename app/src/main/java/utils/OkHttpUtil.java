package utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.BitmapCallback;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author 陈锦业
 * @version $Rev$
 * @time 2017-5-28 18:07
 * @des ${网络请求}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public class OkHttpUtil {
    public static void uploadFile(File file, String url, Map<String, String> params
            , Map<String, String> headers, StringCallback stringCallback) {

        if (headers != null) {
            OkHttpUtils.post()//
                    .addFile("mFile", params.get("name"), file)//
                    .url(url)//
                    .params(params)
                    .headers(headers)
                    .build()
                    .execute(stringCallback);
        } else {

            OkHttpUtils.post()//
                    .addFile("icox", "icox", file)//
                    .url(url)//
                    .params(params)
                    .build()
                    .execute(stringCallback);
        }
    }

    private static StringBuffer getRequestData(Map<String, String> params) {
        StringBuffer stringBuffer = new StringBuffer();
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                stringBuffer.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            stringBuffer.deleteCharAt(stringBuffer.length() - 1);    //删除最后的一个"&"
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuffer;
    }

    public static void getHttp(String url,StringCallback callback){
        OkHttpUtils
                .post()
                .url(url)
                .build()
                .execute(callback);
    }

    public static void getQRCode(Context context, String url, BitmapCallback callBack) {
        OkHttpUtils.get()
                .url(url)
                .tag(context)
                .build()
                .connTimeOut(20000)
                .readTimeOut(20000)
                .writeTimeOut(20000)
                .execute(callBack);
    }


    public static void uploadFile(File file,String url) {
        MediaType MEDIA_TYPE_MARKDOWN
                = MediaType.parse("application/zip; charset=utf-8");
        OkHttpClient okClient = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("name","刘志邦帅哥")
                .build();

        Request request = new Request.Builder()
                .url(url)
//                .post(RequestBody.create(MEDIA_TYPE_MARKDOWN,file))

//                .post(body)
                .build();
        okClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                    Log.e("MainActity11", "上传出错 e = " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i("MainActity11", "onResponse onResponse = " + response.toString());
            }
        });
    }

    public static String toUtf8(String str) {
        String result = null;
        try {
            result = new String(str.getBytes("UTF-8"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    public static String toUTF8(String str) {
        String result = str;
        try {
            result = URLEncoder.encode(result,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 检测网络是否连接
     * @return
     */
    public static boolean checkNetworkState(Context context) {
        boolean flag = false;
        //得到网络连接信息
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //去进行判断网络是否连接
        if (manager.getActiveNetworkInfo() != null) {
            flag = manager.getActiveNetworkInfo().isAvailable();
        }
//        if (!flag) {
//            setNetwork();
//        } else {
//            isNetworkAvailable();
//        }

        return flag;
    }
}
