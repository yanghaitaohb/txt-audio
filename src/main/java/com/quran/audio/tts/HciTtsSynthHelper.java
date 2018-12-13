package com.quran.audio.tts;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.sinovoice.hcicloudsdk.api.tts.HciCloudTts;
import com.sinovoice.hcicloudsdk.common.HciErrorCode;
import com.sinovoice.hcicloudsdk.common.Session;
import com.sinovoice.hcicloudsdk.common.tts.ITtsSynthCallback;
import com.sinovoice.hcicloudsdk.common.tts.TtsConfig;
import com.sinovoice.hcicloudsdk.common.tts.TtsInitParam;
import com.sinovoice.hcicloudsdk.common.tts.TtsSynthResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class HciTtsSynthHelper {
    private static final String TAG = HciTtsSynthHelper.class.getSimpleName();
    private static FileOutputStream mFos;

    private static HciTtsSynthHelper hciTtsSynthHelper = null;

    /**
     * 构造函数私有化
     */
    private HciTtsSynthHelper() {
    }

    /**
     * 实例化对象
     *
     * @return
     */
    public static HciTtsSynthHelper getInstance() {
        if (hciTtsSynthHelper == null) {
            return new HciTtsSynthHelper();
        }
        return hciTtsSynthHelper;
    }

    /**
     * TTS初始化函数
     *
     * @return
     */
    public static int init(Context context) {
        TtsInitParam ttsInitParam = new TtsInitParam();
        String dataPath = context.getFilesDir().getAbsolutePath().replace("files", "lib");

        ttsInitParam.addParam(TtsInitParam.PARAM_KEY_DATA_PATH, dataPath);
        ttsInitParam.addParam(TtsInitParam.PARAM_KEY_FILE_FLAG, TtsInitParam.HCI_TTS_FILE_FLAG_ANDROID_SO);
        ttsInitParam.addParam(TtsInitParam.PARAM_KEY_INIT_CAP_KEYS, getKey());
        Log.i(TAG, "hciTtsInit config :" + ttsInitParam.getStringConfig());
        int errCode = HciCloudTts.hciTtsInit(ttsInitParam.getStringConfig());
        return errCode;
    }

    /**
     * TTS合成函数
     *
     * @param text 需要合成的文本
     * @return 合成成功返回true，失败返回false
     */
    public static boolean synth(String text, String path) {
        filePath = path;
        Session session = new Session();
        TtsConfig ttsConfig = new TtsConfig();
        ttsConfig.addParam(TtsConfig.PARAM_KEY_CAP_KEY, getKey());

        // 音频格式
        ttsConfig.addParam(TtsConfig.PARAM_KEY_AUDIO_FORMAT, "pcm16k16bit");
        ttsConfig.addParam(TtsConfig.PARAM_KEY_SPEED, "5.4");
        /**
         * 设置语风格
         */
        ttsConfig.addParam(TtsConfig.PARAM_KEY_VOICE_STYLE, TtsConfig.HCI_TTS_VOICE_STYLE_VIVID);//，生动
        /**
         * 设置数字读取方式
         */
        ttsConfig.addParam(TtsConfig.PARAM_KEY_DIGIT_MODE, TtsConfig.HCI_TTS_DIGIT_MODE_AUTO_NUMBER);
        String sV = TtsConfig.HCI_TTS_SOUND_EFFECT_BASE;

        String fV = "5";

        ttsConfig.addParam(TtsConfig.PARAM_KEY_SOUND_EFFECT, sV);

        ttsConfig.addParam(TtsConfig.PARAM_KEY_PITCH, fV);
        ttsConfig.addParam(TtsConfig.PARAM_KEY_VOLUME, 9 + "");

        Log.d(TAG, "hciTtsSessionStart config : " + ttsConfig.getStringConfig());
        int errCode = HciCloudTts.hciTtsSessionStart(ttsConfig.getStringConfig(), session);
        if (errCode != HciErrorCode.HCI_ERR_NONE) {
            Log.d(TAG, "hciTtsSessionStart failed.");
            return false;
        }
        TtsConfig synthConfig = new TtsConfig();
        errCode = HciCloudTts.hciTtsSynth(session, text, synthConfig.getStringConfig(), mTtsSynthCallback);
        if (errCode != HciErrorCode.HCI_ERR_NONE) {
            Log.d(TAG, "hciTtsSynth failed.");
            return false;
        }
        errCode = HciCloudTts.hciTtsSessionStop(session);
        if (errCode != HciErrorCode.HCI_ERR_NONE) {
            Log.d(TAG, "hciTtsSessionStop failed.");
            return false;
        }
        return true;
    }

    private static String getKey() {
//		return "tts.local.xixi.v6";
        return "tts.local.xixi_xixi.v6";
    }

    /**
     * TTS反初始化函数
     *
     * @return
     */
    public static int release() {
        int errCode = HciCloudTts.hciTtsRelease();
        return errCode;
    }

    /**
     * TTS合成回调函数
     */
    private static ITtsSynthCallback mTtsSynthCallback = new ITtsSynthCallback() {

        @Override
        public boolean onSynthFinish(int errorCode, TtsSynthResult result) {
            // TODO Auto-generated method stub
            //如果合成出错，直接退出
            if (errorCode != HciErrorCode.HCI_ERR_NONE) {
                Log.e(TAG, "synth error, code = " + errorCode);
                return false;
            }
            //保存音频文件初始化
            if (mFos == null) {
                initFileOutputStream();
            }
            //将本次合成的数据写入文件
            // 每次合成的数据，可能不是需要合成文本的全部，需要多次写入
            if (result != null && result.getVoiceData() != null) {
                int length = result.getVoiceData().length;
                if (length > 0) {
                    try {
                        mFos.write(result.getVoiceData(), 0, length);
                        mFos.flush();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            //是否写完了
            if (!result.isHasMoreData()) {
                flushOutputStream();
            }
            // 返回true表示处理结果成功,通知引擎可以继续合成并返回下一次的合成结果; 如果不希望引擎继续合成, 则返回false
            // 该方法在引擎中是同步的,即引擎会持续阻塞一直到该方法执行结束
            return true;
        }

    };
    private static String filePath = Environment.getExternalStorageDirectory() + "/synth.pcm";

    protected static void initFileOutputStream() {
        // TODO Auto-generated method stub
        try {
            File file = new File(filePath);
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            if (file.exists()) {
                file.delete();
            } else {
                file.createNewFile();
            }
            mFos = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected static void flushOutputStream() {
        // TODO Auto-generated method stub
        try {
            mFos.close();
            mFos = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
