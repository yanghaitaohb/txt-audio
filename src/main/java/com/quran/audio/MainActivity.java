package com.quran.audio;

import android.app.Activity;
import android.media.AudioFormat;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.quran.audio.tts.HciCloudSysHelper;
import com.quran.audio.tts.HciTtsSynthHelper;
import com.quran.audio.util.AudioParam;
import com.quran.audio.util.AudioPlayer;
import com.quran.audio.util.PlayState;
import com.sinovoice.hcicloudsdk.common.HciErrorCode;
import com.sinovoice.hcicloudsdk.common.utils.Md5Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class MainActivity extends Activity implements View.OnClickListener{
    private TextView txt;
    private Spinner mSpinner;
    private AudioPlayer mAudioPlayer; // 播放器
    private HciCloudSysHelper mHciCloudSysHelper;
    private BaseHandler mHandler;
    volatile boolean isInit = false;
    volatile boolean isPlay = false;
    static final String TAG = MainActivity.class.getSimpleName();
    String pcmPath = Environment.getExternalStorageDirectory() + "/quran/pcmfile/";
    String filePath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txt = (TextView) findViewById(R.id.txt);
        txt.setOnClickListener(this);

        mSpinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.test_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
        mSpinner.setOnItemSelectedListener(spinnerSelectedListener);

        new Thread(new Runnable() {
            @Override
            public void run() {
                initTts();
            }
        }).start();
        mHandler = new BaseHandler(this);
        mAudioPlayer = new AudioPlayer(mHandler);

        // 获取音频参数
        AudioParam audioParam = getAudioParam();
        mAudioPlayer.setAudioParam(audioParam);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txt:
                Log.d(TAG, "onClick: ===========1111");
                if (mAudioPlayer.mPlayState == PlayState.MPS_PLAYING) {
                    isPlay = false;
                    stop();
                    return;
                }
                String str = txt.getText().toString();
                if (TextUtils.isEmpty(str)) {
//                    Toast.makeText(MainActivity.this, "请输入合成内容", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onClick: 请输入合成内容");
                    return;
                }
                if (!isInit) {
                    Toast.makeText(MainActivity.this, "正在初始化，请稍后", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onClick: 正在初始化，请稍后");
                    return;
                }
//                synth(str);
                String name = Md5Util.MD5(str);
                synth(str, name + "");
                break;
        }
    }
    private void handleMessage(Message msg) {
        Log.d(TAG, "handleMessage: ========= " + msg.obj);
        switch (msg.what) {
            case 3:
                synth(txt.getText().toString(), "0");
                break;
        }
        if ((int)msg.obj == 1 && isPlay){
            play();
        }
    }


    /*
     * 获得PCM音频数据参数
     */
    public AudioParam getAudioParam() {
        AudioParam audioParam = new AudioParam();
        audioParam.mFrequency = 16000;
        // CHANNEL_CONFIGURATION_STEREO双声道
        audioParam.mChannel = AudioFormat.CHANNEL_CONFIGURATION_MONO;
        audioParam.mSampBit = AudioFormat.ENCODING_PCM_16BIT;
        return audioParam;
    }

    private void synth(String string, String name) {
        // TODO Auto-generated method stub
        filePath = pcmPath + name + ".pcm";
        boolean bool = HciTtsSynthHelper.getInstance().synth(string, filePath);
        Log.d(TAG, "synth: =======222 "+bool);
        if (bool) {
            Toast.makeText(this, "合成成功", Toast.LENGTH_SHORT).show();
            initLogic(filePath);
            play();
            isPlay = true;
        } else {
//            synth(string, name);
        }
    }

    public void pause() {
        mAudioPlayer.pause();
    }

    public void stop() {
        if (mAudioPlayer != null)
            mAudioPlayer.stop();
    }
    public void play() {
        mAudioPlayer.play();
    }

    public void initLogic(String filePath) {


        // 把音频文件从assets目录下拷贝到sd卡
        // copyPcmToSdcard("testmusic.pcm", filePath);

        // 获取音频数据
        byte[] data = getPCMData(filePath);
        mAudioPlayer.setDataSource(data);
        // 音频源就绪
        mAudioPlayer.prepare();
        if (data == null) {
            Toast.makeText(this, "文件不存在", Toast.LENGTH_SHORT).show();
        }
    }

    /*
     * 获得PCM音频数据
     */
    public byte[] getPCMData(String filePath) {
        File file = new File(filePath);
        if (file == null) {
            return null;
        }
        FileInputStream inStream;
        try {
            inStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        byte[] data_pack = null;
        if (inStream != null) {
            long size = file.length();

            data_pack = new byte[(int) size];
            try {
                inStream.read(data_pack);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }
        }
        return data_pack;
    }


    /**
     * 初始化
     */
    private void initTts() {
        // TODO Auto-generated method stub
        mHciCloudSysHelper = HciCloudSysHelper.getInstance();
        int errCode = mHciCloudSysHelper.init(this);
        Log.e(TAG, "----errCode1 :" + errCode);
        if (errCode != HciErrorCode.HCI_ERR_NONE) {
            return;
        }
        errCode = HciTtsSynthHelper.init(this);
        Log.e(TAG, "----errCode2 :" + errCode);
        if (errCode != HciErrorCode.HCI_ERR_NONE) {
            return;
        }
        isInit = true;
    }
    protected static class BaseHandler extends Handler {
        private final WeakReference<MainActivity> mObjects;

        public BaseHandler(MainActivity mainActivity) {
            mObjects = new WeakReference<MainActivity>(mainActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity mObject = mObjects.get();
            if (mObject != null)
                mObject.handleMessage(msg);
        }
    }

    /**
     * TTS和SYS反初始化
     */
    private void releaseTts() {
        // TODO Auto-generated method stub
        int errCode = HciTtsSynthHelper.getInstance().release();
        if (errCode != HciErrorCode.HCI_ERR_NONE) {
            return;
        }
        mHciCloudSysHelper.release();

    }
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();

        releaseTts();
        mAudioPlayer.release();
    }
    private final Spinner.OnItemSelectedListener spinnerSelectedListener = new Spinner.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };
}
