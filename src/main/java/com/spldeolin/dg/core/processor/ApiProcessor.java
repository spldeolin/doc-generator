package com.spldeolin.dg.core.processor;

import java.util.Collection;
import org.apache.commons.lang3.tuple.Pair;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.common.collect.Lists;
import com.spldeolin.dg.Conf;
import com.spldeolin.dg.core.domain.ApiDomain;
import com.spldeolin.dg.core.domain.HandlerEntry;
import com.spldeolin.dg.core.enums.MethodType;
import com.spldeolin.dg.core.util.Javadocs;

/**
 * @author Deolin 2019-12-03
 */
public class ApiProcessor {

    public ApiDomain process(HandlerEntry handlerEntry) {
        ClassOrInterfaceDeclaration controller = handlerEntry.getController();
        MethodDeclaration handler = handlerEntry.getHandler();

        ApiDomain api = new ApiDomain();

        Pair<Collection<MethodType>, Collection<String>> urisAndMethods = new RequestMappingProcessor()
                .process(controller, handler);
        api.setMethod(urisAndMethods.getLeft());
        api.setUri(urisAndMethods.getRight());

        api.setDescription(Javadocs.extractFirstLine(handler));
        api.setUriPathFields(Lists.newArrayList());
        api.setUriQueryFields(Lists.newArrayList());

        FieldProcessor fieldProcessor = new FieldProcessor();
        String resultTypeName = Conf.HOW_TO_FIND_RESULT_TYPE.getExtractor().extractHandlerResultTypeQualifier(handler);
        fieldProcessor.processRequestBody(handler.getParameters(), api);
        fieldProcessor.processResponseBody(resultTypeName, api);

        return api;
    }

}