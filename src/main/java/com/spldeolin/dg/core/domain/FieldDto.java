package com.spldeolin.dg.core.domain;

import java.util.Collection;
import org.apache.commons.lang3.tuple.Pair;
import com.spldeolin.dg.core.enums.NumberFormat;
import com.spldeolin.dg.core.enums.TypeName;
import com.spldeolin.dg.core.enums.ValidEnum;
import lombok.Data;
import lombok.ToString;

/**
 * @author Deolin 2019-12-02
 */
@Data
@ToString(exclude = {"parentField"}) // StackOverflowError
public class FieldDto {

    private FieldDto parentField;

    private String fieldName;

    /**
     * @see TypeName
     * @see ApiDto#getUriPathFields() string, number, boolean
     * @see ApiDto#getUriQueryFields() string, number, boolean
     */
    private TypeName typeName;

    private String stringFormat;

    private NumberFormat numberFormat;

    /**
     * notNull absent & notEmpty absent & notBlank absent = TRUE
     */
    private Boolean nullable;

    private Collection<Pair<ValidEnum, String>> valids;

    private String description;

    /**
     * com.topaiebiz.rapgen2.enums.TypeName#object
     * com.topaiebiz.rapgen2.enums.TypeName#objectArray
     */
    private Collection<FieldDto> fields;

}