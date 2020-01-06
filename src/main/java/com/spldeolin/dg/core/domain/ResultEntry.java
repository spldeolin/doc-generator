package com.spldeolin.dg.core.domain;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.spldeolin.dg.core.enums.ResponseBodyStructure;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-01-03
 */
@Data
@Accessors(fluent = true)
public class ResultEntry {

    /**
     * void, value, keyValue, mazy
     */
    private ResponseBodyStructure struct;

    /**
     * 1none, 2array, 3page
     */
    private Integer outermostWrapper;

    private Class<?> reflectClass;

    private ClassOrInterfaceDeclaration clazz;

    /**
     * mazy | val | arrayValue
     */
    private JsonSchema jsonSchema;

}
