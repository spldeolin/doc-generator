package com.spldeolin.dg.core.processor;

import java.nio.file.Path;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.common.collect.Lists;
import com.spldeolin.dg.core.domain.ApiDto;
import com.spldeolin.dg.core.extractor.annotation.UriExtractor;
import com.spldeolin.dg.core.extractor.javadoc.JavadocExtractStrategy;
import com.spldeolin.dg.core.extractor.javadoc.JavadocExtractor;

/**
 * @author Deolin 2019-12-03
 */
public class ApiProcessor {

    private Path path;

    public ApiProcessor(Path path) {
        this.path = path;
    }

    public ApiDto process(ClassOrInterfaceDeclaration o000, MethodDeclaration o00) {

        String handlerComment = new JavadocExtractor().javadoc(o00).strategy(JavadocExtractStrategy.EXTRACT_FIRST_LINE)
                .extract();

        String resultTypeName = new JavadocExtractor().javadoc(o00)
                .strategy(JavadocExtractStrategy.EXTRACT_FIRST_SEE_TAG).extract();

        String uri = new UriExtractor().controller(o000).handler(o00).extract();

        FieldProcessor fieldProcessor = new FieldProcessor(path);

        ApiDto api = new ApiDto();
        api.setUri(uri);
        api.setDescription(handlerComment);
        api.setUriPathFields(Lists.newArrayList());
        api.setUriQueryFields(Lists.newArrayList());
        fieldProcessor.processRequestBody(o00.getParameters(), api);
        fieldProcessor.processResponseBody(resultTypeName, api);
        return api;
    }

}