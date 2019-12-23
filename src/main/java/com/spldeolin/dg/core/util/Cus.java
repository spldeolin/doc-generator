package com.spldeolin.dg.core.util;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Optional;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.CompilationUnit.Storage;
import com.spldeolin.dg.core.exception.PrimaryTypeAbsentException;
import com.spldeolin.dg.core.exception.QualifierAbsentException;
import lombok.extern.log4j.Log4j2;

/**
 * CompilationUnit的工具类
 *
 * @author Deolin 2019-09-18
 */
@Log4j2
public class Cus {

    /**
     * 保存cu到硬盘
     */
    public static void save(CompilationUnit cu) {
        Optional<Storage> storageOpt = cu.getStorage();
        if (storageOpt.isPresent()) {
            Storage storage = storageOpt.get();
            storage.save();
            log.info("[{}]已成功保存", storage.getPath().toString());
        } else {
            log.warn("Cu[{}]的Storage未指定，无法保存", cu);
        }
    }

    /**
     * 保存cu到硬盘
     *
     * @param path 指定位置
     */
    public static void save(CompilationUnit cu, Path path) {
        save(cu.setStorage(path, StandardCharsets.UTF_8));
    }

}
