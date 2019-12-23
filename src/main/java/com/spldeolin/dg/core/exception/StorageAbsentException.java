package com.spldeolin.dg.core.exception;

import com.github.javaparser.ast.CompilationUnit;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Deolin 2019-12-13
 */
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class StorageAbsentException extends RuntimeException {

    private CompilationUnit cu;

}
