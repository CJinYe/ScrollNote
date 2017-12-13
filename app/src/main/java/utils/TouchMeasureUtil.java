package utils;

import android.view.MotionEvent;

/**
 * @author 陈锦业
 * @version $Rev$
 * @time 2017-7-8 15:11
 * @des ${触摸测量类}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public class TouchMeasureUtil {
    /**
     * 三点触摸是否在一个范围内
     *
     * @param event
     * @return
     */
    public static boolean ThreeTouchDistance(MotionEvent event) {
        int count = event.getPointerCount();
        if (count > 1) {

            float x1 = event.getX(0);
            float x2 = event.getX(1);
            float y1 = event.getY(0);
            float y2 = event.getY(1);

            double distance23 = 0;
            double distance13 = 0;
            if (count > 2) {
                float x3 = event.getX(2);
                float y3 = event.getY(2);
                distance23 = Math.sqrt((Math.pow(x2 - x3, 2) + Math.pow(y2 - y3, 2)));
                distance13 = Math.sqrt((Math.pow(x1 - x3, 2) + Math.pow(y1 - y3, 2)));
            }
            double distance12 = Math.sqrt((Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2)));

            if (distance12 < 400 && distance23 < 400 && distance13 < 400 && distance13 != 0 && distance23 != 0) {
                return true;
            }
        }

        return false;
    }
}
