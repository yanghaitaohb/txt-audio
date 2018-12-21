package com.quran.audio.util;

import android.content.Context;
import android.widget.Toast;


/**
 * 提示工具类
 * 
 * @author Administrator
 * 
 */
public class PromptManager {

	private static Toast toast;

    public static void showToast(Context context, int toastId) {
        showToast(context, toastId, Toast.LENGTH_SHORT);
    }

    public static void showToast(Context context, int toastId, int duration) {
        showToast(context, context.getString(toastId), duration);
    }

    public static void showToast(Context context, String text) {
        showToast(context, text, Toast.LENGTH_SHORT);
	}

    public static void showToast(Context context,  String text, int duration) {
        if (toast == null) {
            toast = Toast.makeText(context, text, duration);
        }
        toast.setText(text);
        toast.setDuration(duration);
        toast.show();
    }
}
