package vn.hust.socialnetwork.utils;

import android.util.Log;

import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;
import java.util.List;

public class StringExtension {

    /**
     * Format string react count.
     *
     * @param count number user react the post
     * @return xxx.xxx
     */
    public static String formatReactCountPost(int count) {
        if (count == 0) {
            return "0";
        }
        StringBuilder result = new StringBuilder();
        int temp;
        while (count != 0) {
            temp = count % 1000;
            count /= 1000;
            if (count > 0) {
                result.insert(0, "." + temp);
            } else {
                result.insert(0, temp);
            }
        }
        return result.toString();
    }

    /**
     * Format string member group count.
     *
     * @param count number member of the group
     * @return xxxk
     */
    public static String formatMemberGroupCount(int count) {
        if (count >= 1000000) {
            double temp = count / 1000000.0;
            return ((double) Math.floor(temp * 10) / 10) + " triệu";
        }
        if (count >= 1000) {
            return (count / 1000) + "k";
        }
        return String.valueOf(count);
    }

    /**
     * Check not null and not empty for a string object.
     *
     * @param str a string
     * @return true if string is not null and has value
     * <p>false if string is null or empty
     */
    public static boolean checkValidValueString(String str) {
        return str != null && !str.isEmpty();
    }

    /**
     * Replace sensitive words to ***
     */
    public static String cleanContent(String content) {
        List<String> sensitiveWords = Hawk.get(AppSharedPreferences.SENSITIVE_WORD_KEY, new ArrayList<>());
        for (String word : sensitiveWords) {
            String lowerWord = word.toLowerCase();
            String upperWord = word.toUpperCase();
            String convertWord = getConvertWord(word.length());
            content = content.replace(word, convertWord);
            content = content.replace(lowerWord, convertWord);
            content = content.replace(upperWord, convertWord);
        }
        return content;
    }

    public static String getConvertWord(int lengthWord) {
        String s = "";
        for (int i = 0; i < lengthWord; i++) {
            s = s + "*";
        }
        return s;
    }
}
