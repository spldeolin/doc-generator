package com.spldeolin.dg.ast.container;

import java.util.Collection;
import java.util.Map;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.spldeolin.dg.ast.exception.QualifierAbsentException;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2019-12-06
 */
@Log4j2
public class EnumContainer {

    @Getter
    private Collection<EnumDeclaration> all = Lists.newLinkedList();

    private Map<String, EnumDeclaration> byQualifier;

    @Getter
    private static final EnumContainer instance = new EnumContainer();

    private EnumContainer() {
        CuContainer.getInstance().getAll().forEach(cu -> all.addAll(cu.findAll(EnumDeclaration.class)));
        log.info("(Summary) Collected {} EnumDeclaration.", all.size());
    }

    public Map<String, EnumDeclaration> getByQualifier() {
        if (byQualifier == null) {
            byQualifier = Maps.newHashMapWithExpectedSize(all.size());
            all.forEach(enumDeclaration -> byQualifier
                    .put(enumDeclaration.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new),
                            enumDeclaration));
        }
        return byQualifier;
    }

}
