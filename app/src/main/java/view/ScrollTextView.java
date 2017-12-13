package view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * @author Administrator
 * @version $Rev$
 * @time 2017-5-20 18:20
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public class ScrollTextView extends TextView {
    public ScrollTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
    }

    @Override
    public void onWindowFocusChanged(boolean focused) {
            super.onWindowFocusChanged(true);
    }

    @Override
    public boolean isFocused() {
        return true;//一直返回true，假装这个控件一直获取着焦点
    }
}
