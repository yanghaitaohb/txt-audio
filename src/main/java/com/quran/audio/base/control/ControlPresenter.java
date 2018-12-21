package com.quran.audio.base.control;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.quran.audio.R;
import com.quran.audio.base.BasePresenter;
import com.quran.audio.base.UiView;



/**
 *  Presenter 的基类
 */
public class ControlPresenter<T extends UiView> extends BasePresenter<T> {
    public ControlPresenter(T mView) {
        super(mView);
    }
    protected ProgressDialog progressDialog;

    public void showProgressDialog(boolean isCancel, String content) {
        if (progressDialog == null)
            progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage(content);
        progressDialog.setCancelable(isCancel);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

    }


    public void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

    }
//
//    /**
//     * 设置提示对话框
//     *
//     * @param mContext 上下文
//     * @param value    提示的内容
//     */
//
//    public void ShowHintDialog(Context mContext, String value) {
//        if (TextUtils.isEmpty(value))
//            return;
//        final CustomHintDialog dialog = new CustomHintDialog(mContext, -1);
//        dialog.setMessage(value);
//        dialog.setSubmitButton(mContext.getString(R.string.common_ok), new CustomHintDialog.IButtonOnClickLister() {
//            @Override
//            public void onClickLister() {
//                dialog.dismiss();
//            }
//        });
//        dialog.show();
//    }
//

    /**
     * 访问网络接口回调
     */
    public interface OnNetResultCallback {
        void onSuccess(Object result);

        void onFail(int errorCode, String result);

        void sending(int total, int current);
    }


    public void exit() {
        if (mView instanceof Activity)
            ((Activity) getContext()).finish();
        else Log.e(TAG, "UiView is not Activity");
    }

    // 在状态栏提示分享操作
    private void showNotification(long cancelTime, String text) {
        try {
            Context app = getContext().getApplicationContext();
            NotificationManager nm = (NotificationManager) app
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            final int id = Integer.MAX_VALUE / 13 + 1;
            nm.cancel(id);

            long when = System.currentTimeMillis();
            PendingIntent pi = PendingIntent.getActivity(app, 0, new Intent(),
                    0);
            Notification notification = new Notification.Builder(app)
                    .setContentTitle("")
                    .setContentText(text)
                    .setContentIntent(pi)
                    .setSmallIcon(R.mipmap.icon)
                    .setWhen(when)
                    .build();
//            Notification notification = new Notification(R.mipmap.icon, text, when);
//            PendingIntent pi = PendingIntent.getActivity(app, 0, new Intent(),
//                    0);
//            notification.setLatestEventInfo(app, "", text, pi);
//            notification.flags = Notification.FLAG_AUTO_CANCEL;
            nm.notify(id, notification);

            if (cancelTime > 0) {
                Message msg = new Message();
                msg.what = 1;
                msg.obj = nm;
                msg.arg1 = id;
//                UIHandler.sendMessageDelayed(msg, cancelTime, this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

}
