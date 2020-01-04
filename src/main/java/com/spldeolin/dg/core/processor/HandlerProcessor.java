package com.spldeolin.dg.core.processor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import org.springframework.web.bind.annotation.RequestBody;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedAnnotationDeclaration;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.spldeolin.dg.core.classloader.SpringBootFatJarClassLoader;
import com.spldeolin.dg.core.domain.HandlerEntry;
import com.spldeolin.dg.core.strategy.HandlerFilter;
import com.spldeolin.dg.core.strategy.HandlerResultTypeParser;
import com.spldeolin.dg.core.util.MethodQualifier;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-01-02
 */
@Log4j2
public class HandlerProcessor {

    public Collection<HandlerEntry> process(@RequestBody Collection<ClassOrInterfaceDeclaration> classes,
            HandlerFilter handlerFilter, HandlerResultTypeParser hanlderResultTypeParser) {
        Collection<HandlerEntry> result = Lists.newLinkedList();
        classes.stream().filter(this::isController).forEach(controller -> {

            // reflect controller
            Class<?> reflectController;
            String name = this.qualifierForClassLoader(controller);
            try {
                reflectController = SpringBootFatJarClassLoader.classLoader.loadClass(name);
            } catch (ClassNotFoundException e) {
                log.warn("class[{}] not found", name);
                return;
            }

            Map<String, Method> declaredMethods = listDeclaredMethodAsMap(reflectController);
            controller.getMethods().stream().filter(this::isHandler).forEach(handler -> {
                if (handlerFilter != null && !handlerFilter.filter(controller, handler)) {
                    return;
                }

                HandlerEntry entry = new HandlerEntry();

                // controller
                entry.controller(controller);
                entry.reflectController(reflectController);

                // handler
                String shortestQualifiedSignature = MethodQualifier.getShortestQualifiedSignature(handler);
                entry.shortestQualifiedSignature(shortestQualifiedSignature);
                entry.handler(handler);
                Method reflectHandler = declaredMethods.get(shortestQualifiedSignature);
                if (reflectHandler == null) {
                    log.warn("method[{}] not found", shortestQualifiedSignature);
                    return;
                }
                entry.reflectHandler(reflectHandler);

                // result
                if (hanlderResultTypeParser != null) {
                    entry.handlerResultResolvedType(hanlderResultTypeParser.parse(handler));
                } else {
                    entry.handlerResultResolvedType(handler.getType().resolve());
                }

                // requestBody requestParams pathVariables
                Collection<Parameter> requestParams = Lists.newLinkedList();
                Collection<Parameter> pathVariables = Lists.newLinkedList();
                for (Parameter parameter : handler.getParameters()) {
                    parameter.getAnnotationByName("RequestBody").map(AnnotationExpr::resolve)
                            .filter(resolvedAnno -> "org.springframework.web.bind.annotation.RequestBody"
                                    .equals(resolvedAnno.getId()))
                            .ifPresent(resolvedAnno -> entry.requestBodyResolveType(parameter.getType().resolve()));
                    parameter.getAnnotationByName("RequestParam").map(AnnotationExpr::resolve)
                            .filter(resolvedAnno -> "org.springframework.web.bind.annotation.RequestParam"
                                    .equals(resolvedAnno.getId()))
                            .ifPresent(resolvedAnno -> requestParams.add(parameter));
                    parameter.getAnnotationByName("PathVariable").map(AnnotationExpr::resolve)
                            .filter(resolvedAnno -> "org.springframework.web.bind.annotation.PathVariable"
                                    .equals(resolvedAnno.getId()))
                            .ifPresent(resolvedAnno -> pathVariables.add(parameter));
                }
                entry.requestParams(requestParams);
                entry.pathVariables(pathVariables);

                result.add(entry);
                log.debug("hanlder : {}",
                        shortestQualifiedSignature.substring(0, shortestQualifiedSignature.lastIndexOf('(')));
            });
        });

        return result;
    }

    private Map<String, Method> listDeclaredMethodAsMap(Class<?> reflectController) {
        Map<String, Method> declaredMethods = Maps.newHashMap();
        Arrays.stream(reflectController.getDeclaredMethods())
                .forEach(method -> declaredMethods.put(MethodQualifier.getShortestQualifiedSignature(method), method));
        return declaredMethods;
    }

    private boolean isController(ClassOrInterfaceDeclaration clazz) {
        return clazz.getAnnotations().stream().anyMatch(anno -> {
            try {
                ResolvedAnnotationDeclaration resolvedAnno = anno.resolve();
                return isKindOfController(resolvedAnno) || isKindOfRequestMapping(resolvedAnno);
            } catch (UnsolvedSymbolException e) {
                return false;
            }
        });
    }

    private boolean isHandler(MethodDeclaration method) {
        return method.getAnnotations().stream().anyMatch(anno -> {
            try {
                return isKindOfRequestMapping(anno.resolve());
            } catch (UnsolvedSymbolException e) {
                return false;
            }
        });
    }

    private boolean isKindOfController(ResolvedAnnotationDeclaration resolvedAnno) {
        String controllerQualifier = "org.springframework.stereotype.Controller";
        return controllerQualifier.equals(resolvedAnno.getId()) || resolvedAnno
                .hasDirectlyAnnotation(controllerQualifier);
    }

    private boolean isKindOfRequestMapping(ResolvedAnnotationDeclaration resolvedAnno) {
        String requestMappingQualifier = "org.springframework.web.bind.annotation.RequestMapping";
        return requestMappingQualifier.equals(resolvedAnno.getId()) || resolvedAnno
                .hasDirectlyAnnotation(requestMappingQualifier);
    }

    private String qualifierForClassLoader(ClassOrInterfaceDeclaration controller) {
        StringBuilder qualifierForClassLoader = new StringBuilder(64);
        this.qualifierForClassLoader(qualifierForClassLoader, controller);
        return qualifierForClassLoader.toString();
    }

    private void qualifierForClassLoader(StringBuilder qualifier, TypeDeclaration<?> node) {
        node.getParentNode().ifPresent(parent -> {
            if (parent instanceof TypeDeclaration) {
                this.qualifierForClassLoader(qualifier, (TypeDeclaration<?>) parent);
                qualifier.append("$");
                qualifier.append(node.getNameAsString());
            } else {
                node.getFullyQualifiedName().ifPresent(qualifier::append);
            }
        });
    }

}
