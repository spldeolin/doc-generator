package com.spldeolin.dg.ast.container;

import java.util.Collection;
import java.util.Map;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.spldeolin.dg.ast.exception.ParentAbsentException;
import com.spldeolin.dg.ast.exception.QualifierAbsentException;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-01-20
 */
@Log4j2
public class FieldVariableContainer {

    @Getter
    private Collection<VariableDeclarator> all = Lists.newLinkedList();

    private Map<String, VariableDeclarator> byQualifier;

    @Getter
    private static final FieldVariableContainer instance = new FieldVariableContainer();

    private FieldVariableContainer() {
        CuContainer.getInstance().getAll()
                .forEach(cu -> cu.findAll(FieldDeclaration.class).forEach(field -> all.addAll(field.getVariables())));
        log.info("(Summary) Collected {} field VariableDeclarator.", all.size());
    }

    public Map<String, VariableDeclarator> getByQualifier() {
        if (byQualifier == null) {
            byQualifier = Maps.newHashMapWithExpectedSize(all.size());
            all.forEach(variable -> {
                Node field = variable.getParentNode().orElseThrow(ParentAbsentException::new);
                Node parent = field.getParentNode().orElseThrow(ParentAbsentException::new);
                if (!(parent instanceof TypeDeclaration)) {
                    // 例如这个field在一个匿名内部类中，那么parent就是ObjectCreationExpr..
                    // 由于是在匿名类中，所有没有全限定名
                    return;
                }

                String fieldVarQulifier = Joiner.on(".").join(((TypeDeclaration<?>) parent).getFullyQualifiedName()
                        .orElseThrow(QualifierAbsentException::new), variable.getNameAsString());
                byQualifier.put(fieldVarQulifier, variable);
            });
        }
        return byQualifier;
    }

}
