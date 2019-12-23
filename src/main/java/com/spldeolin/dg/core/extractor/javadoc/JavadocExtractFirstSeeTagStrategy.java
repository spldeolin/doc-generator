package com.spldeolin.dg.core.extractor.javadoc;

import java.util.List;
import java.util.Optional;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.javadoc.JavadocBlockTag.Type;
import com.github.javaparser.javadoc.description.JavadocDescriptionElement;

/**
 * @author Deolin 2019-12-03
 */
public class JavadocExtractFirstSeeTagStrategy implements JavadocExtractStrategy {

    @Override
    public String extract(Javadoc javadoc) {
        Optional<JavadocBlockTag> firstSeeTag = javadoc.getBlockTags().stream()
                .filter(tag -> tag.getType().equals(Type.SEE)).findFirst();

        if (!firstSeeTag.isPresent()) {
            return "";
        }

        List<JavadocDescriptionElement> elements = firstSeeTag.get().getContent().getElements();
        if (elements.size() == 0) {
            return "";
        }

        JavadocDescriptionElement javadocDescriptionElement = elements.get(0);
        return javadocDescriptionElement.toText();
    }

}
