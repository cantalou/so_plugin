package com.cantalou.manager.soloader;

/**
 * @author cantalou
 * @date 2016年08月29日 17:47
 */
public enum Platform {

    NULL(""), X86("x86"), ARMEABI("armeabi"), ARMEABI_V7A("armeabi-v7a");

    String name;

    Platform(String name) {
        this.name = name;
    }
}

