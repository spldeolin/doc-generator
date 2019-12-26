package com.spldeolin.dg.core.lcbi;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2019-12-16
 */
@Data
@Accessors(chain = true)
public class PojoSchemaDto {

    private String pojoQualifier;

    private JsonSchema jsonSchema;

}