package utils;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Administrator
 * @version $Rev$
 * @time 2017-5-6 9:26
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public class SDcardUtil {

    public static boolean ExistSDCard() {
        if (android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED)) {
            return true;
        } else
            return false;
    }

    public static String getDiskCacheDir(Context context) {
        String cachePath = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return cachePath;
    }

    //删除文件夹和文件夹里面的文件
    public static void deleteDir(String pPath) {
        File dir = new File(pPath);
        deleteDirWihtFile(dir);
    }

    public static void deleteDirWihtFile(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory())
            return;
        for (File file : dir.listFiles()) {
            if (file.isFile())
                file.delete(); // 删除所有文件
            else if (file.isDirectory())
                deleteDirWihtFile(file); // 递规的方式删除文件夹
        }
        dir.delete();// 删除目录本身
    }

    public static String getSavaPath() throws Exception {
        String sdPath = null;
        if (SDcardUtil.ExistSDCard()) {
            sdPath = Environment.getExternalStorageDirectory() + "/ScrawlNote";

            File f = new File(sdPath);
            if (!f.exists()) {
                f.mkdirs();
            }
        } else {
            sdPath = getExternalStorageDirectory() + "/ScrawlNote";
            File f = new File(sdPath);
            if (!f.exists()) {
                f.mkdirs();
            }
        }

        return sdPath;
    }

    /**
     * 书本笔记保存的路径
     *
     * @return
     * @throws Exception
     */
    public static String getBookNoteSavaPath() throws Exception {
        String sdPath = null;
        if (SDcardUtil.ExistSDCard()) {
            sdPath = Environment.getExternalStorageDirectory() + "/ScrawlNote/BookNote";
            File f = new File(sdPath);
            if (!f.exists()) {
                f.mkdirs();
            }
        } else {
            sdPath = getExternalStorageDirectory() + "/ScrawlNote/BookNote";
            File f = new File(sdPath);
            if (!f.exists()) {
                f.mkdirs();
            }
        }

        return sdPath;
    }

    public static String getPicturesPath() throws Exception {
        String sdPath = null;
        if (SDcardUtil.ExistSDCard()) {
            sdPath = Environment.getExternalStorageDirectory() + "/Pictures";
            File f = new File(sdPath);
            if (!f.exists()) {
                f.mkdirs();
            }
        } else {
            sdPath = getExternalStorageDirectory() + "/Pictures";
            File f = new File(sdPath);
            if (!f.exists()) {
                f.mkdirs();
            }
        }
        return sdPath;
    }

    public static File getPicturesCacheFile() throws Exception {
        String sdPath = null;
        File f;
        if (SDcardUtil.ExistSDCard()) {
            sdPath = Environment.getExternalStorageDirectory() + "/ScrawlNote/Caches";

            f = new File(sdPath);
            if (!f.exists()) {
                f.mkdirs();
            }
        } else {
            sdPath = getExternalStorageDirectory() + "/ScrawlNote/Caches";
            f = new File(sdPath);
            if (!f.exists()) {
                f.mkdirs();
            }
        }

        return f;
    }

    public static String getPicturesCachePath() throws Exception {
        String sdPath = null;
        File f;
        if (SDcardUtil.ExistSDCard()) {
            sdPath = Environment.getExternalStorageDirectory() + "/ScrawlNote/Caches";
        } else {
            sdPath = getExternalStorageDirectory() + "/ScrawlNote/Caches";
        }

        return sdPath;
    }

    private static String getExternalStorageDirectory() throws Exception {
        String dir = new String();
        Runtime runtime = Runtime.getRuntime();
        Process proc = runtime.exec("mount");
        InputStream is = proc.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        String line;
        BufferedReader br = new BufferedReader(isr);
        while ((line = br.readLine()) != null) {
            // System.out.println(line);
            if (line.contains("secure"))
                continue;
            if (line.contains("asec"))
                continue;

            if (line.contains("fat")) {
                String columns[] = line.split(" ");
                if (columns != null && columns.length > 1) {
                    dir = dir.concat(columns[1]);
                    break;
                }
            } else if (line.contains("fuse")) {
                String columns[] = line.split(" ");
                if (columns != null && columns.length > 1) {
                    dir = dir.concat(columns[1]);
                    break;
                }
            }
        }
        return dir;
    }

    /**
     * 判断手机是否ROOT
     */
    public static boolean isRoot() {

        boolean root = false;

        try {
            if ((!new File("/system/bin/su").exists())
                    && (!new File("/system/xbin/su").exists())) {
                root = false;
            } else {
                root = true;
            }

        } catch (Exception e) {
        }

        return root;
    }
}
