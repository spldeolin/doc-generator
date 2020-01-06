package com.spldeolin.dg.core.domain;

import com.spldeolin.dg.core.enums.JsonType;
import com.spldeolin.dg.core.enums.NumberFormatType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-01-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
public class ValueStructureResultEntry extends ResultEntry {

    /**
     * struct=val时有效
     */
    private JsonType valueStructureJsonType;

    /**
     * struct=val时有效
     */
    private NumberFormatType valueStructureNumberFormat;



}
