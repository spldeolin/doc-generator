package com.spldeolin.dg.core.extractor.javadoc;

import java.util.List;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;
import com.github.javaparser.javadoc.description.JavadocDescriptionElement;
import com.spldeolin.dg.core.util.Strings;

/**
 * @author Deolin 2019-12-03
 */
public class JavadocExtractFirstLineStrategy implements JavadocExtractStrategy {

    @Override
    public String extract(Javadoc javadoc) {
        JavadocDescription description = javadoc.getDescription();
        List<JavadocDescriptionElement> elements = description.getElements();
        if (elements.size() == 0) {
            return "";
        }

        JavadocDescriptionElement javadocDescriptionElement = elements.get(0);
        String txt = javadocDescriptionElement.toText();

        List<String> lines = Strings.splitLineByLine(txt);
        if (lines.size() == 0) {
            return "";
        }

        return lines.get(0);
    }

}
