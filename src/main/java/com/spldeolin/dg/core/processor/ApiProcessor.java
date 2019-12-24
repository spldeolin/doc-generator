package com.spldeolin.dg.core.processor;

import java.nio.file.Path;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.common.collect.Lists;
import com.spldeolin.dg.core.domain.ApiDomain;
import com.spldeolin.dg.core.util.Javadocs;

/**
 * @author Deolin 2019-12-03
 */
public class ApiProcessor {

    private Path path;

    public ApiProcessor(Path path) {
        this.path = path;
    }

    public ApiDomain process(ClassOrInterfaceDeclaration controller, MethodDeclaration handler) {
        FieldProcessor fieldProcessor = new FieldProcessor(path);
        String resultTypeName = Javadocs.extractFirstSeeTag(handler);

        ApiDomain api = new ApiDomain();
        api.setUri(new UriProcessor().process(controller, handler));
        api.setDescription(Javadocs.extractFirstLine(handler));
        api.setUriPathFields(Lists.newArrayList());
        api.setUriQueryFields(Lists.newArrayList());
        fieldProcessor.processRequestBody(handler.getParameters(), api);
        fieldProcessor.processResponseBody(resultTypeName, api);
        return api;
    }

}