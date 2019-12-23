package com.spldeolin.dg.core.util;

import com.github.javaparser.ast.CompilationUnit;

/**
 * @author Deolin 2019-11-15
 */
public class Imports {

    public static void ensureImportExist(CompilationUnit cu, String importName) {
        if (!cu.getInterfaceByName(importName).isPresent()) {
            cu.addImport(importName);
        }
    }

}
