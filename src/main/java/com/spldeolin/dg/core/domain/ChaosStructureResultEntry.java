package com.spldeolin.dg.core.domain;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-01-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
public class ChaosStructureResultEntry extends ResultEntry {

    /**
     * struct=chaos时有效
     */
    private JsonSchema jsonSchema;

}
