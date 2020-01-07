package com.spldeolin.dg.core.container;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.spldeolin.dg.core.exception.QualifierAbsentException;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2019-12-06
 */
@Log4j2
public class InterfaceContainer {

    @Getter
    private final Path path;

    @Getter
    private Collection<ClassOrInterfaceDeclaration> all = Lists.newLinkedList();

    private Map<String, ClassOrInterfaceDeclaration> byQualifier;

    private static Map<Path, InterfaceContainer> instances = Maps.newConcurrentMap();

    public static InterfaceContainer getInstance(Path path) {
        InterfaceContainer result = instances.get(path);
        if (result == null) {
            result = new InterfaceContainer(path);
            instances.put(path, result);
        }
        return result;
    }

    private InterfaceContainer(Path path) {
        CuContainer cuContainer = CuContainer.getInstance(path);
        long start = System.currentTimeMillis();
        this.path = path;
        cuContainer.getAll().forEach(cu -> cu.findAll(ClassOrInterfaceDeclaration.class).stream()
                .filter(ClassOrInterfaceDeclaration::isInterface).forEach(iinterface -> all.add(iinterface)));

        log.info("InterfaceContainer构建完毕，共从[{}]解析到[{}]个Coid，耗时[{}]毫秒", path, all.size(),
                System.currentTimeMillis() - start);
    }

    public Map<String, ClassOrInterfaceDeclaration> getByQualifier() {
        if (byQualifier == null) {
            byQualifier = Maps.newHashMapWithExpectedSize(all.size());
            all.forEach(iinterface -> byQualifier
                    .put(iinterface.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new), iinterface));
        }
        return byQualifier;
    }

}
