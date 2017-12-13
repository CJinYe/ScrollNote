package utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.os.Build;

import kr.co.namee.permissiongen.PermissionGen;

/**
 * @author 陈锦业
 * @version $Rev$
 * @time 2017-6-3 10:11
 * @des ${6.0系统申请权限}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public class PermissionUtil {
    public static void intPermission(Context context) {
        if (Build.VERSION.SDK_INT >= 22) {
            String[] permissions = new String[]{
                    Manifest.permission.READ_FRAME_BUFFER,
                    Manifest.permission.RECEIVE_BOOT_COMPLETED,
                    Manifest.permission.SYSTEM_ALERT_WINDOW,
                    Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE

            };
            PermissionGen.needPermission((Activity) context, 100, permissions);
        }

    }
}
