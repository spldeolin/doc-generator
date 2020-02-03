package com.spldeolin.dg.ast.container2;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.spldeolin.dg.ast.exception.QualifierAbsentException;
import lombok.extern.log4j.Log4j2;

/**
 * TypeDeclaration对象的收集器
 *
 * @param <T> TypeDeclaration的具体类型
 * @author Deolin 2020-02-03
 */
@Log4j2
class TypeDeclarationCollector<T extends TypeDeclaration<?>> {

    private final Class<T> type;

    TypeDeclarationCollector(Class<T> type) {
        this.type = type;
    }

    Collection<T> collectIntoCollection(Collection<CompilationUnit> cus) {
        Collection<T> result = Lists.newLinkedList();
        for (CompilationUnit cu : cus) {
            result.addAll(cu.findAll(type));
        }
        log.info("(Summary) {} {} has collected into Collection.", result.size(), type.getSimpleName());
        return result;
    }

    Map<String, T> collectIntoMapByCompilationUnits(Collection<CompilationUnit> cus) {
        Map<String, T> result = Maps.newHashMap();
        for (CompilationUnit cu : cus) {
            putAll(cu.findAll(type), result);
        }
        log.info("(Summary) {} {} has collected into Map.", result.size(), type.getSimpleName());
        return result;
    }

    Map<String, T> collectIntoMapByCollectedOnes(Collection<T> typeDeclarations) {
        Map<String, T> result = Maps.newHashMapWithExpectedSize(typeDeclarations.size());
        putAll(typeDeclarations, result);
        log.info("(Summary) {} {} has collected into Map.", result.size(), type.getSimpleName());
        return result;
    }

    private void putAll(Collection<T> coids, Map<String, T> map) {
        for (T coid : coids) {
            Optional<String> qualifier = coid.getFullyQualifiedName();
            if (qualifier.isPresent()) {
                map.put(qualifier.get(), coid);
            } else {
                throw new QualifierAbsentException();
            }
        }
    }

}
