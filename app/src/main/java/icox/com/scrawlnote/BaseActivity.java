package icox.com.scrawlnote;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

/**
 * @author 陈锦业
 * @version $Rev$
 * @time 2017-11-8 14:29
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public class BaseActivity extends FragmentActivity implements ViewTreeObserver.OnGlobalLayoutListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initWindow();
        findViewById(android.R.id.content).getViewTreeObserver().addOnGlobalLayoutListener(this);
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
    }

    @Override
    public void onGlobalLayout() {
        initWindow();
        findViewById(android.R.id.content).getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }
}
