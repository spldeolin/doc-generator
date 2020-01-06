package com.spldeolin.dg.core.processor;

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
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.spldeolin.dg.Conf;
import com.spldeolin.dg.core.classloader.SpringBootFatJarClassLoader;
import com.spldeolin.dg.core.container.ClassContainer;
import com.spldeolin.dg.core.domain.ResultEntry;
import com.spldeolin.dg.core.enums.ResponseBodyStructure;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-01-02
 */
@AllArgsConstructor
@Log4j2
public class ResultProcessor {

    private static final JsonSchemaGenerator jsg = new JsonSchemaGenerator(new ObjectMapper());

    private static final ClassLoader cl = SpringBootFatJarClassLoader.classLoader;

    private final ResolvedType type;

    public ResultEntry process() {
        if (type == null) {
            return new ResultEntry().struct(ResponseBodyStructure.v0id);
        }

        ResultEntry result;
        try {
            if (isArray(type)) {
                // 最外层是 数组
                result = tryProcessNonArrayLikeType(type.asArrayType().getComponentType()).outermostWrapper(2);
            } else if (isJUC(type)) {
                // 最外层是 列表
                result = tryProcessNonArrayLikeType(
                        Iterables.getOnlyElement(type.asReferenceType().getTypeParametersMap()).b).outermostWrapper(2);
            } else if (isPage(type)) {
                // 最外层是 Page对象
                result = tryProcessNonArrayLikeType(
                        Iterables.getOnlyElement(type.asReferenceType().getTypeParametersMap()).b).outermostWrapper(3);
            } else {
                // 单层
                result = tryProcessNonArrayLikeType(type).outermostWrapper(1);
            }
        } catch (Exception e) {
            log.warn(e.getClass().getSimpleName() + ":" + Strings.nullToEmpty(e.getMessage()));
            // as mazy mode
            result = new ResultEntry().struct(ResponseBodyStructure.chaos);
        }

        // try generate json schema for mazy mode
        if (result.struct() == ResponseBodyStructure.chaos) {
            JsonSchema jsonSchema = generateSchema(type.describe());
            if (jsonSchema != null) {
                result.jsonSchema(jsonSchema);
            } else {
                result = new ResultEntry().struct(ResponseBodyStructure.v0id);
            }
        }
        return result;
    }

    private boolean isPage(ResolvedType type) {
        if (type.isReferenceType()) {
            return type.asReferenceType().getId().equals(Conf.COMMON_PAGE_TYPE_QUALIFIER);
        }
        return false;
    }

    private boolean isArray(ResolvedType type) {
        return type.isArray();
    }

    private boolean isJUC(ResolvedType type) {
        if (type.isReferenceType()) {
            ResolvedReferenceType referenceType = type.asReferenceType();
            return referenceType.getAllInterfacesAncestors().stream()
                    .anyMatch(ancestor -> ancestor.getId().equals("java.util.Collection"));
        }
        return false;
    }

    private ResultEntry tryProcessNonArrayLikeType(ResolvedType componentType) throws ClassNotFoundException {
        String describe = componentType.describe();
        JsonSchema jsonSchema = generateSchema(describe);
        if (jsonSchema == null) {
            return new ResultEntry().struct(ResponseBodyStructure.v0id);
        }

        if (jsonSchema.isObjectSchema()) {
            ClassOrInterfaceDeclaration coid = ClassContainer.getInstance(Conf.TARGET_PROJECT_PATH).getByQualifier()
                    .get(describe);
            if (coid == null) {
                // 往往是泛型返回值或是类库中会被认为是ObjectSchema的类型
                System.out.println(describe);
                return new ResultEntry().struct(ResponseBodyStructure.chaos);
            }
            Class<?> clazz = cl.loadClass(qualifierForClassLoader(coid));
            return new ResultEntry().struct(ResponseBodyStructure.keyValLike).clazz(coid).reflectClass(clazz);

        } else if (jsonSchema.isValueTypeSchema()) {
            return new ResultEntry().struct(ResponseBodyStructure.valueLike).jsonSchema(jsonSchema);

        } else {
            return new ResultEntry().struct(ResponseBodyStructure.chaos);
        }
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
