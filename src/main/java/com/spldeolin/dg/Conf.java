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

    public static final Path TARGET_PROJECT_PATH = Paths
            .get("/Users/deolin/Documents/project-repo/motherbuy/motherbuy");

    public static final Path TARGET_SPRING_BOOT_FAT_JAR_PATH = Paths.get("");

    public static final HandlerResultTypeExtractStrategy HOW_TO_FIND_RESULT_TYPE =
            HandlerResultTypeExtractStrategy.FIRST_SEE_TAG_CONTENT;

}
