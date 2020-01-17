package com.spldeolin.dg.core.domain;

import java.util.Collection;
import com.spldeolin.dg.core.enums.FieldJsonType;
import com.spldeolin.dg.core.enums.NumberFormatType;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-01-17
 */
@Data
@Accessors(fluent = true)
public class UriFieldDomain {

    private String fieldName;

    /**
     * @see FieldJsonType
     * @see ApiDomain#pathVariableFields() string, number, boolean
     * @see ApiDomain#requestParamFields() string, number, boolean
     */
    private FieldJsonType jsonType;

    private String stringFormat;

    private NumberFormatType numberFormat;

    private Boolean required;

    private Collection<ValidatorDomain> validators;

}
