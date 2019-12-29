package com.spldeolin.dg.core.util;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import com.github.javaparser.ast.body.MethodDeclaration;

/**
 * @author Deolin 2019-12-29
 */
public class MethodQualifiedSignatures {

    public static String baseOnUniformedStandard(Method method) {
        StringBuilder sb = new StringBuilder(64);

        // class qualifier and method name
        sb.append(method.getDeclaringClass().getTypeName()).append('.');
        sb.append(method.getName());

        // every parameter qualifier
        sb.append('(');
        Type[] params = method.getGenericParameterTypes();
        for (int j = 0; j < params.length; j++) {
            String param = params[j].getTypeName();
            if (method.isVarArgs() && (j == params.length - 1)) // replace T[] with T...
            {
                param = param.replaceFirst("\\[\\]$", "...");
            }
            sb.append(param);
            if (j < (params.length - 1)) {
                sb.append(',');
            }
        }
        sb.append(')');

        return trimAllSpaces(sb);
    }

    public static String baseOnUniformedStandard(MethodDeclaration methodDeclaration) {
        return trimAllSpaces(methodDeclaration.resolve().getQualifiedSignature());
    }

    private static String trimAllSpaces(CharSequence s) {
        return s.toString().replaceAll(" ", "");
    }

}
