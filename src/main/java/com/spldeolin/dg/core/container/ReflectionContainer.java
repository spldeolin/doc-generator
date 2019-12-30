package com.spldeolin.dg.core.container;

import java.nio.file.Path;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.spldeolin.dg.Conf;
import com.spldeolin.dg.core.classloader.SpringBootFatJarClassLoaderFactory;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2019-12-30
 */
@Log4j2
public class ReflectionContainer {

    private static final JsonSchemaGenerator jsg = new JsonSchemaGenerator(new ObjectMapper());

    private BiMap<ClassOrInterfaceDeclaration, Class<?>> classes = HashBiMap.create();

    private BiMap<ClassOrInterfaceDeclaration, JsonSchema> jsonSchemasByCoid = HashBiMap.create();

    private BiMap<String, JsonSchema> jsonSchemasByQualifier = HashBiMap.create();

    @Getter
    private final Path path;

    private static Map<Path, ReflectionContainer> instances = Maps.newConcurrentMap();

    public static ReflectionContainer getInstance(Path path) {
        ReflectionContainer result = instances.get(path);
        if (result == null) {
            result = new ReflectionContainer(path);
            instances.put(path, result);
        }
        return result;
    }

    private ReflectionContainer(Path path) {
        this.path = path;
    }

    public JsonSchema getJsonSchemasByQualifier(String qualifier) {
        JsonSchema result = jsonSchemasByQualifier.get(qualifier);
        if (result == null) {
            ClassOrInterfaceDeclaration coid = ClassContainer.getInstance(path).getByQualifier().get(qualifier);
            String qualifierForClassLoader = this.qualifierForClassLoader(coid);
            try {
                Class<?> clazz = SpringBootFatJarClassLoaderFactory.create(Conf.TARGET_SPRING_BOOT_FAT_JAR_PATH)
                        .loadClass(qualifierForClassLoader);
                result = jsg.generateSchema(clazz);
            } catch (ClassNotFoundException | JsonMappingException | NoClassDefFoundError e) {
                log.warn("{} [{}]", e.getClass().getSimpleName(), qualifierForClassLoader);
            }
            if (result != null) {
                jsonSchemasByQualifier.put(qualifier, result);
            }
        }
        return result;
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
