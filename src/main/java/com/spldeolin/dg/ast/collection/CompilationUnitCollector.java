package com.spldeolin.dg.ast.collection;

import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Collection;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.ProjectRoot;
import com.github.javaparser.utils.SourceRoot;
import com.google.common.collect.Lists;
import com.spldeolin.dg.Conf;
import com.spldeolin.dg.ast.classloader.ClassLoaderCollectionStrategy;
import com.spldeolin.dg.ast.classloader.WarOrFatJarClassLoader;
import lombok.extern.log4j.Log4j2;

/**
 * CompilationUnit对象的收集器
 *
 * @author Deolin 2020-02-03
 */
@Log4j2
class CompilationUnitCollector {

    Collection<CompilationUnit> collect(Path path) {
        Collection<CompilationUnit> result = Lists.newLinkedList();
        long start = System.currentTimeMillis();
        collectSoruceRoots(path).forEach(sourceRoot -> parseSourceRoot(sourceRoot, result));
        log.info("(Summary) {} CompilationUnit has parsed and collected from [{}] elapsing {}ms.", result.size(),
                path.toAbsolutePath(), System.currentTimeMillis() - start);
        return result;
    }


    private Collection<SourceRoot> collectSoruceRoots(Path path) {
        URLClassLoader classloader = WarOrFatJarClassLoader.classLoader;
        ProjectRoot projectRoot = new ClassLoaderCollectionStrategy(classloader).collect(path);
        projectRoot.addSourceRoot(path);
        return projectRoot.getSourceRoots();
    }

    private void parseSourceRoot(SourceRoot sourceRoot, Collection<CompilationUnit> all) {
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
            log.info("(Detail) {} CompilationUnit has parsed and collected from [{}] elapsing {}ms.", count,
                    "../" + Conf.PROJECT_PATH.relativize(sourceRoot.getRoot()), System.currentTimeMillis() - start);
        }
    }

}
