package icox.com.scrawlnote;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;

/**
 * @author Administrator
 * @version $Rev$
 * @time 2017-5-22 19:33
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public class MyApplocation extends Application {
    private int result;
    private Intent intent;
    private MediaProjectionManager mMediaProjectionManager;
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
//        CrashHandler crashHandler = CrashHandler.getInstance();
//        crashHandler.init(getApplicationContext());
        context = getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }

    public int getResult() {
        return result;
    }

    public Intent getIntent() {
        return intent;
    }

    public MediaProjectionManager getMediaProjectionManager() {
        return mMediaProjectionManager;
    }

    public void setResult(int result1) {
        result = result1;
    }

    public void setIntent(Intent intent1) {
        intent = intent1;
    }

    public void setMediaProjectionManager(MediaProjectionManager MediaProjectionManager) {
        mMediaProjectionManager = MediaProjectionManager;
    }
}
