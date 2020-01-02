package com.spldeolin.dg.core.strategy;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

/**
 * @author Deolin 2020-01-02
 */
public interface HandlerFilter {

    boolean filter(ClassOrInterfaceDeclaration controller, MethodDeclaration handler);

}
