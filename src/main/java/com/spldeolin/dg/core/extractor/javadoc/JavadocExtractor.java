package com.spldeolin.dg.core.extractor.javadoc;

import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;

/**
 * @author Deolin 2019-12-03
 */
public class JavadocExtractor {

    private Javadoc javadoc;

    private JavadocExtractStrategy javadocExtractStrategy;

    private static final Javadoc EMPTY = new Javadoc(new JavadocDescription());

    public JavadocExtractor javadoc(Javadoc javadoc) {
        this.javadoc = javadoc;
        return this;
    }

    public JavadocExtractor javadoc(JavadocComment javadocComment) {
        return javadoc(javadocComment.parse());
    }

    public JavadocExtractor javadoc(NodeWithJavadoc<?> node) {
        return javadoc(node.getJavadoc().orElse(EMPTY));
    }

    public JavadocExtractor strategy(JavadocExtractStrategy javadocExtractStrategy) {
        this.javadocExtractStrategy = javadocExtractStrategy;
        return this;
    }

    public String extract() {
        if (javadoc == null || javadocExtractStrategy == null) {
            return "";
        }
        return javadocExtractStrategy.extract(javadoc);
    }

}
