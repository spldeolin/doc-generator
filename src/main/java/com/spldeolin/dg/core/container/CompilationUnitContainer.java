package com.spldeolin.dg.core.container;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy;
import com.github.javaparser.utils.SourceRoot;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.spldeolin.dg.core.exception.PrimaryTypeAbsentException;
import com.spldeolin.dg.core.exception.QualifierAbsentException;
import lombok.Data;
import lombok.Value;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2019-12-06
 */
@Log4j2
@Data
public class CompilationUnitContainer {

    private static final int EXPECTED = 5500;

    private final Path path;

    private final Collection<CompilationUnit> all = Lists.newLinkedList();

    private final Map<String, CompilationUnit> byPrimaryClassQualifier = Maps.newHashMapWithExpectedSize(EXPECTED);

    private final Multimap<String, CompilationUnit> byPrimaryClassName = ArrayListMultimap.create(EXPECTED, 1);

    private final Multimap<String, CompilationUnit> byClassQualifier = ArrayListMultimap.create(EXPECTED, 1);

    private final Multimap<String, CompilationUnit> byClassName = ArrayListMultimap.create(EXPECTED, 1);

    private final Multimap<String, CompilationUnit> byPackageQualifier = ArrayListMultimap.create(EXPECTED, 1);

    private final Collection<Report> reports = Sets.newTreeSet();

    /* package-private */ CompilationUnitContainer(Path path) {
        long start = System.currentTimeMillis();
        this.path = path;
        this.listSoruceRoots(path).forEach(sourceRoot -> this.parseCus(sourceRoot).forEach(cu -> {
            all.add(cu);

            // 忽略没有主类的cu
            cu.getPrimaryType().ifPresent(primaryType -> {
                byPrimaryClassQualifier
                        .put(primaryType.getFullyQualifiedName().orElseThrow(PrimaryTypeAbsentException::new), cu);
                byPrimaryClassName.put(primaryType.getNameAsString(), cu);
            });

            // 忽略没有type的cu（一般是整个被注释了）
            cu.getTypes().forEach(type -> {
                byClassQualifier.put(type.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new), cu);
                byClassName.put(type.getNameAsString(), cu);
            });

            // 忽略没有package的cu（一般是整个被注释了）
            cu.getPackageDeclaration().ifPresent(pkg -> byPackageQualifier.put(pkg.getNameAsString(), cu));
        }));

        log.info("CompilationUnitContainer构建完毕，共从[{}]解析到[{}]个CompilationUnit，耗时[{}]毫秒", path, all.size(),
                System.currentTimeMillis() - start);
        reports.forEach(report -> log.info("\t[{}]模块耗时[{}]毫秒", report.getPath(), report.getElapsed()));


        if (EXPECTED < all.size() + 100) {
            log.warn("CompilationUnitContainer.EXPECTED[{}]过小，可能会引发扩容降低性能，建议扩大这个值。（CompilationUnitContainer.all[{}]）",
                    EXPECTED, all.size());
        }
    }

    private Collection<SourceRoot> listSoruceRoots(Path path) {
        Collection<SourceRoot> sourceRoots = new SymbolSolverCollectionStrategy().collect(path).getSourceRoots();
        sourceRoots.add(new SourceRoot(path));
        return sourceRoots;
    }

    private Collection<CompilationUnit> parseCus(SourceRoot sourceRoot) {
        Collection<CompilationUnit> result = Lists.newArrayList();
        long start = System.currentTimeMillis();
        try {
            List<ParseResult<CompilationUnit>> parseResults = sourceRoot.tryToParse();
            for (ParseResult<CompilationUnit> parseResult : parseResults) {
                parseResult.ifSuccessful(result::add);
                if (parseResult.getProblems().size() > 0) {
                    log.warn("无法正确被解析，跳过[{}]", parseResult.getProblems());
                }
            }

            List<String> pathParts = Lists.newArrayList(sourceRoot.getRoot().toString().split(File.separator));
            reports.add(new Report(pathParts.get(pathParts.size() - 4), System.currentTimeMillis() - start));
        } catch (IOException e) {
            log.error("StaticJavaParser.parse失败", e);
        }
        return result;
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