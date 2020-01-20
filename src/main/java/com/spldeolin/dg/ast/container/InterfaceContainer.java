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
public class InterfaceContainer {

    @Getter
    private Collection<ClassOrInterfaceDeclaration> all = Lists.newLinkedList();

    private Map<String, ClassOrInterfaceDeclaration> byQualifier;

    @Getter
    private static InterfaceContainer instance = new InterfaceContainer();

    private InterfaceContainer() {
        CuContainer.getInstance().getAll().forEach(cu -> cu.findAll(ClassOrInterfaceDeclaration.class).stream()
                .filter(ClassOrInterfaceDeclaration::isInterface).forEach(iinterface -> all.add(iinterface)));
        log.info("(Summary) Collected {} interface COID.", all.size());
    }

    public Map<String, ClassOrInterfaceDeclaration> getByQualifier() {
        if (byQualifier == null) {
            byQualifier = Maps.newHashMapWithExpectedSize(all.size());
            all.forEach(iinterface -> byQualifier
                    .put(iinterface.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new), iinterface));
        }
        return byQualifier;
    }

}
