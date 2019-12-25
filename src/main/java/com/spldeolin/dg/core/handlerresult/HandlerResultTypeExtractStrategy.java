package com.spldeolin.dg.core.handlerresult;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Deolin 2019-12-25
 */
@AllArgsConstructor
@Getter
public enum HandlerResultTypeExtractStrategy {

    FIRST_SEE_TAG_CONTENT(new FirstSeeTagContentExtractor()),

    DEFAULT_AS_METHOD_SIGNATURE(new DefaultAsMethodSignatureExtractor());

    private HandlerResultTypeExtractor extractor;

}
