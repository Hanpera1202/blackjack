package com.ivoid.bj;

import android.graphics.Bitmap;
import java.util.HashMap;

/**
 * Created on 2015/10/31.
 */
public class ImageCache {
    private static HashMap cache = new HashMap();

    public static Bitmap getImage(String key) {
        if (cache.containsKey(key)) {
            return (Bitmap) cache.get(key);
        }
        return null;
    }

    public static void setImage(String key, Bitmap image) {
        cache.put(key, image);
    }
}
