package com.spldeolin.dg.core.domain;

import java.util.Collection;
import com.spldeolin.dg.core.enums.RequestBodyType;
import com.spldeolin.dg.core.enums.ResponseBodyType;
import lombok.Data;

/**
 * @author Deolin 2019-12-02
 */
@Data
public class ApiDto {

    private String uri;

    private String description;

    private Collection<FieldDto> uriPathFields;

    private Collection<FieldDto> uriQueryFields;

    private RequestBodyType requestBodyType;

    private Collection<FieldDto> requestBodyFileds;

    private Collection<FieldDto> requestBodyFiledsFlatly;

    private ResponseBodyType responseBodyType;

    private Collection<FieldDto> responseBodyFields;

    private Collection<FieldDto> responseBodyFieldsFlatly;

}