package com.cantalou.util.soplugin;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.cantalou.manager.jcvideoplayer.JCVideoPlayerAdapter;

import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayerStandard;

public class MainActivity extends Activity {

    JCVideoPlayerAdapter jcVideoPlayerStandard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        jcVideoPlayerStandard = (JCVideoPlayerAdapter) findViewById(R.id.custom_videoplayer_standard);
        jcVideoPlayerStandard.setUp("http://2449.vod.myqcloud.com/2449_22ca37a6ea9011e5acaaf51d105342e3.f20.mp4"
                , JCVideoPlayerStandard.SCREEN_LAYOUT_LIST, "闭眼睛");
        jcVideoPlayerStandard.thumbImageView.setImageResource(R.drawable.video);
    }

    @Override
    public void onBackPressed() {
        if (JCVideoPlayer.backPress()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        jcVideoPlayerStandard.releaseVideos();
    }
}
