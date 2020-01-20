package com.spldeolin.dg.ast.container;

import java.util.Collection;
import java.util.Map;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.spldeolin.dg.ast.exception.QualifierAbsentException;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2019-12-06
 */
@Log4j2
public class ClassContainer {

    @Getter
    private Collection<ClassOrInterfaceDeclaration> all = Lists.newLinkedList();

    private Map<String, ClassOrInterfaceDeclaration> byQualifier;

    @Getter
    private static final ClassContainer instance = new ClassContainer();

    private ClassContainer() {
        CuContainer.getInstance().getAll().forEach(
                cu -> cu.findAll(ClassOrInterfaceDeclaration.class).stream().filter(one -> !one.isInterface())
                        .forEach(classDeclaration -> all.add(classDeclaration)));
        log.info("(Summary) Collected {} class COID.", all.size());
    }

    public Map<String, ClassOrInterfaceDeclaration> getByQualifier() {
        if (byQualifier == null) {
            byQualifier = Maps.newHashMapWithExpectedSize(all.size());
            all.forEach(classDeclaration -> byQualifier
                    .put(classDeclaration.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new),
                            classDeclaration));
        }
        return byQualifier;
    }

}
