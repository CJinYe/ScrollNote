package utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.support.v4.util.LruCache;
import android.util.Log;

import java.io.File;

import constants.NoteBeanConf;

/**
 * @author Administrator
 * @version $Rev$
 * @time 2017-5-9 14:17
 * @des ${图片缓存}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public class CacheBitmapUtil {
    /**
     * 图片缓存技术的核心类，用于缓存所有下载好的图片，在程序内存达到设定值时会将最少最近使用的图片移除掉。
     */
    public static LruCache<String, Bitmap> mMemoryCache;

    private static void createCache() {
        if (mMemoryCache == null) {
            int maxMemory = (int) Runtime.getRuntime().maxMemory();
            int cacheSize = (int) (maxMemory / 1.1);
            Log.i("createCache", "max = " + maxMemory + "  , size = " + cacheSize);
            mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
                @Override
                protected int sizeOf(String key, Bitmap value) {
                    return value.getByteCount();
                }

                @Override
                protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                    super.entryRemoved(evicted, key, oldValue, newValue);
                    if (evicted && oldValue != null) {

                        if (oldValue != NoteBeanConf.nullBitmap) {
                            //                            oldValue.recycle();
                            Log.i("createCache", "evicted = " + evicted + "  , oldValue = " + oldValue.isRecycled());
                        } else {
                            Log.i("createCache", "相等 = " + newValue + "  , newValue = ");
                        }
                        //                        oldValue.recycle();
                        //                        oldValue = null;
                        //                        System.gc();
                    }
                }
            };
        }
    }

    /**
     * 从LruCache中获取一张图片，如果不存在就返回null。
     *
     * @param key LruCache的键，这里传入图片的URL地址。
     * @return 对应传入键的Bitmap对象，或者null。
     */
    public static Bitmap getBitmapFromMemoryCache(String key) {
        createCache();
        return mMemoryCache.get(key);
    }

    /**
     * 将一张图片存储到LruCache中。
     *
     * @param key    LruCache的键，这里传入图片的URL地址。
     * @param bitmap LruCache的键，这里传入从网络上下载的Bitmap对象。
     */
    public static void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        createCache();
        if (getBitmapFromMemoryCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public static void deleteBitmapToCache(String key) {
        createCache();
        if (getBitmapFromMemoryCache(key) != null) {
            Bitmap bitmap = mMemoryCache.remove(key);
            Log.i("deleteBitmapToCache", "  d = " + mMemoryCache.size());
            if (bitmap != null) {
                Log.i("deleteBitmapToCache", "  recycle = " + mMemoryCache.size());
                bitmap.recycle();
            }
            System.gc();
        }
    }

    public static Bitmap createBitmap(String path) {
        createCache();
        if (getBitmapFromMemoryCache(path) == null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_4444;
            options.inPurgeable = true;
            options.inInputShareable = true;
            Bitmap bitmap = null;

            if (Runtime.getRuntime().maxMemory() / 1.2 <= Runtime.getRuntime().totalMemory()) {
                //            if (Runtime.getRuntime().maxMemory() / 1.13 <= mMemoryCache.size()) {
                Log.e("createCache", " size = " + mMemoryCache.size());
                //                addBitmapToMemoryCache(path, NoteBeanConf.createNullBitmap(screenWidth, screenHeight));
            } else {
                bitmap = BitmapFactory.decodeFile(path, options);
                addBitmapToMemoryCache(path, bitmap);
            }
            return bitmap;
        } else {
            Log.d("createCache", " size = " + mMemoryCache.size());
            return getBitmapFromMemoryCache(path);
        }

    }

    public static Bitmap createBitmap(String path, int width) {
        createCache();
        if (getBitmapFromMemoryCache(path) == null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_4444;
            options.inPurgeable = true;
            options.inInputShareable = true;
            Bitmap bitmap = null;

            if (Runtime.getRuntime().maxMemory() / 1.2 <= Runtime.getRuntime().totalMemory()) {
                //            if (Runtime.getRuntime().maxMemory() / 1.13 <= mMemoryCache.size()) {
                Log.e("createCache", " size = " + mMemoryCache.size());
                //                addBitmapToMemoryCache(path, NoteBeanConf.createNullBitmap(screenWidth, screenHeight));
            } else {
                bitmap = BitmapFactory.decodeFile(path, options);
                addBitmapToMemoryCache(path, bitmap);
            }
            return bitmap;
        } else {
            Log.d("createCache", " size = " + mMemoryCache.size());
            Bitmap bitmap = getBitmapFromMemoryCache(path);
            Matrix matrix = new Matrix();
            Log.d("createCache", " bitmap.getWidth() = " + bitmap.getWidth()+"    width = "+width);
            if (bitmap.getWidth() > width) {
                matrix.setRotate(270);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
                addBitmapToMemoryCache(path, bitmap);
            } else {
                matrix.setRotate(90);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
                addBitmapToMemoryCache(path, bitmap);
            }
            Log.i("createCache", " bitmap.getWidth() = " + bitmap.getWidth()+"    width = "+width);
            return bitmap;
        }

    }

    public static void destoryCacheBitmap(File[] files) {
        if (mMemoryCache != null) {
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    Bitmap bitmap = mMemoryCache.remove(files[i].getPath());
                    if (bitmap != null) {
                        bitmap.recycle();
                    }
                }
            }
            mMemoryCache.evictAll();
            mMemoryCache = null;
            System.gc();
        }
    }

    public static int getBitmapBackground(Bitmap bitmap) {
        int pixelColor = bitmap.getPixel(0, 0);
        int A = Color.alpha(pixelColor);
        int R = Color.red(pixelColor);
        int G = Color.green(pixelColor);
        int B = Color.blue(pixelColor);
        int backgroundColor = Color.argb(A, R, G, B);
        return backgroundColor;
    }
}
