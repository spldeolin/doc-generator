package com.spldeolin.dg.core.domain;

import java.lang.reflect.Method;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import lombok.Data;

/**
 * @author Deolin 2020-01-02
 */
@Data
public class HandlerEntry {

    private ClassOrInterfaceDeclaration controller;

    private Class<?> reflectController;

    private MethodDeclaration handler;

    private Method reflectHandler;



}