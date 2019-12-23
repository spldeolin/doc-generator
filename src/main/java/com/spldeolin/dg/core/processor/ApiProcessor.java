package com.spldeolin.dg.core.processor;

import java.nio.file.Path;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.common.collect.Lists;
import com.spldeolin.dg.core.domain.ApiDto;
import com.spldeolin.dg.core.extractor.annotation.UriExtractor;
import com.spldeolin.dg.core.util.Javadocs;

/**
 * @author Deolin 2019-12-03
 */
public class ApiProcessor {

    private Path path;

    public ApiProcessor(Path path) {
        this.path = path;
    }

    public ApiDto process(ClassOrInterfaceDeclaration controller, MethodDeclaration handler) {
        String uri = new UriExtractor().controller(controller).handler(handler).extract();

        FieldProcessor fieldProcessor = new FieldProcessor(path);
        String resultTypeName = Javadocs.extractFirstSeeTag(handler);

        ApiDto api = new ApiDto();
        api.setUri(uri);
        api.setDescription(Javadocs.extractFirstLine(handler));
        api.setUriPathFields(Lists.newArrayList());
        api.setUriQueryFields(Lists.newArrayList());
        fieldProcessor.processRequestBody(handler.getParameters(), api);
        fieldProcessor.processResponseBody(resultTypeName, api);
        return api;
    }

}