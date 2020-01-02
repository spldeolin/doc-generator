package com.spldeolin.dg.core.domain;

import java.util.Collection;
import com.spldeolin.dg.core.enums.MethodType;
import com.spldeolin.dg.core.enums.RequestBodyType;
import com.spldeolin.dg.core.enums.ResponseBodyMode;
import lombok.Data;

/**
 * @author Deolin 2019-12-02
 */
@Data
public class ApiDomain {

    private Collection<MethodType> method;

    private Collection<String> uri;

    private String description;

    private Collection<FieldDomain> uriPathFields;

    private Collection<FieldDomain> uriQueryFields;

    private RequestBodyType requestBodyType;

    private Collection<FieldDomain> requestBodyFileds;

    private Collection<FieldDomain> requestBodyFiledsFlatly;

    private ResponseBodyMode responseBodyType;

    private Collection<FieldDomain> responseBodyFields;

    private Collection<FieldDomain> responseBodyFieldsFlatly;

}