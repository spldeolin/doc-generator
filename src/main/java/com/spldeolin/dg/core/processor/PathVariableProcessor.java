package com.spldeolin.dg.core.processor;

import static com.github.javaparser.utils.CodeGenerationUtils.f;

import java.util.Collection;
import org.apache.commons.lang3.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.resolution.types.ResolvedType;
import com.google.common.collect.Lists;
import com.spldeolin.dg.core.classloader.WarOrFatJarClassLoader;
import com.spldeolin.dg.core.constant.QualifierConstants;
import com.spldeolin.dg.core.domain.UriFieldDomain;
import com.spldeolin.dg.core.enums.FieldJsonType;
import com.spldeolin.dg.core.enums.NumberFormatType;
import com.spldeolin.dg.core.enums.StringFormatType;
import com.spldeolin.dg.core.util.Strings;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-01-06
 */
@Log4j2
public class PathVariableProcessor {

    private static final JsonSchemaGenerator jsg = new JsonSchemaGenerator(new ObjectMapper());

    public Collection<UriFieldDomain> processor(Collection<Parameter> parameters) {
        Collection<UriFieldDomain> result = Lists.newLinkedList();
        for (Parameter parameter : parameters) {
            UriFieldDomain field = new UriFieldDomain();
            AnnotationExpr pathVariable = parameter.getAnnotationByName("PathVariable").get();
            String name = null;
            boolean required = false;
            if (pathVariable.isSingleMemberAnnotationExpr()) {
                name = pathVariable.asSingleMemberAnnotationExpr().getMemberValue().asStringLiteralExpr().asString();
            }
            if (pathVariable.isNormalAnnotationExpr()) {
                NormalAnnotationExpr normal = pathVariable.asNormalAnnotationExpr();
                for (MemberValuePair pair : normal.getPairs()) {
                    String pairName = pair.getNameAsString();
                    if ("required".equals(pairName)) {
                        required = pair.getValue().asBooleanLiteralExpr().getValue();
                    }
                    if (StringUtils.equalsAny(pairName, "name", "value")) {
                        name = pair.getValue().asStringLiteralExpr().getValue();
                    }
                }
            }
            if (pathVariable.isMarkerAnnotationExpr() || name == null) {
                name = parameter.getNameAsString();
            }
            field.fieldName(name).required(required);

            FieldJsonType jsonType;
            NumberFormatType numberFormat = null;
            StringBuilder stringFormat = new StringBuilder();
            ResolvedType type = parameter.getType().resolve();
            String describe = type.describe();
            JsonSchema jsonSchema = generateSchema(describe);
            if (jsonSchema != null && jsonSchema.isValueTypeSchema()) {
                if (jsonSchema.isStringSchema()) {
                    jsonType = FieldJsonType.string;
                    parameter.getAnnotationByClass(DateTimeFormat.class)
                            .ifPresent(dateTimeFormat -> dateTimeFormat.ifNormalAnnotationExpr(normal -> {
                                normal.getPairs().forEach(pair -> {
                                    if (pair.getNameAsString().equals("pattern")) {
                                        stringFormat.append(f(StringFormatType.datetime.getValue(), pair.getValue()));
                                    }
                                });
                            }));
                    if (stringFormat.length() == 0) {
                        stringFormat.append(StringFormatType.normal.getValue());
                    }
                } else if (jsonSchema.isNumberSchema()) {
                    jsonType = FieldJsonType.number;

                    if (!jsonSchema.isIntegerSchema()) {
                        numberFormat = NumberFormatType.f1oat;
                    } else if (StringUtils.equalsAny(type.describe(), QualifierConstants.INTEGER, "int")) {
                        numberFormat = NumberFormatType.int32;
                    } else if (StringUtils.equalsAny(type.describe(), QualifierConstants.LONG, "long")) {
                        numberFormat = NumberFormatType.int64;
                    } else {
                        numberFormat = NumberFormatType.inT;
                    }
                } else if (jsonSchema.isBooleanSchema()) {
                    jsonType = FieldJsonType.bool;
                } else {
                    throw new RuntimeException("impossible unless bug");
                }
            } else {
                log.warn("parameter[{}]不是ValueSchema", parameter);
                continue;
            }
            field.jsonType(jsonType).numberFormat(numberFormat).stringFormat(stringFormat.toString());

            field.validators(new ValidatorProcessor().process(parameter));
            result.add(field);
        }
        return result;
    }

    private JsonSchema generateSchema(String resolvedTypeDescribe) {
        JsonSchema jsonSchema = generateSchemaByQualifierForClassLoader(resolvedTypeDescribe);
        if (jsonSchema == null && resolvedTypeDescribe.contains(".")) {
            generateSchema(Strings.replaceLast(resolvedTypeDescribe, "\\.", "$"));
        }
        return jsonSchema;
    }

    private JsonSchema generateSchemaByQualifierForClassLoader(String qualifierForClassLoader) {
        JavaType javaType;
        try {
            javaType = new TypeFactory(null) {
                private static final long serialVersionUID = -8151903006798193420L;

                @Override
                public ClassLoader getClassLoader() {
                    return WarOrFatJarClassLoader.classLoader;
                }
            }.constructFromCanonical(qualifierForClassLoader);
        } catch (IllegalArgumentException e) {
            log.warn("TypeFactory.constructFromCanonical({})", qualifierForClassLoader);
            return null;
        }
        try {
            return jsg.generateSchema(javaType);
        } catch (JsonMappingException e) {
            log.warn("jsg.generateSchema({})", javaType);
            return null;
        }
    }

}
