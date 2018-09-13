package com.android.king.xmppdemo.util;

import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author：King
 * @time: 2018/9/13 19:27
 */
public class FileUtil {

    public static byte[] getFileBytes(File file) throws IOException {
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(file));
            int bytes = (int) file.length();
            byte[] buffer = new byte[bytes];
            int readBytes = bis.read(buffer);
            if (readBytes != buffer.length) {
                throw new IOException("Entire file not read");
            }
            return buffer;
        } finally {
            if (bis != null) {
                bis.close();
            }
        }
    }


    public static String saveAvatarToFile(byte[] buf, String fileName) {
        String filePath = Environment.getExternalStorageDirectory() + "/MicroChat/avatar";

        return byte2File(buf, filePath, fileName);
    }

    public static boolean isAvatarExist(String fileName) {
        String filePath = Environment.getExternalStorageDirectory() + "/MicroChat/avatar/" + fileName + ".png";

        File file = new File(filePath);
        if (!file.exists()) {
            return false;
        } else if (System.currentTimeMillis() - file.lastModified() > 3 * 24 * 60 * 60 * 1000) {//缓存三天后失效
            file.delete();
            return false;
        }
        return true;
    }

    public static String getAvatarCache(String fileName) {
        return Environment.getExternalStorageDirectory() + "/MicroChat/avatar/" + fileName + ".png";
    }

    public static String byte2File(byte[] buf, String filePath, String fileName) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {
            File dir = new File(filePath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String fullPath = filePath + File.separator + fileName;
            file = new File(fullPath);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(buf);
            return fullPath;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

}
