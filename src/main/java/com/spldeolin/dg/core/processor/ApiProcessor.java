package com.spldeolin.dg.core.processor;

import java.nio.file.Path;
import java.util.List;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.spldeolin.dg.Conf;
import com.spldeolin.dg.core.domain.ApiDomain;
import com.spldeolin.dg.core.lcbi.LoadClassBasedInfoImporter;
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
        String resultTypeName = Conf.HOW_TO_FIND_RESULT_TYPE.getExtractor().extractHandlerResultTypeQualifier(handler);

        StringBuilder sb = new StringBuilder(64);
        ResolvedMethodDeclaration resolve = handler.resolve();
        sb.append(resolve.getQualifiedName());
        sb.append("(");
        List<String> parameterTypeQualifier = Lists.newArrayList();
        for (int i = 0; i < resolve.getNumberOfParams(); i++) {
            parameterTypeQualifier.add(resolve.getParam(i).getType().asReferenceType().getId());
        }
        Joiner.on(",").appendTo(sb, parameterTypeQualifier);
        sb.append(")");
        String methodQualifier = sb.toString();

        ApiDomain api = new ApiDomain();
        api.setHttpMethod(LoadClassBasedInfoImporter.getHandlerHttpMethods(methodQualifier));
        api.setUri(LoadClassBasedInfoImporter.getHandlerUri(methodQualifier));
        api.setDescription(Javadocs.extractFirstLine(handler));
        api.setUriPathFields(Lists.newArrayList());
        api.setUriQueryFields(Lists.newArrayList());
        fieldProcessor.processRequestBody(handler.getParameters(), api);
        fieldProcessor.processResponseBody(resultTypeName, api);
        return api;
    }

}