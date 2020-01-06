package com.spldeolin.dg.core.processor;

import java.util.Collection;
import org.apache.commons.lang3.tuple.Pair;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.common.collect.Lists;
import com.spldeolin.dg.Conf;
import com.spldeolin.dg.core.domain.ApiDomain;
import com.spldeolin.dg.core.domain.HandlerEntry;
import com.spldeolin.dg.core.domain.ResultEntry;
import com.spldeolin.dg.core.enums.BodyType;
import com.spldeolin.dg.core.enums.MethodType;
import com.spldeolin.dg.core.util.Javadocs;
import lombok.AllArgsConstructor;

/**
 * @author Deolin 2019-12-03
 */
@AllArgsConstructor
public class ApiProcessor {

    private final HandlerEntry handlerEntry;

    public ApiDomain process() {
        ClassOrInterfaceDeclaration controller = handlerEntry.controller();
        MethodDeclaration handler = handlerEntry.handler();

        ApiDomain api = new ApiDomain();

        Pair<Collection<MethodType>, Collection<String>> urisAndMethods = new RequestMappingProcessor()
                .process(controller, handler);
        api.method(urisAndMethods.getLeft());
        api.uri(urisAndMethods.getRight());

        api.description(Javadocs.extractFirstLine(handler));
        api.uriPathFields(Lists.newArrayList());
        api.uriQueryFields(Lists.newArrayList());

        FieldProcessor fieldProcessor = new FieldProcessor();
        String resultTypeName = Conf.HOW_TO_FIND_RESULT_TYPE.getExtractor().extractHandlerResultTypeQualifier(handler);
        fieldProcessor.processRequestBody(handler.getParameters(), api);
        fieldProcessor.processResponseBody(resultTypeName, api);

        ResultEntry resultEntry = new ResultProcessor(handlerEntry.responseBodyResolvedType()).process();
        this.calcResponseBodyType(api, resultEntry);

        return api;
    }

    private void calcResponseBodyType(ApiDomain api, ResultEntry resultEntry) {
        if (resultEntry.isVoidStructureResultEntry()) {
            api.responseBodyType(BodyType.none);
        }
        if (resultEntry.isChaosStructureResultEntry()) {
            api.responseBodyType(BodyType.chaos);
        }
        if (resultEntry.isValueStructureResultEntry()) {
            if (resultEntry.inArray()) {
                api.responseBodyType(BodyType.valueArray);
            } else {
                api.responseBodyType(BodyType.va1ue);
            }
        }
        if (resultEntry.isKeyValStructureResultEntry()) {
            if (resultEntry.inArray()) {
                api.responseBodyType(BodyType.keyValueArray);
            } else if (resultEntry.inPage()) {
                api.responseBodyType(BodyType.keyValuePage);
            } else {
                api.responseBodyType(BodyType.keyValue);
            }
        }
    }

}