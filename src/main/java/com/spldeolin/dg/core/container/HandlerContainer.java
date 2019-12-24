package com.spldeolin.dg.core.container;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2019-12-07
 */
@Log4j2
@Data
public class HandlerContainer {

    private final Path path;

    private final Collection<MethodDeclaration> all = Lists.newLinkedList();

    private final List<HandlerEntry> withController = Lists.newLinkedList();

    /* package-private */ HandlerContainer(Path path) {
        CoidContainer coidContainer = ContainerFactory.coidContainer(path);
        long start = System.currentTimeMillis();
        this.path = path;

        coidContainer.getAll().stream().filter(this::isController).forEach(coid -> {

            if (!coid.getNameAsString().startsWith("Marketing")) {
                return;
            }

            coid.findAll(MethodDeclaration.class).stream().filter(this::isHandler).forEach(method -> {
                all.add(method);
                withController.add(new HandlerEntry().setController(coid).setHandler(method));
            });
        });

        log.info("HandlerContainer构建完毕，共从[{}]解析到[{}]个Handler，耗时[{}]毫秒", path, all.size(),
                System.currentTimeMillis() - start);
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

    @Data
    @Accessors(chain = true)
    public static class HandlerEntry {

        private ClassOrInterfaceDeclaration controller;

        private MethodDeclaration handler;

    }

}
