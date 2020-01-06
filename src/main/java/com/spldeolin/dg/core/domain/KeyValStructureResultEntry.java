package com.spldeolin.dg.core.domain;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * kv型数据结构
 *
 * e.g.: @ResponseBody public UserVo ....
 *
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

    private ObjectSchema objectSchema;

}
