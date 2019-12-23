package com.spldeolin.dg.core.container;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
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
public class CoidContainer {

    private static final int EXPECTED = 5500;

    @Getter
    private final Path path;

    @Getter
    private final Collection<ClassOrInterfaceDeclaration> all = Lists.newLinkedList();

    private final Map<String, ClassOrInterfaceDeclaration> byCoidQualifier = Maps.newHashMapWithExpectedSize(EXPECTED);

    private final Multimap<String, ClassOrInterfaceDeclaration> byPackageQualifier = ArrayListMultimap
            .create(EXPECTED, 1);

    private final Multimap<String, ClassOrInterfaceDeclaration> byCoidName = ArrayListMultimap.create(EXPECTED, 1);

    private final Multimap<String, String> coidQulifierByCoidName = ArrayListMultimap.create(EXPECTED, 1);

    /* package-private */ CoidContainer(Path path) {
        CuContainer cuContainer = ContainerFactory.compilationUnitContainer(path);
        long start = System.currentTimeMillis();
        this.path = path;
        cuContainer.getByPackageQualifier().asMap().forEach((packageQualifier, cus) -> cus.forEach(cu -> {
            all.addAll(cu.findAll(ClassOrInterfaceDeclaration.class));
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
            ContainerFactory.compilationUnitContainer(path).getByPackageQualifier().asMap()
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

}
