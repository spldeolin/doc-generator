package com.spldeolin.dg.core.domain;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.spldeolin.dg.core.enums.JsonType;
import com.spldeolin.dg.core.enums.NumberFormatType;
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

    /**
     * struct=val时有效
     */
    private JsonType valueStructureJsonType;

    /**
     * struct=val时有效
     */
    private NumberFormatType valueStructureNumberFormat;

    /**
     * struct=keyVal时有效
     */
    private Class<?> reflectClass;

    /**
     * struct=keyVal时有效
     */
    private ClassOrInterfaceDeclaration clazz;

    /**
     * struct=chaos时有效
     */
    private JsonSchema jsonSchema;

}
