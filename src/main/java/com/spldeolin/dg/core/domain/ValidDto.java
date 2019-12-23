package com.spldeolin.dg.core.domain;

import com.spldeolin.dg.core.enums.ValidEnum;
import lombok.Data;

/**
 * @author Deolin 2019-12-23
 */
@Data
public class ValidDto {

    private ValidEnum validEnum;

    private String note;

}
