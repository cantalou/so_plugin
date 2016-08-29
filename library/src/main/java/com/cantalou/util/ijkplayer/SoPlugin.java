package com.cantalou.util.ijkplayer;

import android.content.Context;

/**
 * @author cantalou
 * @date 2016年08月29日 13:58
 */
public class SoPlugin
{
    public static enum Platform
    {
        X86("x86"), ARMEABI("armeabi"), ARMEABI_V7A("armeabi-v7a");

        String name;

        Platform(String name)
        {
            this.name = name;
        }
    }

    public static final String[] IJKPLAYER_SO_FILE = new String[]{
            "ijkffmpeg", "ijksdl", "ijkplayer"
    };

    private static class Holder
    {
        static final SoPlugin INSTANCE = new SoPlugin();
    }

    private SoPlugin()
    {
    }

    public static SoPlugin getInstance()
    {
        return Holder.INSTANCE;
    }

    public void install(Context context, Platform defaultPlatform)
    {

    }

}
