package com.spldeolin.dg.core.domain;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-01-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
public class KeyValStructureResultEntry extends ResultEntry {

    /**
     * struct=keyVal时有效
     */
    private Class<?> reflectClass;

    /**
     * struct=keyVal时有效
     */
    private ClassOrInterfaceDeclaration clazz;



}
