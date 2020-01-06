package com.spldeolin.dg.core.container;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.spldeolin.dg.core.exception.QualifierAbsentException;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2019-12-06
 */
@Log4j2
public class EnumContainer {

    public static final int EXPECTED = 5200;

    @Getter
    private Path path;

    @Getter
    private Collection<EnumDeclaration> all = Lists.newLinkedList();

    private Map<String, EnumDeclaration> byEnumQualifier = Maps.newHashMapWithExpectedSize(EXPECTED);

    private Multimap<String, EnumDeclaration> byEnumName = ArrayListMultimap.create(EXPECTED, 1);

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

        if (EXPECTED < all.size() + 100) {
            log.warn("EnumContainer.EXPECTED[{}]过小，可能会引发扩容降低性能，建议扩大这个值。（EnumContainer.all[{}]）", EXPECTED, all.size());
        }
    }

    public Map<String, EnumDeclaration> getByEnumQualifier() {
        if (byEnumQualifier.size() == 0) {
            all.forEach(enumDeclaration -> byEnumQualifier
                    .put(enumDeclaration.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new),
                            enumDeclaration));
        }
        return byEnumQualifier;
    }

    public Multimap<String, EnumDeclaration> getByEnumName() {
        if (byEnumName.size() == 0) {
            all.forEach(enumDeclaration -> byEnumName.put(enumDeclaration.getNameAsString(), enumDeclaration));
        }
        return byEnumName;
    }

}
