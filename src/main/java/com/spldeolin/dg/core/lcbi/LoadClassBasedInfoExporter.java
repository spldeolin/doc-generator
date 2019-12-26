package com.spldeolin.dg.core.lcbi;

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
public class LoadClassBasedInfoExporter {

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
        new LoadClassBasedInfoExporter().process();
    }

    /*
        process
     */

    private void process() {
        log.info("start process");
        Path path = Paths.get(Conf.PROJECT_PATH);

        Collection<PojoSchemaDto> pojoJsonSchemas = Lists.newLinkedList();
        Collection<HandlerMappingDto> handlerMappings = Lists.newLinkedList();
        this.listSoruceRoots(path).forEach(sourceRoot -> this.parseCus(sourceRoot).forEach(
                cu -> cu.findAll(ClassOrInterfaceDeclaration.class)
                        .forEach(coid -> coid.getFullyQualifiedName().ifPresent(pojoQualifier -> {

                            if (coid.getAnnotationByName("Data").isPresent()) {
                                log.info(pojoQualifier);
                                String qualifierForClassLoader = qualifierForClassLoader(coid);
                                SchemaFactoryWrapper sfw = new SchemaFactoryWrapper();
                                try {
                                    om.acceptJsonFormatVisitor(Class.forName(qualifierForClassLoader), sfw);
                                } catch (ClassNotFoundException e) {
                                    log.warn("Class.forName({})", qualifierForClassLoader);
                                } catch (JsonMappingException e) {
                                    log.warn("om.acceptJsonFormatVisitor(Class.forName({}))", pojoQualifier, e);
                                }
                                pojoJsonSchemas.add(new PojoSchemaDto().setPojoQualifier(pojoQualifier)
                                        .setJsonSchema(sfw.finalSchema()));
                            }

                            if (coid.getAnnotationByName("RestController").isPresent()) {
                                log.info(pojoQualifier);
                                String qualifierForClassLoader = qualifierForClassLoader(coid);
                                try {
                                    handlerMappings
                                            .addAll(new UriProcessor().process(Class.forName(qualifierForClassLoader)));
                                } catch (ClassNotFoundException e) {
                                    log.warn("Class.forName({})", qualifierForClassLoader);
                                }
                            }
                        }))));

        log.info("{} CU(pojo) has been parsed and taken {} ms complete.", pojoJsonSchemas.size(),
                sw.elapsed(TimeUnit.MILLISECONDS));
        reports.forEach(report -> log.info("\t{} took [{}]ms.", report.getPath(), report.getElapsed()));


        File jsonSchemaOutput = new File(Conf.POJO_SCHEMA_PATH);
        try {
            FileUtils.writeStringToFile(jsonSchemaOutput, Jsons.toJson(pojoJsonSchemas), StandardCharsets.UTF_8);
            log.info("POJO schema has been export into [{}].", jsonSchemaOutput);
        } catch (IOException e) {
            log.error("FileUtils.writeStringToFile", e);
        }

        File handlerMappingOutput = new File(Conf.HANDLER_MAPPING_PATH);
        try {
            FileUtils.writeStringToFile(handlerMappingOutput, Jsons.toJson(handlerMappings), StandardCharsets.UTF_8);
            log.info("Handler mapping has been export into [{}].", handlerMappingOutput);
        } catch (IOException e) {
            log.error("FileUtils.writeStringToFile", e);
        }
    }

    private String qualifierForClassLoader(ClassOrInterfaceDeclaration coid) {
        StringBuilder qualifierForClassLoader = new StringBuilder(64);
        qualifierForClassLoader(qualifierForClassLoader, coid);
        return qualifierForClassLoader.toString();
    }

    private void qualifierForClassLoader(StringBuilder qualifier, TypeDeclaration<?> node) {
        node.getParentNode().ifPresent(parent -> {
            if (parent instanceof TypeDeclaration) {
                qualifierForClassLoader(qualifier, (TypeDeclaration<?>) parent);
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
