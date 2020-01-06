package com.spldeolin.dg.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Deolin 2019-12-03
 */
@AllArgsConstructor
@Getter
public enum BodyType {

    none("none"),

    va1ue("value"),

    valueArray("valueArray"),

    keyValue("keyValue"),

    keyValueArray("keyValueArray"),

    keyValuePage("keyValuePage"),

    chaos("chaos");

    private String value;

}
