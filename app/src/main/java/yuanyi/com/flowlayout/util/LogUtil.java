package yuanyi.com.flowlayout.util;

import android.util.Log;

/**
 * Created by admin on 2016/6/7.
 */
public class LogUtil {
    private LogUtil() {}

    private static final String TAG = "tag";
    private static boolean isLog = true;

    public static void i(String tag, String info) {
        if (isLog) {
            Log.i(tag, info);
        }
    }

    public static void i(String str) {
        i(TAG, str);
    }

    public static void i(Object obj) {
        if (obj == null) {
            i("NULL");
            return;
        }
        i(obj.toString());
    }

    public static void e(String tag, String err, Throwable t) {
        if (isLog) {
            Log.e(tag, err, t);
        }
    }

    public static void e(String tag, String err) {
        e(tag, err, null);
    }

    public static void e(String err, Throwable t) {
        e(TAG, err, t);
    }

    public static void e(String err) {
        e(TAG, err, null);
    }
}
