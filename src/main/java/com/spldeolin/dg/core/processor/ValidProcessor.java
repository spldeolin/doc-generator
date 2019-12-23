package com.spldeolin.dg.core.processor;

import java.nio.file.Path;
import java.util.Collection;
import java.util.NoSuchElementException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.spldeolin.dg.core.container.ContainerFactory;
import com.spldeolin.dg.core.enums.ValidEnum;
import com.spldeolin.dg.core.util.Javadocs;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2019-12-09
 */
@Log4j2
public class ValidProcessor {

    private final Path path;

    public ValidProcessor(Path path) {
        this.path = path;
    }

    public Collection<Pair<ValidEnum, String>> process(FieldDeclaration fieldDeclaration) {
        Collection<Pair<ValidEnum, String>> valids = this.calcValids(fieldDeclaration.getAnnotations());

        if (valids.removeIf(pair -> pair.getLeft() == ValidEnum.enumValue)) {
            StringBuilder enumValueNote = this.calcEnumValueSpecially(fieldDeclaration);
            valids.add(Pair.of(ValidEnum.enumValue, enumValueNote.toString()));
        }

        return valids;
    }

    private StringBuilder calcEnumValueSpecially(FieldDeclaration fieldDeclaration) {
        StringBuilder result = new StringBuilder(64);
        fieldDeclaration.getAnnotationByName("ValidEnumValue").ifPresent(anno -> {
            result.append("（");
            anno.asNormalAnnotationExpr().getPairs().forEach(pair -> {
                if (nameOf(pair, "enumType")) {
                    String enumName = StringUtils.removeEnd(pair.getValue().toString(), ".class");
                    Collection<EnumDeclaration> enumDeclarations = ContainerFactory.enumContainer(path).getByEnumName()
                            .get(enumName);

                    Collection<String> parts = Lists.newLinkedList();
                    try {
                        Iterables.getOnlyElement(enumDeclarations).getEntries().forEach(entry -> {
                            if (entry.getArguments().size() > 0) {
                                // 约定第1个作为参数绑定的value
                                parts.add(entry.getArgument(0).toString());
                                parts.add(Javadocs.extractFirstLine(entry));
                            } else {
                                // 类似于 public enum Gender {male, female;}
                            }
                        });
                    } catch (NoSuchElementException e) {
                        log.warn("找不到enum[{}]", enumName);
                        return;
                    } catch (IllegalArgumentException e) {
                        log.warn("enum[{}]存在多个同名enum[{}]，无法识别", enumName, enumDeclarations);
                        return;
                    }
                    Joiner.on("、").appendTo(result, parts);
                }
            });
        });
        return result;
    }

    private Collection<Pair<ValidEnum, String>> calcValids(NodeList<AnnotationExpr> annos) {
        Collection<Pair<ValidEnum, String>> result = Lists.newLinkedList();
        annos.forEach(anno -> {
            switch (anno.getNameAsString()) {
                case "NotEmpty":
                    result.add(Pair.of(ValidEnum.notEmpty, ""));
                    break;
                case "NotBlank":
                    result.add(Pair.of(ValidEnum.notBlank, ""));
                    break;
                case "Size":
                case "Length":
                    anno.asNormalAnnotationExpr().getPairs().forEach(pair -> {
                        if (nameOf(pair, "min")) {
                            result.add(Pair.of(ValidEnum.maxSize, pair.getValue().toString()));
                        }
                        if (nameOf(pair, "max")) {
                            result.add(Pair.of(ValidEnum.minSize, pair.getValue().toString()));
                        }
                    });
                    break;
                case "Max":
                    anno.ifSingleMemberAnnotationExpr(singleAnno -> result
                            .add(Pair.of(ValidEnum.maxInteger, singleAnno.getMemberValue().toString())));
                    anno.ifNormalAnnotationExpr(
                            normalAnno -> normalAnno.getPairs().stream().filter(this::nameIsValue).findAny().ifPresent(
                                    pair -> result.add(Pair.of(ValidEnum.maxInteger, pair.getValue().toString()))));
                    break;
                case "Min":
                    anno.ifSingleMemberAnnotationExpr(singleAnno -> result
                            .add(Pair.of(ValidEnum.minInteger, singleAnno.getMemberValue().toString())));
                    anno.ifNormalAnnotationExpr(
                            normalAnno -> normalAnno.getPairs().stream().filter(this::nameIsValue).findAny().ifPresent(
                                    pair -> result.add(Pair.of(ValidEnum.minInteger, pair.getValue().toString()))));
                    break;
                case "DecimalMax":
                    anno.ifSingleMemberAnnotationExpr(singleAnno -> result
                            .add(Pair.of(ValidEnum.maxFloat, singleAnno.getMemberValue().toString())));
                    anno.ifNormalAnnotationExpr(
                            normalAnno -> normalAnno.getPairs().stream().filter(this::nameIsValue).findAny().ifPresent(
                                    pair -> result.add(Pair.of(ValidEnum.maxFloat, pair.getValue().toString()))));
                    break;
                case "DecimalMin":
                    anno.ifSingleMemberAnnotationExpr(singleAnno -> result
                            .add(Pair.of(ValidEnum.minFloat, singleAnno.getMemberValue().toString())));
                    anno.ifNormalAnnotationExpr(
                            normalAnno -> normalAnno.getPairs().stream().filter(this::nameIsValue).findAny().ifPresent(
                                    pair -> result.add(Pair.of(ValidEnum.minFloat, pair.getValue().toString()))));
                    break;
                case "Future":
                    result.add(Pair.of(ValidEnum.future, ""));
                    break;
                case "Past":
                    result.add(Pair.of(ValidEnum.past, ""));
                    break;
                case "Digits":
                    anno.asNormalAnnotationExpr().getPairs().forEach(pair -> {
                        if (nameOf(pair, "integer")) {
                            result.add(Pair.of(ValidEnum.maxIntegralDigits, pair.getValue().toString()));
                        }
                        if (nameOf(pair, "fraction")) {
                            result.add(Pair.of(ValidEnum.maxFractionalDigits, pair.getValue().toString()));
                        }
                    });
                    break;
                case "Positive":
                    result.add(Pair.of(ValidEnum.positive, ""));
                    break;
                case "Pattern":
                    anno.asNormalAnnotationExpr().getPairs().forEach(pair -> {
                        if (nameOf(pair, "regexp")) {
                            result.add(Pair.of(ValidEnum.regex, pair.getValue().asStringLiteralExpr().asString()));
                        }
                    });
                    break;
                case "ValidEnumValue":
                    result.add(Pair.of(ValidEnum.enumValue, ""));
                    break;
            }
        });

        return result;
    }

    private boolean nameIsValue(MemberValuePair pair) {
        return nameOf(pair, "value");
    }

    private boolean nameOf(MemberValuePair pair, String value) {
        return pair.getNameAsString().equals(value);
    }

}
