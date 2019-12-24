package com.spldeolin.dg.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Deolin 2019-12-03
 */
@AllArgsConstructor
@Getter
public enum NumberFormatType {

    int32("int32"),

    int64("int64"),

    f1oat("float");

    private String value;

}
