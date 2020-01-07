package com.spldeolin.dg.core.container;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.spldeolin.dg.core.exception.QualifierAbsentException;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2019-12-06
 */
@Log4j2
public class EnumContainer {

    @Getter
    private Path path;

    @Getter
    private Collection<EnumDeclaration> all = Lists.newLinkedList();

    private Map<String, EnumDeclaration> byEnumQualifier;

    private static Map<Path, EnumContainer> instances = Maps.newConcurrentMap();

    public static EnumContainer getInstance(Path path) {
        EnumContainer result = instances.get(path);
        if (result == null) {
            result = new EnumContainer(path);
            instances.put(path, result);
        }
        return result;
    }

    private EnumContainer(Path path) {
        CuContainer cuContainer = CuContainer.getInstance(path);
        long start = System.currentTimeMillis();
        this.path = path;
        cuContainer.getAll().forEach(cu -> all.addAll(cu.findAll(EnumDeclaration.class)));

        log.info("EnumContainer构建完毕，共从[{}]解析到[{}]个EnumDeclaration，耗时[{}]毫秒", path, all.size(),
                System.currentTimeMillis() - start);
    }

    public Map<String, EnumDeclaration> getByEnumQualifier() {
        if (byEnumQualifier == null) {
            byEnumQualifier = Maps.newHashMapWithExpectedSize(all.size());
            all.forEach(enumDeclaration -> byEnumQualifier
                    .put(enumDeclaration.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new),
                            enumDeclaration));
        }
        return byEnumQualifier;
    }

}
