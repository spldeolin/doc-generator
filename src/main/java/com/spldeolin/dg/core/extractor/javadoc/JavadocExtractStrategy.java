package com.spldeolin.dg.core.extractor.javadoc;

import com.github.javaparser.javadoc.Javadoc;

/**
 * @author Deolin 2019-12-03
 */
public interface JavadocExtractStrategy {

    JavadocExtractStrategy EXTRACT_FIRST_LINE = new JavadocExtractFirstLineStrategy();

    JavadocExtractStrategy EXTRACT_FIRST_SEE_TAG = new JavadocExtractFirstSeeTagStrategy();

    String extract(Javadoc javadoc);

}
