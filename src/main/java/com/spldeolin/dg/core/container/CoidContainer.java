package com.spldeolin.dg.core.container;

import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.spldeolin.dg.Conf;
import com.spldeolin.dg.core.classloader.SpringBootFatJarClassLoaderBuilder;
import com.spldeolin.dg.core.exception.QualifierAbsentException;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2019-12-06
 */
@Log4j2
public class CoidContainer {

    private static final int EXPECTED = 5600;

    private static final ObjectMapper om = new ObjectMapper();

    private static final URLClassLoader classLoader = SpringBootFatJarClassLoaderBuilder
            .getInstance(Conf.TARGET_SPRING_BOOT_FAT_JAR_PATH).build();

    @Getter
    private final Path path;

    @Getter
    private Collection<ClassOrInterfaceDeclaration> all = Lists.newLinkedList();

    @Getter
    private Map<String, JsonSchema> jsonSchemasByPojoQualifier = Maps.newHashMapWithExpectedSize(EXPECTED);

    private Map<String, ClassOrInterfaceDeclaration> byCoidQualifier = Maps.newHashMapWithExpectedSize(EXPECTED);

    private Multimap<String, ClassOrInterfaceDeclaration> byPackageQualifier = ArrayListMultimap.create(EXPECTED, 1);

    private Multimap<String, ClassOrInterfaceDeclaration> byCoidName = ArrayListMultimap.create(EXPECTED, 1);

    private Multimap<String, String> coidQulifierByCoidName = ArrayListMultimap.create(EXPECTED, 1);

    private static Map<Path, CoidContainer> instancesCache = Maps.newConcurrentMap();

    public static CoidContainer getInstance(Path path) {
        CoidContainer result = instancesCache.get(path);
        if (result == null) {
            result = new CoidContainer(path);
            instancesCache.put(path, result);
        }
        return result;
    }

    private CoidContainer(Path path) {
        CuContainer cuContainer = CuContainer.getInstance(path);
        long start = System.currentTimeMillis();
        this.path = path;
        cuContainer.getByPackageQualifier().asMap().forEach((packageQualifier, cus) -> cus.forEach(cu -> {
            cu.findAll(ClassOrInterfaceDeclaration.class).forEach(coid -> {

                coid.getFullyQualifiedName().ifPresent(qualifier -> {

                    if (!coid.isInterface()) {
                        String qualifierForClassLoader = this.qualifierForClassLoader(coid);
                        SchemaFactoryWrapper sfw = new SchemaFactoryWrapper();
                        try {
                            Class<?> clazz = classLoader.loadClass(qualifierForClassLoader);
                            om.acceptJsonFormatVisitor(clazz, sfw);
                        } catch (ClassNotFoundException | JsonMappingException| NoClassDefFoundError e) {
                            log.warn("{} [{}]", e.getClass().getSimpleName(), qualifierForClassLoader);
                        }
                        jsonSchemasByPojoQualifier.put(qualifier, sfw.finalSchema());
                    }
                });

                all.add(coid);
            });
        }));

        log.info("CoidContainer构建完毕，共从[{}]解析到[{}]个Coid，耗时[{}]毫秒", path, all.size(), System.currentTimeMillis() - start);

        if (EXPECTED < all.size() + 100) {
            log.warn("CoidContainer.EXPECTED[{}]过小，可能会引发扩容降低性能，建议扩大这个值。（CoidContainer.all[{}]）", EXPECTED, all.size());
        }
    }

    public Map<String, ClassOrInterfaceDeclaration> getByCoidQualifier() {
        if (byCoidQualifier.size() == 0) {
            all.forEach(coid -> byCoidQualifier
                    .put(coid.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new), coid));
        }
        return byCoidQualifier;
    }

    public Multimap<String, ClassOrInterfaceDeclaration> getByPackageQualifier() {
        if (byPackageQualifier.size() == 0) {
            CuContainer.getInstance(path).getByPackageQualifier().asMap()
                    .forEach((packageQualifier, cus) -> cus.forEach(cu -> {
                        cu.findAll(ClassOrInterfaceDeclaration.class)
                                .forEach(coid -> byPackageQualifier.put(packageQualifier, coid));
                    }));
        }
        return byPackageQualifier;
    }

    public Multimap<String, ClassOrInterfaceDeclaration> getByCoidName() {
        if (byCoidName.size() == 0) {
            all.forEach(coid -> byCoidName.put(coid.getNameAsString(), coid));
        }
        return byCoidName;
    }

    public Multimap<String, String> getCoidQulifierByCoidName() {
        if (coidQulifierByCoidName.size() == 0) {
            all.forEach(coid -> coidQulifierByCoidName.put(coid.getNameAsString(),
                    coid.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new)));
        }
        return coidQulifierByCoidName;
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
