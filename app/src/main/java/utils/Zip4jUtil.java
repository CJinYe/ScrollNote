package utils;

import android.text.TextUtils;
import android.util.Log;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.File;
import java.util.ArrayList;

import static utils.SDcardUtil.getSavaPath;


/**
 * @author Administrator
 * @version $Rev$
 * @time 2017-5-9 9:14
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public class Zip4jUtil {
    private static String TAG = "Zip4jUtil";

    public static File unzip(File zipFile, String dest, String passwd) throws ZipException {

        ZipFile zFile = new ZipFile(zipFile);  // 首先创建ZipFile指向磁盘上的.zip文件
        zFile.setFileNameCharset("GBK");       // 设置文件名编码，在GBK系统中需要设置
        if (!zFile.isValidZipFile()) {   // 验证.zip文件是否合法，包括文件是否存在、是否为zip文件、是否被损坏等
            throw new ZipException("压缩文件不合法,可能被损坏.");
        }
        File destDir = new File(dest);     // 解压目录
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        if (zFile.isEncrypted()) {
            zFile.setPassword(passwd.toCharArray());  // 设置密码
        }
        //                zFile.setRunInThread(true);
        zFile.extractAll(dest);      // 将文件抽出到解压目录(解压)
        return destDir;
    }

    public static String AddFilesWithAESEncryption(String time, String content,
                                                   String password, ArrayList<File> paths, String compere) {
        String path = null;
        try {

            path = getSavaPath() + "/" + content + "_" + time + "_" + compere + ".zip";

            ZipFile zipFile = new ZipFile(path);

            zipFile.setFileNameCharset("GBK");
            //            ArrayList<File> filesToAdd = new ArrayList<File>();
            //            for (File file : paths) {
            //                filesToAdd.add(file);
            //            }

            for (int i = 0; i < paths.size(); i++) {
                if (!paths.get(i).exists()) {
                    paths.remove(i);
                }
            }

            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);

            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

            parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);

            if (password != null && !TextUtils.isEmpty(password)) {
                parameters.setEncryptFiles(true);
                parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
                parameters.setPassword(password);
            }

            //                                    zipFile.setRunInThread(true);
            zipFile.addFiles(paths, parameters);
            return zipFile.getFile().getPath();
        } catch (ZipException e) {
            e.printStackTrace();
            Log.e("ZipException", "压缩出错 " + e);
            return "保存出错了,错误原因 = " + e;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("ZipException", "压缩出错 " + e);
            return "保存出错了,错误原因 = " + e;
        }
    }
}
