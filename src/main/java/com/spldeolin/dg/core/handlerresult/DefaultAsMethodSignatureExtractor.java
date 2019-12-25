package com.spldeolin.dg.core.handlerresult;

import com.github.javaparser.ast.body.MethodDeclaration;

/**
 * @author Deolin 2019-12-25
 */
class DefaultAsMethodSignatureExtractor implements HandlerResultTypeExtractor {

    @Override
    public String extractHandlerResultTypeQualifier(MethodDeclaration handler) {
        return handler.getType().resolve().asReferenceType().getId();
    }

}
