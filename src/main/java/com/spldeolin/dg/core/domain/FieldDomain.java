package com.spldeolin.dg.core.domain;

import java.util.Collection;
import com.spldeolin.dg.core.enums.JsonType;
import com.spldeolin.dg.core.enums.NumberFormatType;
import lombok.Data;
import lombok.ToString;

/**
 * @author Deolin 2019-12-02
 */
@Data
@ToString(exclude = {"parentField"}) // StackOverflowError
public class FieldDomain {

    private FieldDomain parentField;

    private String fieldName;

    /**
     * @see JsonType
     * @see ApiDomain#getUriPathFields() string, number, boolean
     * @see ApiDomain#getUriQueryFields() string, number, boolean
     */
    private JsonType jsonType;

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
    private Collection<FieldDomain> fields;

}