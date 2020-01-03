package com.spldeolin.dg.core.processor;

import java.util.NoSuchElementException;
import org.apache.commons.lang3.tuple.Triple;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
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
import com.spldeolin.dg.core.enums.ResponseBodyMode;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-01-02
 */
@Log4j2
public class ResultProcessor {

    private static final JsonSchemaGenerator jsg = new JsonSchemaGenerator(new ObjectMapper());

    public Triple<ResponseBodyMode, Class<?>, ClassOrInterfaceDeclaration> process(ResolvedType type) {
        if (type == null) {
            return Triple.of(ResponseBodyMode.nothing, null, null);
        }

        // e.g.: UserVo[]
        if (isArray(type)) {
            return tryProcessNonArrayLikeType(type.asArrayType().getComponentType(), true);
        }

        if (isJUC(type)) {
            try {
                // e.g.: public List<UserVo>
                return tryProcessNonArrayLikeType(
                        Iterables.getOnlyElement(type.asReferenceType().getTypeParametersMap()).b, true);
            } catch (NoSuchElementException | IllegalArgumentException e) {
                // e.g.: public Collection
                return Triple.of(ResponseBodyMode.mazy, null, null);
            }
        }

        return tryProcessNonArrayLikeType(type, false);
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

    private Triple<ResponseBodyMode, Class<?>, ClassOrInterfaceDeclaration> tryProcessNonArrayLikeType(
            ResolvedType componentType, boolean isInArray) {
        String describe = componentType.describe();
        ClassOrInterfaceDeclaration coid = ClassContainer.getInstance(Conf.TARGET_PROJECT_PATH).getByQualifier()
                .get(describe);
        if (coid == null) {
            return Triple.of(ResponseBodyMode.nothing, null, null);
        }

        JavaType javaType = new TypeFactory(null) {
            private static final long serialVersionUID = -8151903006798193420L;

            @Override
            public ClassLoader getClassLoader() {
                return SpringBootFatJarClassLoader.classLoader;
            }
        }.constructFromCanonical(describe);

        try {
            JsonSchema jsonSchema = jsg.generateSchema(javaType);
            if (jsonSchema.isObjectSchema()) {
                Class<?> clazz;
                try {
                    clazz = SpringBootFatJarClassLoader.classLoader.loadClass(qualifierForClassLoader(coid));
                } catch (Exception e) {
                    log.warn("class[{}] not found", coid.getFullyQualifiedName());
                    return Triple.of(ResponseBodyMode.nothing, null, null);
                }
                if (isInArray) {
                    return Triple.of(ResponseBodyMode.arrayObject, clazz, coid);
                } else {
                    return Triple.of(ResponseBodyMode.object, clazz, coid);
                }
            } else if (jsonSchema.isValueTypeSchema()) {
                if (isInArray) {
                    return Triple.of(ResponseBodyMode.arrayValue, null, null);
                } else {
                    return Triple.of(ResponseBodyMode.val, null, null);
                }
            } else {
                return Triple.of(ResponseBodyMode.mazy, null, null);
            }
        } catch (JsonProcessingException e) {
            log.warn("jsg.generateSchema({})", javaType, e);
            return Triple.of(ResponseBodyMode.nothing, null, null);
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
