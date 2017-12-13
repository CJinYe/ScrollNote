package backups;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * @author Administrator
 * @version $Rev$
 * @time 2017-4-27 14:44
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public class NoScrollViewpager extends ViewPager {

    public NoScrollViewpager(Context context) {
        super(context);
    }

    public NoScrollViewpager(Context context, AttributeSet attributeSet){
        super(context,attributeSet);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

}
