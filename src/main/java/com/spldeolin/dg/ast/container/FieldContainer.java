package com.spldeolin.dg.ast.container;

import java.util.Collection;
import java.util.Map;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.spldeolin.dg.ast.exception.ParentAbsentException;
import com.spldeolin.dg.ast.exception.QualifierAbsentException;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2019-12-06
 */
@Log4j2
public class FieldContainer {

    @Getter
    private Collection<FieldDeclaration> all = Lists.newLinkedList();

    private Map<String, FieldDeclaration> byVariableQualifier;

    @Getter
    private static final FieldContainer instance = new FieldContainer();

    private FieldContainer() {
        ClassContainer.getInstance().getByQualifier()
                .forEach((classQualifier, coid) -> all.addAll(coid.findAll(FieldDeclaration.class)));
        log.info("(Summary) Collected {} FieldDeclaration.", all.size());
    }

    public Map<String, FieldDeclaration> getByVariableQualifier() {
        if (byVariableQualifier == null) {
            byVariableQualifier = Maps.newHashMapWithExpectedSize(all.size());
            all.forEach(field -> {
                field.getVariables().forEach(variable -> {
                    Node parent = field.getParentNode().orElseThrow(ParentAbsentException::new);
                    if (!(parent instanceof TypeDeclaration)) {
                        // 例如这个field在一个匿名内部类中，那么parent就是ObjectCreationExpr..
                        // 由于是在匿名类中，所有没有全限定名
                        return;
                    }

                    String fieldVarQulifier = Joiner.on(".").join(((TypeDeclaration<?>) parent).getFullyQualifiedName()
                            .orElseThrow(QualifierAbsentException::new), variable.getNameAsString());

                    byVariableQualifier.put(fieldVarQulifier, field);
                });
            });
        }
        return byVariableQualifier;
    }

}
