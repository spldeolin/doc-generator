package com.spldeolin.dg.core.handlerresult;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.spldeolin.dg.core.util.Javadocs;

/**
 * @author Deolin 2019-12-25
 */
class FirstSeeTagContentExtractor implements HandlerResultTypeExtractor {

    @Override
    public String extractHandlerResultTypeQualifier(MethodDeclaration handler) {
        return Javadocs.extractFirstSeeTag(handler);
    }

}
