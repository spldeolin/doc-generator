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

    public static String replaceLast(String string, String target, String replacement) {
        int ind = string.lastIndexOf(target);
        return string.substring(0, ind) + replacement + string.substring(ind + 1);
    }

}
