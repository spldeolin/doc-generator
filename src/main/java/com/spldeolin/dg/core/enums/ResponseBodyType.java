package com.spldeolin.dg.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Deolin 2019-12-03
 */
@AllArgsConstructor
@Getter
public enum ResponseBodyType {

    object("object"),

    objectArray("objectArray"),

    objectPage("objectPage");

    private String value;

}
