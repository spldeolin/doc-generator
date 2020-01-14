package com.spldeolin.dg.core.domain;

import java.util.Collection;
import com.spldeolin.dg.core.enums.FieldJsonType;
import com.spldeolin.dg.core.enums.NumberFormatType;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2019-12-02
 */
@Data
@Accessors(fluent = true)
@ToString(exclude = {"parentField"}) // StackOverflowError
public class BodyFieldDomain {

    private BodyFieldDomain parentField;

    private String fieldName;

    /**
     * @see FieldJsonType
     * @see ApiDomain#pathVariableFields() string, number, boolean
     * @see ApiDomain#requestParamFields() string, number, boolean
     */
    private FieldJsonType jsonType;

    private String stringFormat;

    private NumberFormatType numberFormat;

    /**
     * notNull absent & notEmpty absent & notBlank absent = TRUE
     */
    private Boolean nullable;

    private Collection<ValidatorDomain> validators;

    private String description;

    /**
     * com.topaiebiz.rapgen2.enums.TypeName#object
     * com.topaiebiz.rapgen2.enums.TypeName#objectArray
     */
    private Collection<BodyFieldDomain> fields;

}