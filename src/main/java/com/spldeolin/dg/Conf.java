package com.spldeolin.dg;

import com.spldeolin.dg.core.handlerresult.HandlerResultTypeExtractStrategy;

/**
 * @author Deolin 2019-12-23
 */
public class Conf {

    private Conf() {

    }

    public static final String PROJECT_PATH = "input target project path here.";

    public static final String POJO_SCHEMA_PATH = "input pojo-schema.json path exporting from target project";

    public static final String HANDLER_MAPPING_PATH = "input pojo-schema.json path exporting from target project";

    public static final HandlerResultTypeExtractStrategy HOW_TO_FIND_RESULT_TYPE =
            HandlerResultTypeExtractStrategy.FIRST_SEE_TAG_CONTENT;

}
