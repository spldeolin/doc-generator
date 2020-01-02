package com.spldeolin.dg.core.util;

import java.lang.reflect.Method;
import com.github.javaparser.ast.body.MethodDeclaration;

/**
 * @author Deolin 2019-12-29
 */
public class MethodQualifier {

    /**
     * 获取能定位到唯一一个方法的最短形式QualifiedSignature
     */
    public static String getShortestQualifiedSignature(Method method) {
        StringBuilder result = new StringBuilder(64);

        // class qualifier and method name
        result.append(method.getDeclaringClass().getTypeName()).append('.');
        result.append(method.getName());

        // every parameter qualifier
        result.append('(');
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int j = 0; j < parameterTypes.length; j++) {
            result.append(parameterTypes[j].getTypeName());
            if (j < (parameterTypes.length - 1)) {
                result.append(",");
            }
        }
        result.append(')');

        return trimAllSpaces(result);
    }

    /**
     * 获取能定位到唯一一个方法的最短形式QualifiedSignature
     */
    public static String getShortestQualifiedSignature(MethodDeclaration methodDeclaration) {
        String result = methodDeclaration.resolve().getQualifiedSignature().replaceAll("<[^>]+>", "");
        return trimAllSpaces(result);
    }

    private static String trimAllSpaces(CharSequence s) {
        return s.toString().replaceAll(" ", "");
    }

    static void separateWithCommas(Class<?>[] types, StringBuilder sb) {
        for (int j = 0; j < types.length; j++) {
            sb.append(types[j].getTypeName());
            if (j < (types.length - 1)) {
                sb.append(",");
            }
        }

    }



}
