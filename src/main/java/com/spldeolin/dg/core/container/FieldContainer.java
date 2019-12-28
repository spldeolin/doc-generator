package com.spldeolin.dg.core.container;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.spldeolin.dg.core.exception.ParentAbsentException;
import com.spldeolin.dg.core.exception.QualifierAbsentException;
import com.spldeolin.dg.core.util.Javadocs;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2019-12-06
 */
@Log4j2
@Data
public class FieldContainer {

    private static final int EXPECTED = 28000;

    private final Path path;

    private final Collection<FieldDeclaration> all = Lists.newLinkedList();

    private final Map<String, FieldDeclaration> byFieldVarQualifier = Maps.newHashMapWithExpectedSize(EXPECTED);

    // 存在这种情况 private int a, b = 1, c;
    private final Map<String, VariableDeclarator> varByFieldVarQualifier = Maps.newHashMapWithExpectedSize(EXPECTED);

    private final Multimap<String, FieldDeclaration> byClassQualifier = ArrayListMultimap.create(EXPECTED, 1);

    private final Map<String, String> cmtByFieldVarQualifier = Maps.newHashMapWithExpectedSize(EXPECTED);

    /* package-private */ FieldContainer(Path path) {
        CoidContainer coidContainer = ContainerFactory.coidContainer(path);
        long start = System.currentTimeMillis();
        this.path = path;
        coidContainer.getByCoidQualifier()
                .forEach((classQualifier, coid) -> coid.findAll(FieldDeclaration.class).forEach(field -> {
                    all.add(field);

                    field.getVariables().forEach(variable -> {
                        Node parent = field.getParentNode().orElseThrow(ParentAbsentException::new);
                        if (!(parent instanceof TypeDeclaration)) {
                            // 例如这个field在一个匿名内部类中，那么parent就是ObjectCreationExpr..
                            // 这些field往往不会是handler的参数或返回值
                            return;
                        }

                        String fieldVarQulifier = Joiner.on(".")
                                .join(((TypeDeclaration<?>) parent).getFullyQualifiedName()
                                        .orElseThrow(QualifierAbsentException::new), variable.getNameAsString());

                        byFieldVarQualifier.put(fieldVarQulifier, field);
                        varByFieldVarQualifier.put(fieldVarQulifier, variable);
                        cmtByFieldVarQualifier.put(fieldVarQulifier, Javadocs.extractFirstLine(field));
                    });

                    byClassQualifier.put(classQualifier, field);
                }));

        log.info("FieldContainer构建完毕，共从[{}]解析到[{}]个Field，耗时[{}]毫秒", path, all.size(),
                System.currentTimeMillis() - start);

        if (EXPECTED < all.size() + 100) {
            log.warn("FieldContainer.EXPECTED[{}]过小，可能会引发扩容降低性能，建议扩大这个值。（FieldContainer.all[{}]）", EXPECTED,
                    all.size());
        }
    }

}
