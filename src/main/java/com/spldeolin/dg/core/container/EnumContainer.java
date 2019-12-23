package com.spldeolin.dg.core.container;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2019-12-06
 */
@Log4j2
@Data
public class EnumContainer {

    private static final int EXPECTED = 5200;

    private final Path path;

    private final Collection<EnumDeclaration> all = Lists.newLinkedList();

    private final Map<String, EnumDeclaration> byEnumQualifier = Maps.newHashMapWithExpectedSize(EXPECTED);

    private final Multimap<String, EnumDeclaration> byPackageQualifier = ArrayListMultimap.create(EXPECTED, 1);

    private final Multimap<String, EnumDeclaration> byEnumName = ArrayListMultimap.create(EXPECTED, 1);

    private final Multimap<String, String> enumQulifierByEnumName = ArrayListMultimap.create(EXPECTED, 1);

    /* package-private */ EnumContainer(Path path) {
        CompilationUnitContainer cuContainer = ContainerFactory.compilationUnitContainer(path);
        long start = System.currentTimeMillis();
        this.path = path;
        cuContainer.getByPackageQualifier().asMap().forEach((packageQualifier, cus) -> cus.forEach(cu -> {
            cu.findAll(EnumDeclaration.class).forEach(enumDeclaration -> {
                all.add(enumDeclaration);

                String enumQualifier = Joiner.on(".").join(packageQualifier, enumDeclaration.getNameAsString());
                byEnumQualifier.put(enumQualifier, enumDeclaration);
                byPackageQualifier.put(packageQualifier, enumDeclaration);
                byEnumName.put(enumDeclaration.getNameAsString(), enumDeclaration);
                enumQulifierByEnumName.put(enumDeclaration.getNameAsString(), enumQualifier);
            });
        }));

        log.info("EnumContainer构建完毕，共从[{}]解析到[{}]个EnumDeclaration，耗时[{}]毫秒", path, all.size(),
                System.currentTimeMillis() - start);

        if (EXPECTED < all.size() + 100) {
            log.warn("EnumContainer.EXPECTED[{}]过小，可能会引发扩容降低性能，建议扩大这个值。（EnumContainer.all[{}]）", EXPECTED, all.size());
        }
    }

}
