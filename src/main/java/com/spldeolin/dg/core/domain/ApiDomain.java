package com.spldeolin.dg.core.domain;

import java.util.Collection;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.spldeolin.dg.core.enums.BodyType;
import com.spldeolin.dg.core.enums.MethodType;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2019-12-02
 */
@Data
@Accessors(fluent = true)
public class ApiDomain {

    private Collection<MethodType> method;

    private Collection<String> uri;

    private String description;

    private Collection<FieldDomain> uriPathFields;

    private Collection<FieldDomain> uriQueryFields;

    private BodyType requestBodyType;

    private Collection<FieldDomain> requestBodyFields;

    private Collection<FieldDomain> requestBodyFieldsFlatly;

    private JsonSchema requestBodyChaosJsonSchema;

    private BodyType responseBodyType;

    private Collection<FieldDomain> responseBodyFields;

    private Collection<FieldDomain> responseBodyFieldsFlatly;

    private JsonSchema responseBodyChaosJsonSchema;

}