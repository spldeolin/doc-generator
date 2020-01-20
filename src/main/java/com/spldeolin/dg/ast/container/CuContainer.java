package com.spldeolin.dg.ast.container;

import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.ProjectRoot;
import com.github.javaparser.utils.SourceRoot;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.spldeolin.dg.Conf;
import com.spldeolin.dg.ast.classloader.ClassLoaderCollectionStrategy;
import com.spldeolin.dg.ast.classloader.WarOrFatJarClassLoader;
import lombok.Getter;
import lombok.Value;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2019-12-06
 */
@Log4j2
public class CuContainer {

    @Getter
    private Path path;

    @Getter
    private Collection<CompilationUnit> all = Lists.newLinkedList();

    private static Map<Path, CuContainer> instances = Maps.newConcurrentMap();

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
        List<Report> reports = Lists.newLinkedList();
        this.listSoruceRoots(path).forEach(sourceRoot -> this.parseCus(sourceRoot, all, reports));

        Collections.sort(reports);
        reports.forEach(log::info);
        log.info("(Summary) Collected {} CU from [{}] elapsing {}ms.", all.size(), path.toAbsolutePath(),
                System.currentTimeMillis() - start);
    }

    private Collection<SourceRoot> listSoruceRoots(Path path) {
        URLClassLoader classloader = WarOrFatJarClassLoader.classLoader;
        ProjectRoot projectRoot = new ClassLoaderCollectionStrategy(classloader).collect(path);
        projectRoot.addSourceRoot(path);
        return projectRoot.getSourceRoots();
    }

    private void parseCus(SourceRoot sourceRoot, Collection<CompilationUnit> all, Collection<Report> reports) {
        long start = System.currentTimeMillis();
        int count = 0;
        for (ParseResult<CompilationUnit> parseResult : sourceRoot.tryToParseParallelized()) {
            if (parseResult.isSuccessful()) {
                parseResult.getResult().ifPresent(all::add);
                count++;
            } else {
                log.warn("无法正确被解析，跳过[{}]", parseResult.getProblems());
            }
        }

        if (count > 0) {
            reports.add(new Report("../" + Conf.PROJECT_PATH.toUri().relativize(sourceRoot.getRoot().toUri()).getPath(),
                    count, System.currentTimeMillis() - start));
        }
    }

    @Value
    private static class Report implements Comparable<Report> {

        private String relativePath;

        private Integer count;

        private Long elapsed;

        @Override
        public int compareTo(Report that) {
            return that.getElapsed().compareTo(this.getElapsed());
        }

        @Override
        public String toString() {
            return "Collected " + count + " CU from [" + relativePath + "] elapsing " + elapsed + "ms.";
        }

    }

}