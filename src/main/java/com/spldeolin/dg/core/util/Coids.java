package com.spldeolin.dg.core.util;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

/**
 * @author Deolin 2019-09-27
 */
public class Coids {

    public static boolean hasExtended(ClassOrInterfaceDeclaration classOrInterface) {
        return classOrInterface.getExtendedTypes().size() > 0;
    }

    public static boolean isAnnotatedBy(ClassOrInterfaceDeclaration classOrInterface, String annotationName) {
        return classOrInterface.getAnnotationByName(annotationName).isPresent();
    }

    public static boolean isNotAnnotatedBy(ClassOrInterfaceDeclaration classOrInterface, String annotationName) {
        return !isAnnotatedBy(classOrInterface, annotationName);
    }

    public static boolean hasField(ClassOrInterfaceDeclaration classOrInterface, String fieldName) {
        return classOrInterface.getFieldByName(fieldName).isPresent();
    }

}
