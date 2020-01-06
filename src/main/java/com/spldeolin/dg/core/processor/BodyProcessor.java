package com.spldeolin.dg.core.processor;

import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.google.common.collect.Iterables;
import com.spldeolin.dg.Conf;
import com.spldeolin.dg.core.classloader.SpringBootFatJarClassLoader;
import com.spldeolin.dg.core.container.ClassContainer;
import com.spldeolin.dg.core.processor.result.ChaosStructureBodyProcessResult;
import com.spldeolin.dg.core.processor.result.KeyValueStructureBodyProcessResult;
import com.spldeolin.dg.core.processor.result.BodyProcessResult;
import com.spldeolin.dg.core.processor.result.ValueStructureBodyProcessResult;
import com.spldeolin.dg.core.processor.result.VoidStructureBodyProcessResult;
import com.spldeolin.dg.core.enums.JsonType;
import com.spldeolin.dg.core.enums.NumberFormatType;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-01-02
 */
@AllArgsConstructor
@Log4j2
public class BodyProcessor {

    private static final JsonSchemaGenerator jsg = new JsonSchemaGenerator(new ObjectMapper());

    private static final ClassLoader cl = SpringBootFatJarClassLoader.classLoader;

    private final ResolvedType type;

    public BodyProcessResult process() {
        if (type == null) {
            return new VoidStructureBodyProcessResult();
        }

        BodyProcessResult result;
        try {
            if (isArray(type)) {
                // 最外层是 数组
                result = tryProcessNonArrayLikeType(getArrayElementType(type)).inArray(true);
            } else if (isJUC(type)) {
                // 最外层是 列表
                result = tryProcessNonArrayLikeType(getJUCElementType(type)).inArray(true);
            } else if (isPage(type)) {
                // 最外层是 Page对象
                result = tryProcessNonArrayLikeType(getPageElementType(type)).inPage(true);
            } else {
                // 单层
                result = tryProcessNonArrayLikeType(type);
            }
        } catch (Exception e) {
            log.warn("type={}", type, e);
            // as mazy mode
            result = new ChaosStructureBodyProcessResult().jsonSchema(generateSchema(type.describe()));
        }
        return result;
    }

    private BodyProcessResult tryProcessNonArrayLikeType(ResolvedType type) throws ClassNotFoundException {
        String describe = type.describe();
        JsonSchema jsonSchema = generateSchema(describe);
        if (jsonSchema == null) {
            return new VoidStructureBodyProcessResult();
        }

        if (jsonSchema.isObjectSchema()) {
            ClassOrInterfaceDeclaration coid = ClassContainer.getInstance(Conf.TARGET_PROJECT_PATH).getByQualifier()
                    .get(describe);
            if (coid == null) {
                // 往往是泛型返回值或是类库中会被认为是ObjectSchema的类型
                System.out.println(describe);
                return new ChaosStructureBodyProcessResult().jsonSchema(jsonSchema);
            }
            Class<?> clazz = cl.loadClass(qualifierForClassLoader(coid));
            return new KeyValueStructureBodyProcessResult().clazz(coid).reflectClass(clazz)
                    .objectSchema(jsonSchema.asObjectSchema());

        } else if (jsonSchema.isValueTypeSchema()) {
            JsonType jsonType;
            NumberFormatType numberFormat = null;
            if (jsonSchema.isStringSchema()) {
                jsonType = JsonType.string;
            } else if (jsonSchema.isNumberSchema()) {
                jsonType = JsonType.number;

                if (!jsonSchema.isIntegerSchema()) {
                    numberFormat = NumberFormatType.f1oat;
                } else if (StringUtils.equalsAny(type.describe(), "java.lang.Integer", "int")) {
                    numberFormat = NumberFormatType.int32;
                } else if (StringUtils.equalsAny(type.describe(), "java.lang.Long", "long")) {
                    numberFormat = NumberFormatType.int64;
                } else {
                    numberFormat = NumberFormatType.inT;
                }
            } else if (jsonSchema.isBooleanSchema()) {
                jsonType = JsonType.bool;
            } else {
                throw new RuntimeException("impossible unless bug");
            }
            return new ValueStructureBodyProcessResult().valueStructureJsonType(jsonType)
                    .valueStructureNumberFormat(numberFormat);

        } else {
            return new ChaosStructureBodyProcessResult().jsonSchema(jsonSchema);
        }
    }

    private boolean isArray(ResolvedType type) {
        return type.isArray();
    }

    private ResolvedType getArrayElementType(ResolvedType arrayType) {
        return arrayType.asArrayType().getComponentType();
    }

    private boolean isJUC(ResolvedType type) {
        if (type.isReferenceType()) {
            ResolvedReferenceType referenceType = type.asReferenceType();
            return referenceType.getAllInterfacesAncestors().stream()
                    .anyMatch(ancestor -> ancestor.getId().equals("java.util.Collection"));
        }
        return false;
    }

    private ResolvedType getJUCElementType(ResolvedType JUCType) {
        return Iterables.getOnlyElement(JUCType.asReferenceType().getTypeParametersMap()).b;
    }

    private boolean isPage(ResolvedType type) {
        if (type.isReferenceType()) {
            return type.asReferenceType().getId().equals(Conf.COMMON_PAGE_TYPE_QUALIFIER);
        }
        return false;
    }

    private ResolvedType getPageElementType(ResolvedType pageType) {
        return Iterables.getOnlyElement(pageType.asReferenceType().getTypeParametersMap()).b;
    }

    private JsonSchema generateSchema(String describe) {
        JavaType javaType;
        try {
            javaType = new TypeFactory(null) {
                private static final long serialVersionUID = -8151903006798193420L;

                @Override
                public ClassLoader getClassLoader() {
                    return cl;
                }
            }.constructFromCanonical(describe);
        } catch (IllegalArgumentException e) {
            log.warn("TypeFactory.constructFromCanonical({})", describe);
            return null;
        }
        try {
            return jsg.generateSchema(javaType);
        } catch (JsonMappingException e) {
            log.warn("jsg.generateSchema({})", javaType);
            return null;
        }
    }

    private String qualifierForClassLoader(ClassOrInterfaceDeclaration coid) {
        StringBuilder qualifierForClassLoader = new StringBuilder(64);
        this.qualifierForClassLoader(qualifierForClassLoader, coid);
        return qualifierForClassLoader.toString();
    }

    private void qualifierForClassLoader(StringBuilder qualifier, TypeDeclaration<?> node) {
        node.getParentNode().ifPresent(parent -> {
            if (parent instanceof TypeDeclaration) {
                this.qualifierForClassLoader(qualifier, (TypeDeclaration<?>) parent);
                qualifier.append("$");
                qualifier.append(node.getNameAsString());
            } else {
                node.getFullyQualifiedName().ifPresent(qualifier::append);
            }
        });
    }

}
