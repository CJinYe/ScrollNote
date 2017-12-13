package constants;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.SparseArray;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;

import bean.TuyaViewBean;

import static com.wnafee.vector.MorphButton.TAG;

/**
 * @author Administrator
 * @version $Rev$
 * @time 2017-5-8 9:21
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public class NoteBeanConf {
    public static SparseArray<TuyaViewBean> tuyaBeanList = new SparseArray<>();
    public static SparseArray<Bitmap> fileBitmaps = new SparseArray<>();

    /**
     * 保存的上一页背景颜色
     */
    public static int saveNoteBgColor = -1;
    /**
     * 保存的上一页画笔颜色
     */
    public static int saveNotePaintColor = -1;
    /**
     * 保存的上一页画笔大小
     */
    public static int saveNotePaintSize = -1;
    /**
     * 保存的上一页画笔样式
     */
    public static int saveNotePaintStyle = -1;

    public static Bitmap nullBitmap = null;
    private static Bitmap saveBitmap = null;
    private static Bitmap fileBitmap = null;

    public static Bitmap createNullBitmap(int screenWidth, int screenHeight) {
        if (nullBitmap == null || nullBitmap.isRecycled()) {
            nullBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_4444);
        }
        return nullBitmap;
    }

    public static Bitmap createSaveBitmap(int screenWidth, int screenHeight) {
        if (saveBitmap == null || saveBitmap.isRecycled()) {
            saveBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_4444);
        }
        return saveBitmap;
    }

    public static Bitmap createFileBitmap(String path, int key) {
        Bitmap bitmap;
        if (fileBitmaps.get(key) == null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_4444;
            options.inPurgeable = true;
            options.inInputShareable = true;
            bitmap = BitmapFactory.decodeFile(path, options);
            fileBitmaps.put(key, bitmap);
        } else {
            bitmap = fileBitmaps.get(key);
        }
        return bitmap;
    }


    public static Bitmap createBitmap(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_4444;
        options.inPurgeable = true;
        options.inInputShareable = true;
        return BitmapFactory.decodeFile(null, options);
    }

    private static Bitmap mBitmap;

    public static Bitmap picassoGetBitmap(Context context, File desFile) {
        Picasso.with(context).load(desFile).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                mBitmap = bitmap;
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                Log.i(TAG, "errorDrawable = " + errorDrawable);
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });

        return mBitmap;
    }


}
