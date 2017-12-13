package broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import constants.Constants;
import service.PostilService;
import utils.SpUtils;

public class BootBroadcastReceiver extends BroadcastReceiver {

    static final String ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(ACTION)) {
            SpUtils spUtils = new SpUtils(context);
            boolean isOpen = spUtils.getBoolean(Constants.ON_OFF_POSTIL, true);
            if (isOpen) {
                Intent intent1 = new Intent(context, PostilService.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startService(intent1);
            }
        }
    }
}