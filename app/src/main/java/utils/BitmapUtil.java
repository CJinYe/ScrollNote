package utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * @author 陈锦业
 * @version $Rev$
 * @time 2017-7-11 16:04
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public class BitmapUtil {
    /**
     * 旋转图片
     * @param bitmap
     * @param rotate
     * @param isRecycle
     * @return
     */
    public static Bitmap rotate(Bitmap bitmap, int rotate, boolean isRecycle) {
        Matrix matrix = new Matrix();
        matrix.setRotate(rotate, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);
        Bitmap bitmap1 = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (isRecycle)
            bitmap.recycle();
        return bitmap1;
    }

}
