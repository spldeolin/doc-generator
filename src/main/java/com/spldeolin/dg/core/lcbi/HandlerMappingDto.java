package com.spldeolin.dg.core.lcbi;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2019-12-26
 */
@Data
@Accessors(chain = true)
public class HandlerMappingDto {

    private String methodQualifierByToString;

    private String httpMethod;

    private String uri;

}