package com.spldeolin.dg.core.handlerresult;

import com.github.javaparser.ast.body.MethodDeclaration;

/**
 * @author Deolin 2019-12-25
 */
public interface HandlerResultTypeExtractor {

    String extractHandlerResultTypeQualifier(MethodDeclaration handler);

}
