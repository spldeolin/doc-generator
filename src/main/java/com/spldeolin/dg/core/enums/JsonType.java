package com.spldeolin.dg.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Deolin 2019-12-03
 */
@AllArgsConstructor
@Getter
public enum JsonType {

    string("string"),

    number("number"),

    bool("boolean"),

    object("object"),

    stringArray("stringArray"),

    numberArray("numberArray"),

    booleanArray("booleanArray"),

    objectArray("objectArray");

    private String value;

}
