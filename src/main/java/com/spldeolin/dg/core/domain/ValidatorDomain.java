package com.spldeolin.dg.core.domain;

import com.spldeolin.dg.core.enums.ValidatorType;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2019-12-23
 */
@Data
@Accessors(chain = true)
public class ValidatorDomain {

    private ValidatorType validatorType;

    private String note;

}
