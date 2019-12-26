package com.spldeolin.dg.core.jsonschema;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy;
import com.github.javaparser.utils.SourceRoot;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.spldeolin.dg.Conf;
import com.spldeolin.dg.core.util.Jsons;
import lombok.Value;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2019-12-16
 */
@Log4j2
public class PojoJsonSchemaExporter {

    Collection<Report> reports = Sets.newTreeSet();

    /*
        guava stopwatch
     */

    private final Stopwatch sw = Stopwatch.createStarted();

    private final Stopwatch sw2 = Stopwatch.createStarted();

    /*
        jaskson
     */

    private final ObjectMapper om = new ObjectMapper();

    public static void main(String[] args) {
        new PojoJsonSchemaExporter().process();
    }

    /*
        process
     */

    private void process() {
        log.info("start process");
        Path path = Paths.get(Conf.PROJECT_PATH);

        Collection<PojoJsonSchemaData> pojoJsonSchemas = Lists.newLinkedList();
        this.listSoruceRoots(path).forEach(sourceRoot -> this.parseCus(sourceRoot).forEach(cu -> {
            cu.findAll(ClassOrInterfaceDeclaration.class).stream()
                    .filter(coid -> coid.getAnnotationByName("Data").isPresent())
                    .forEach(coid -> coid.getFullyQualifiedName().ifPresent(pojoQualifier -> {
                        log.info(pojoQualifier);

                        SchemaFactoryWrapper sfw = new SchemaFactoryWrapper();
                        StringBuilder qualifierForClassLoader = new StringBuilder(64);
                        this.classForQualifier(qualifierForClassLoader, coid);
                        try {
                            om.acceptJsonFormatVisitor(Class.forName(qualifierForClassLoader.toString()), sfw);
                        } catch (ClassNotFoundException e) {
                            log.warn("Class.forName({})", qualifierForClassLoader.toString());
                        } catch (JsonMappingException e) {
                            log.warn("om.acceptJsonFormatVisitor(Class.forName({}))", pojoQualifier, e);
                        }
                        pojoJsonSchemas.add(new PojoJsonSchemaData(pojoQualifier, sfw.finalSchema()));
                    }));
        }));

        log.info("{} CU(pojo) has been parsed and taken {} ms complete.", pojoJsonSchemas.size(),
                sw.elapsed(TimeUnit.MILLISECONDS));
        reports.forEach(report -> log.info("\t{} took [{}]ms.", report.getPath(), report.getElapsed()));


        File jsonSchemaOutput = Paths.get(Conf.POJO_SCHEMA_PATH).toFile();
        try {
            FileUtils.writeStringToFile(jsonSchemaOutput, Jsons.toJson(pojoJsonSchemas), StandardCharsets.UTF_8);
            log.info("POJO JSON schema has been export into [{}].", jsonSchemaOutput);
        } catch (IOException e) {
            log.error("FileUtils.writeStringToFile", e);
        }
    }

    private void classForQualifier(StringBuilder qualifier, TypeDeclaration<?> node) {
        node.getParentNode().ifPresent(parent -> {
            if (parent instanceof TypeDeclaration) {
                classForQualifier(qualifier, (TypeDeclaration<?>) parent);
                qualifier.append("$");
                qualifier.append(node.getNameAsString());
            } else {
                node.getFullyQualifiedName().ifPresent(qualifier::append);
            }
        });

    }

    private Collection<SourceRoot> listSoruceRoots(Path path) {
        Collection<SourceRoot> sourceRoots = new SymbolSolverCollectionStrategy().collect(path).getSourceRoots();
        sourceRoots.add(new SourceRoot(path));
        return sourceRoots;
    }

    private Collection<CompilationUnit> parseCus(SourceRoot sourceRoot) {
        Collection<CompilationUnit> result = Lists.newArrayList();
        try {
            List<ParseResult<CompilationUnit>> parseResults = sourceRoot.tryToParse();
            for (ParseResult<CompilationUnit> parseResult : parseResults) {
                parseResult.ifSuccessful(result::add);
                if (parseResult.getProblems().size() > 0) {
                    log.warn("parse error [{}]", parseResult.getProblems());
                }
            }

            List<String> pathParts = Lists.newArrayList(sourceRoot.getRoot().toString().split(File.separator));
            reports.add(new Report(pathParts.get(pathParts.size() - 4), sw2.elapsed(TimeUnit.MILLISECONDS)));
            sw2.reset().start();
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
