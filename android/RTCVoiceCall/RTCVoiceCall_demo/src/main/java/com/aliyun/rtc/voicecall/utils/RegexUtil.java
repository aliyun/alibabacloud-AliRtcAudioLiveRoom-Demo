package com.aliyun.rtc.voicecall.utils;

import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtil {
    public static final String CHANNELID_REGEX = "^[a-zA-Z0-9_]{1,64}$";
    public static boolean regexStr(String regex, CharSequence str) {
        if (TextUtils.isEmpty(regex) || TextUtils.isEmpty(str)) {
            return true;
        }
        Pattern compile = Pattern.compile(regex);
        Matcher matcher = compile.matcher(str);
        return !matcher.find();
    }
}
