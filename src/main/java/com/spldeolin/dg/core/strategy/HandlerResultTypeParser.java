package com.spldeolin.dg.core.strategy;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;

/**
 * @author Deolin 2020-01-02
 */
public interface HandlerResultTypeParser {

    ResolvedType parse(MethodDeclaration handler);

}
