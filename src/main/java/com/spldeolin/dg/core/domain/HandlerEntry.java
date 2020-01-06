package com.spldeolin.dg.core.domain;

import java.lang.reflect.Method;
import java.util.Collection;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.resolution.types.ResolvedType;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-01-02
 */
@Data
@Accessors(fluent = true)
public class HandlerEntry {

    private ClassOrInterfaceDeclaration controller;

    private Class<?> reflectController;

    private String shortestQualifiedSignature;

    private MethodDeclaration handler;

    private Method reflectHandler;

    private ResolvedType responseBodyResolvedType;

    private ResolvedType requestBodyResolveType;

    private Collection<Parameter> pathVariables;

    private Collection<Parameter> requestParams;

    @Override
    public String toString() {
        return "HandlerEntry{" + shortestQualifiedSignature + '}';
    }

}