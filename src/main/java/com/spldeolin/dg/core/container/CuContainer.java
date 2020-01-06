package com.spldeolin.dg.core.container;

import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.ProjectRoot;
import com.github.javaparser.utils.SourceRoot;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.spldeolin.dg.core.classloader.ClassLoaderCollectionStrategy;
import com.spldeolin.dg.core.classloader.SpringBootFatJarClassLoader;
import lombok.Getter;
import lombok.Value;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2019-12-06
 */
@Log4j2
public class CuContainer {

    private static final int EXPECTED = 5500;

    @Getter
    private Path path;

    @Getter
    private Collection<CompilationUnit> all = Lists.newLinkedList();

    private Collection<Report> reports = Sets.newTreeSet();

    private static Map<Path, CuContainer> instances = Maps.newConcurrentMap();

    ;

    public static CuContainer getInstance(Path path) {
        CuContainer result = instances.get(path);
        if (result == null) {
            result = new CuContainer(path);
            instances.put(path, result);
        }
        return result;
    }


    private CuContainer(Path path) {
        long start = System.currentTimeMillis();
        this.path = path;

        this.listSoruceRoots(path).forEach(sourceRoot -> this.parseCus(sourceRoot, all));

        log.debug("CompilationUnitContainer构建完毕，共从[{}]解析到[{}]个CompilationUnit，耗时[{}]毫秒", path, all.size(),
                System.currentTimeMillis() - start);
        reports.forEach(report -> log.info("\t[{}]模块耗时[{}]毫秒", report.getPath(), report.getElapsed()));

        if (EXPECTED < all.size() + 100) {
            log.warn("CompilationUnitContainer.EXPECTED[{}]过小，可能会引发扩容降低性能，建议扩大这个值。（CompilationUnitContainer.all[{}]）",
                    EXPECTED, all.size());
        }
    }

    private Collection<SourceRoot> listSoruceRoots(Path path) {
        URLClassLoader classloader = SpringBootFatJarClassLoader.classLoader;
        ProjectRoot projectRoot = new ClassLoaderCollectionStrategy(classloader).collect(path);
        projectRoot.addSourceRoot(path);
        return projectRoot.getSourceRoots();
    }

    private void parseCus(SourceRoot sourceRoot, Collection<CompilationUnit> all) {
        long start = System.currentTimeMillis();
        try {
            List<ParseResult<CompilationUnit>> parseResults = sourceRoot.tryToParse();
            for (ParseResult<CompilationUnit> parseResult : parseResults) {
                if (parseResult.isSuccessful()) {
                    parseResult.getResult().ifPresent(cu -> {
//                        cu.getStorage().ifPresent(storage -> log.debug("CompilationUnit : {}", storage.getPath()));
                        all.add(cu);
                    });
                } else {
                    log.warn("无法正确被解析，跳过[{}]", parseResult.getProblems());
                }
            }
            List<String> pathParts = Lists.newArrayList(
                    sourceRoot.getRoot().toString().split(Pattern.quote(System.getProperty("file.separator"))));
            reports.add(new Report(pathParts.get(pathParts.size() - 4), System.currentTimeMillis() - start));
        } catch (IOException e) {
            log.error("StaticJavaParser.parse失败", e);
        }
    }

    @Value
    private static class Report implements Comparable<Report> {

        private String path;

        private Long elapsed;

        @Override
        public int compareTo(Report that) {
            return that.getElapsed().compareTo(this.getElapsed());
        }

    }

}