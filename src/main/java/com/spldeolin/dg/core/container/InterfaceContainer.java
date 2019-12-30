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
public class InterfaceContainer {

    public static final int EXPECTED = 5600;

    @Getter
    private final Path path;

    @Getter
    private Collection<ClassOrInterfaceDeclaration> all = Lists.newLinkedList();

    private Map<String, ClassOrInterfaceDeclaration> byQualifier = Maps.newHashMapWithExpectedSize(EXPECTED);

    private Multimap<String, ClassOrInterfaceDeclaration> byPackageQualifier = ArrayListMultimap.create(EXPECTED, 1);

    private Multimap<String, ClassOrInterfaceDeclaration> byInterfaceName = ArrayListMultimap.create(EXPECTED, 1);

    private static Map<Path, InterfaceContainer> instancesCache = Maps.newConcurrentMap();

    public static InterfaceContainer getInstance(Path path) {
        InterfaceContainer result = instancesCache.get(path);
        if (result == null) {
            result = new InterfaceContainer(path);
            instancesCache.put(path, result);
        }
        return result;
    }

    private InterfaceContainer(Path path) {
        CuContainer cuContainer = CuContainer.getInstance(path);
        long start = System.currentTimeMillis();
        this.path = path;
        cuContainer.getByPackageQualifier().asMap().forEach((packageQualifier, cus) -> cus.forEach(cu -> {
            cu.findAll(ClassOrInterfaceDeclaration.class).stream().filter(ClassOrInterfaceDeclaration::isInterface)
                    .forEach(iinterface -> all.add(iinterface));
        }));

        log.info("InterfaceContainer构建完毕，共从[{}]解析到[{}]个Coid，耗时[{}]毫秒", path, all.size(),
                System.currentTimeMillis() - start);

        if (EXPECTED < all.size() + 100) {
            log.warn("InterfaceContainer.EXPECTED[{}]过小，可能会引发扩容降低性能，建议扩大这个值。（InterfaceContainer.all[{}]）", EXPECTED,
                    all.size());
        }
    }

    public Map<String, ClassOrInterfaceDeclaration> getByQualifier() {
        if (byQualifier.size() == 0) {
            all.forEach(iinterface -> byQualifier
                    .put(iinterface.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new), iinterface));
        }
        return byQualifier;
    }

    public Multimap<String, ClassOrInterfaceDeclaration> getByPackageQualifier() {
        if (byPackageQualifier.size() == 0) {
            CuContainer.getInstance(path).getByPackageQualifier().asMap()
                    .forEach((packageQualifier, cus) -> cus.forEach(cu -> {
                        cu.findAll(ClassOrInterfaceDeclaration.class)
                                .forEach(iinterface -> byPackageQualifier.put(packageQualifier, iinterface));
                    }));
        }
        return byPackageQualifier;
    }

    public Multimap<String, ClassOrInterfaceDeclaration> getByInterfaceName() {
        if (byInterfaceName.size() == 0) {
            all.forEach(iinterface -> byInterfaceName.put(iinterface.getNameAsString(), iinterface));
        }
        return byInterfaceName;
    }

}