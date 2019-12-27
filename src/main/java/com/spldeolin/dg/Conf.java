package com.spldeolin.dg;

import java.nio.file.Path;
import java.nio.file.Paths;
import com.spldeolin.dg.core.handlerresult.HandlerResultTypeExtractStrategy;

/**
 * @author Deolin 2019-12-23
 */
public class Conf {

    private Conf() {

    }

    public static final Path PROJECT_PATH = Paths.get("");

    public static final Path POJO_SCHEMA_PATH = Paths.get("");

    public static final Path HANDLER_MAPPING_PATH = Paths.get("");

    public static final Path SPRING_BOOT_FAT_JAR_PATH = Paths.get("");

    public static final HandlerResultTypeExtractStrategy HOW_TO_FIND_RESULT_TYPE =
            HandlerResultTypeExtractStrategy.FIRST_SEE_TAG_CONTENT;

}
