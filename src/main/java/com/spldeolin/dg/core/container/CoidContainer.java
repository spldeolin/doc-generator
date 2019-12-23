package com.spldeolin.dg.core.container;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
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
public class CoidContainer {

    private static final int EXPECTED = 5500;

    private final Collection<Path> paths = Lists.newLinkedList();

    private final Collection<ClassOrInterfaceDeclaration> all = Lists.newLinkedList();

    private final Map<String, ClassOrInterfaceDeclaration> byCoidQualifier = Maps.newHashMapWithExpectedSize(EXPECTED);

    private final Multimap<String, ClassOrInterfaceDeclaration> byPackageQualifier = ArrayListMultimap
            .create(EXPECTED, 1);

    private final Multimap<String, ClassOrInterfaceDeclaration> byCoidName = ArrayListMultimap.create(EXPECTED, 1);

    private final Multimap<String, String> coidQulifierByCoidName = ArrayListMultimap.create(EXPECTED, 1);

    /* package-private */ CoidContainer(Path path) {
        CompilationUnitContainer cuContainer = ContainerFactory.compilationUnitContainer(path);
        long start = System.currentTimeMillis();
        paths.add(path);
        cuContainer.getByPackageQualifier().asMap().forEach((packageQualifier, cus) -> cus.forEach(cu -> {
            cu.findAll(ClassOrInterfaceDeclaration.class).forEach(coid -> {
                all.add(coid);

                String coidQualifier = Joiner.on(".").join(packageQualifier, coid.getNameAsString());
                byCoidQualifier.put(coidQualifier, coid);
                byPackageQualifier.put(packageQualifier, coid);
                byCoidName.put(coid.getNameAsString(), coid);
                coidQulifierByCoidName.put(coid.getNameAsString(), coidQualifier);
            });
        }));

        log.info("CoidContainer构建完毕，共从[{}]解析到[{}]个Coid，耗时[{}]毫秒", path, all.size(), System.currentTimeMillis() - start);

        if (EXPECTED < all.size() + 100) {
            log.warn("CoidContainer.EXPECTED[{}]过小，可能会引发扩容降低性能，建议扩大这个值。（CoidContainer.all[{}]）", EXPECTED, all.size());
        }
    }

    public void putAll(CoidContainer others) {
        this.paths.addAll(others.paths);
        this.all.addAll(others.all);
        this.byCoidQualifier.putAll(others.byCoidQualifier);
        this.byPackageQualifier.putAll(others.byPackageQualifier);
        this.byCoidName.putAll(others.byCoidName);
        this.coidQulifierByCoidName.putAll(others.coidQulifierByCoidName);
    }

}
