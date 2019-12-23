package com.spldeolin.dg.core.util;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import com.google.common.collect.Lists;

/**
 * @author Deolin 2019-12-03
 */
public class Strings {

    public static List<String> splitLineByLine(String string) {
        if (string == null) {
            return Lists.newArrayList();
        }
        return Lists.newArrayList(string.split("\\r?\\n"));
    }

    public static boolean isSurroundedBy(String string, String start, String end) {
        return string.startsWith(start) && string.endsWith(end);
    }

    public static String removeSurround(String string, String start, String end) {
        string = StringUtils.removeStart(string, start);
        string = StringUtils.removeEnd(string, end);
        return string;
    }

    public static String removeFirstLetterAndTrim(String s) {
        if (StringUtils.isBlank(s)) {
            return s;
        }
        return s.substring(1).trim();
    }

    public static String upperFirstLetter(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public static String lowerFirstLetter(String s) {
        return s.substring(0, 1).toLowerCase() + s.substring(1);
    }

}
