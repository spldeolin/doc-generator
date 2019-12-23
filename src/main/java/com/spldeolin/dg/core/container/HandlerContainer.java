package com.spldeolin.dg.core.container;

import java.nio.file.Path;
import java.util.Collection;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2019-12-07
 */
@Log4j2
@Data
public class HandlerContainer {

    private static final int EXPECTED = 200;

    private final Collection<Path> paths = Lists.newLinkedList();

    private final Collection<MethodDeclaration> all = Lists.newLinkedList();

    private final Multimap<ClassOrInterfaceDeclaration, MethodDeclaration> byController = ArrayListMultimap
            .create(EXPECTED, 10);

    /* package-private */ HandlerContainer(Path path) {
        CoidContainer coidContainer = ContainerFactory.coidContainer(path);
        long start = System.currentTimeMillis();
        paths.add(path);

        coidContainer.getAll().stream().filter(this::isController).forEach(coid -> {

            if (!coid.getNameAsString().startsWith("Marketing")) {
                return;
            }

            coid.findAll(MethodDeclaration.class).stream().filter(this::isHandler).forEach(method -> {
                all.add(method);
                byController.put(coid, method);
            });
        });

        log.info("HandlerContainer构建完毕，共从[{}]解析到[{}]个Handler，耗时[{}]毫秒", path, all.size(),
                System.currentTimeMillis() - start);

        if (EXPECTED * 10 < all.size() + 100) {
            log.warn("HandlerContainer.EXPECTED[{}]过小，可能会引发扩容降低性能，建议扩大这个值。（HandlerContainer.all[{}]）", EXPECTED,
                    all.size());
        }
    }

    private boolean isController(ClassOrInterfaceDeclaration coid) {
        return coid.getAnnotations().stream()
                .anyMatch(anno -> StringUtils.equalsAny(anno.getNameAsString(), "RestController", "Controller"));
    }

    private boolean isHandler(MethodDeclaration method) {
        return method.getAnnotations().stream().anyMatch(anno -> StringUtils
                .equalsAny(anno.getNameAsString(), "GetMapping", "PostMapping", "PutMapping", "PatchMapping",
                        "DeleteMapping", "RequestMapping"));
    }

    public void putAll(HandlerContainer others) {
        this.paths.addAll(others.paths);
        this.all.addAll(others.all);
        this.byController.putAll(others.byController);
    }

}
