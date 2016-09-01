package com.cantalou.manager.jcvideoplayer;

import android.content.Context;

import com.cantalou.manager.soplugin.Request;
import com.cantalou.manager.soplugin.RequestListener;

import java.util.ArrayList;

import tv.danmaku.ijk.media.player.IjkLibLoader;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * @author cantalou
 * @date 2016年08月30日 17:03
 */
public class IjkplayerRequest extends Request {

    /**
     * 默认so文件的下载地址
     */
    public static final String DEFAULT_LIB_DIR_URL = "http://127.0.0.1:8080/libs/ijkplayer";

    private static final ArrayList<String> ijkplayerSoFiles = new ArrayList<String>();

    static {
        IjkMediaPlayer.loadLibrariesOnce(new IjkLibLoader() {
                                             @Override
                                             public void loadLibrary(String s) throws UnsatisfiedLinkError, SecurityException {
                                                 ijkplayerSoFiles.add(s);
                                             }
                                         }
        );
    }

    public IjkplayerRequest(Context context, RequestListener listener) {
        super(context, DEFAULT_LIB_DIR_URL, ijkplayerSoFiles.toArray(new String[ijkplayerSoFiles.size()]), listener);
    }

}
