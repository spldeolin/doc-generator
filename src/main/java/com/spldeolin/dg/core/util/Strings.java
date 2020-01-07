package com.spldeolin.dg.core.util;

import java.util.List;
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

    public static String replaceLast(String text, String regex, String replacement) {
        return text.replaceFirst("(?s)" + regex + "(?!.*?" + regex + ")", replacement);
    }

}
