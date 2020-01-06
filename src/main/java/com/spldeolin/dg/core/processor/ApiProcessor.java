package com.spldeolin.dg.core.processor;

import java.util.Collection;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.common.collect.Lists;
import com.spldeolin.dg.core.domain.ApiDomain;
import com.spldeolin.dg.core.domain.FieldDomain;
import com.spldeolin.dg.core.enums.BodyType;
import com.spldeolin.dg.core.processor.result.BodyProcessResult;
import com.spldeolin.dg.core.processor.result.HandlerProcessResult;
import com.spldeolin.dg.core.processor.result.RequestMappingProcessResult;
import com.spldeolin.dg.core.processor.result.ValueStructureBodyProcessResult;
import com.spldeolin.dg.core.util.Javadocs;
import lombok.AllArgsConstructor;

/**
 * @author Deolin 2019-12-03
 */
@AllArgsConstructor
public class ApiProcessor {

    private final HandlerProcessResult handlerEntry;

    public ApiDomain process() {
        ClassOrInterfaceDeclaration controller = handlerEntry.controller();
        MethodDeclaration handler = handlerEntry.handler();

        ApiDomain api = new ApiDomain();

        RequestMappingProcessResult requestMappingProcessResult = new RequestMappingProcessor()
                .process(controller, handler);
        api.method(requestMappingProcessResult.methodTypes());
        api.uri(requestMappingProcessResult.uris());

        api.description(Javadocs.extractFirstLine(handler));
        api.uriPathFields(Lists.newArrayList());
        api.uriQueryFields(Lists.newArrayList());

        FieldProcessor fieldProcessor = new FieldProcessor();
        fieldProcessor.processRequestBody(handler.getParameters(), api);

        BodyProcessResult resultEntry = new BodyProcessor(handlerEntry.responseBodyResolvedType()).process();
        this.calcResponseBodyType(api, resultEntry);
        if (resultEntry.isKeyValueStructure()) {
            new FieldProcessorV2().processResponseBody(resultEntry.asKeyValueStructure().objectSchema(), api);
        }
        if (resultEntry.isValueStructure()) {
            ValueStructureBodyProcessResult valueStruct = resultEntry.asValueStructure();
            Collection<FieldDomain> field = Lists.newArrayList(
                    new FieldDomain().jsonType(valueStruct.valueStructureJsonType())
                            .numberFormat(valueStruct.valueStructureNumberFormat()));
            api.responseBodyFields(field);
            api.responseBodyFieldsFlatly(field);
        }
        if (resultEntry.isChaosStructure()) {
            api.responseBodyChaosJsonSchema(resultEntry.asChaosStructure().jsonSchema());
        }

        return api;
    }

    private void calcResponseBodyType(ApiDomain api, BodyProcessResult resultEntry) {
        if (resultEntry.isVoidStructure()) {
            api.responseBodyType(BodyType.none);
        }
        if (resultEntry.isChaosStructure()) {
            api.responseBodyType(BodyType.chaos);
        }
        if (resultEntry.isValueStructure()) {
            if (resultEntry.inArray()) {
                api.responseBodyType(BodyType.valueArray);
            } else {
                api.responseBodyType(BodyType.va1ue);
            }
        }
        if (resultEntry.isKeyValueStructure()) {
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