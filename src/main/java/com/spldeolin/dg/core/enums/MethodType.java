package com.spldeolin.dg.core.enums;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Deolin 2019-12-28
 */
@AllArgsConstructor
@Getter
public enum MethodType {

    DELETE("DELETE", "org.springframework.web.bind.annotation.DeleteMapping", "RequestMethod.DELETE"),

    GET("GET", "org.springframework.web.bind.annotation.GetMapping", "RequestMethod.GET"),

    PATCH("PATCH", "org.springframework.web.bind.annotation.PatchMapping", "RequestMethod.PATCH"),

    POST("POST", "org.springframework.web.bind.annotation.PostMapping", "RequestMethod.POST"),

    PUT("PUT", "org.springframework.web.bind.annotation.PutMapping", "RequestMethod.PUT");


    private String value;

    private String annotationQualifier;

    private String fieldAccessExpr;

    public Collection<MethodType> inCollection() {
        return Lists.newArrayList(this);
    }

    public static Optional<MethodType> ofValue(String value) {
        return Arrays.stream(values()).filter(one -> one.getValue().equals(value)).findFirst();
    }

    public static Optional<MethodType> ofAnnotationQualifier(String annotationQualifier) {
        return Arrays.stream(values()).filter(one -> one.getAnnotationQualifier().equals(annotationQualifier))
                .findFirst();
    }

    public static Optional<MethodType> ofFieldAccessExpr(String fieldAccessExpr) {
        return Arrays.stream(values()).filter(one -> one.getFieldAccessExpr().equals(fieldAccessExpr)).findFirst();
    }

}
