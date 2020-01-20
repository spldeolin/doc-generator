package com.spldeolin.dg.ast.container;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.spldeolin.dg.ast.exception.QualifierAbsentException;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2019-12-06
 */
@Log4j2
public class ClassContainer {

    @Getter
    private final Path path;

    @Getter
    private Collection<ClassOrInterfaceDeclaration> all = Lists.newLinkedList();

    private Map<String, ClassOrInterfaceDeclaration> byQualifier;

    private static Map<Path, ClassContainer> instances = Maps.newConcurrentMap();

    public static ClassContainer getInstance(Path path) {
        ClassContainer result = instances.get(path);
        if (result == null) {
            result = new ClassContainer(path);
            instances.put(path, result);
        }
        return result;
    }

    private ClassContainer(Path path) {
        CuContainer cuContainer = CuContainer.getInstance(path);
        long start = System.currentTimeMillis();
        this.path = path;
        cuContainer.getAll().forEach(
                cu -> cu.findAll(ClassOrInterfaceDeclaration.class).stream().filter(one -> !one.isInterface())
                        .forEach(classDeclaration -> all.add(classDeclaration)));

        log.info("CoidContainer构建完毕，共从[{}]解析到[{}]个Coid，耗时[{}]毫秒", path, all.size(), System.currentTimeMillis() - start);
    }

    public Map<String, ClassOrInterfaceDeclaration> getByQualifier() {
        if (byQualifier == null) {
            byQualifier = Maps.newHashMapWithExpectedSize(all.size());
            all.forEach(classDeclaration -> byQualifier
                    .put(classDeclaration.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new),
                            classDeclaration));
        }
        return byQualifier;
    }

}
