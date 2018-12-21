package com.quran.audio.base.control;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import com.quran.audio.MyApplication;
import com.quran.audio.R;
import com.quran.audio.base.BaseActivity;
import com.quran.audio.ui.customView.NumberProgressBar;

public abstract class ControlActivity extends BaseActivity {
    private View view;

    @Override
    protected void onResume() {
        super.onResume();
        NumberProgressBar progressBar = (NumberProgressBar) super.findViewById(R.id.progressbar);
        progressBar.setMax(100);
        bindView(progressBar);
    }
    private void bindView(NumberProgressBar progressBar) {
        MyApplication.from(this).bindProgressView(progressBar);
    }

    @Override
    protected void onSetContentView() {
        setContentView(R.layout.activity_base);

        FrameLayout contentView = (FrameLayout) super.findViewById(R.id.fl_content);
        view = View.inflate(this, getContentViewResource(), null);
        contentView.addView(view);
        onViewInit();
    }

    @Override
    public View findViewById(int id) {
        return view.findViewById(id);
    }

    public  boolean hideInputMethod(Context context, View view) {
        if (context == null || view == null) {
            return false;
        }
        InputMethodManager imm = (InputMethodManager) context .getSystemService(Context.INPUT_METHOD_SERVICE);
        return imm != null && imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
