package com.spldeolin.dg.core.jsonschema;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Deolin 2019-12-16
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PojoJsonSchemaData {

    private String pojoQualifier;

    private JsonSchema jsonSchema;

}