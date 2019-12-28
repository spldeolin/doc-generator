package com.spldeolin.dg.core.processor;

import static com.spldeolin.dg.core.enums.ValidatorType.enumValue;
import static com.spldeolin.dg.core.enums.ValidatorType.future;
import static com.spldeolin.dg.core.enums.ValidatorType.maxFloat;
import static com.spldeolin.dg.core.enums.ValidatorType.maxFractionalDigits;
import static com.spldeolin.dg.core.enums.ValidatorType.maxInteger;
import static com.spldeolin.dg.core.enums.ValidatorType.maxIntegralDigits;
import static com.spldeolin.dg.core.enums.ValidatorType.maxSize;
import static com.spldeolin.dg.core.enums.ValidatorType.minFloat;
import static com.spldeolin.dg.core.enums.ValidatorType.minInteger;
import static com.spldeolin.dg.core.enums.ValidatorType.minSize;
import static com.spldeolin.dg.core.enums.ValidatorType.notBlank;
import static com.spldeolin.dg.core.enums.ValidatorType.notEmpty;
import static com.spldeolin.dg.core.enums.ValidatorType.past;
import static com.spldeolin.dg.core.enums.ValidatorType.positive;
import static com.spldeolin.dg.core.enums.ValidatorType.regex;

import java.nio.file.Path;
import java.util.Collection;
import java.util.NoSuchElementException;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.spldeolin.dg.core.container.EnumContainer;
import com.spldeolin.dg.core.domain.ValidatorDomain;
import com.spldeolin.dg.core.util.Javadocs;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2019-12-09
 */
@Log4j2
public class ValidatorProcessor {

    private final Path path;

    public ValidatorProcessor(Path path) {
        this.path = path;
    }

    public Collection<ValidatorDomain> process(FieldDeclaration fieldDeclaration) {
        Collection<ValidatorDomain> validators = this.calcValidators(fieldDeclaration.getAnnotations());

        if (validators.removeIf(validator -> enumValue == validator.getValidatorType())) {
            String enumValueNote = this.calcEnumValueSpecially(fieldDeclaration).toString();
            validators.add(new ValidatorDomain().setValidatorType(enumValue).setNote(enumValueNote));
        }

        return validators;
    }

    private StringBuilder calcEnumValueSpecially(FieldDeclaration fieldDeclaration) {
        StringBuilder result = new StringBuilder(64);
        fieldDeclaration.getAnnotationByName("ValidEnumValue").ifPresent(anno -> {
            result.append("（");
            anno.asNormalAnnotationExpr().getPairs().forEach(pair -> {
                if (nameOf(pair, "enumType")) {
                    String enumName = StringUtils.removeEnd(pair.getValue().toString(), ".class");
                    Collection<EnumDeclaration> enumDeclarations = EnumContainer.getInstance(path).getByEnumName()
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

    private Collection<ValidatorDomain> calcValidators(NodeList<AnnotationExpr> annos) {
        Collection<ValidatorDomain> result = Lists.newLinkedList();
        annos.forEach(anno -> {
            switch (anno.getNameAsString()) {
                case "NotEmpty":
                    result.add(new ValidatorDomain().setValidatorType(notEmpty));
                    break;
                case "NotBlank":
                    result.add(new ValidatorDomain().setValidatorType(notBlank));
                    break;
                case "Size":
                case "Length":
                    anno.asNormalAnnotationExpr().getPairs().forEach(pair -> {
                        ValidatorDomain validator = new ValidatorDomain().setNote(pair.getValue().toString());
                        if (nameOf(pair, "min")) {
                            result.add(validator.setValidatorType(minSize));
                        }
                        if (nameOf(pair, "max")) {
                            result.add(validator.setValidatorType(maxSize));
                        }
                    });
                    break;
                case "Max":
                    anno.ifSingleMemberAnnotationExpr(singleAnno -> result
                            .add(new ValidatorDomain().setValidatorType(maxInteger)
                                    .setNote(singleAnno.getMemberValue().toString())));
                    anno.ifNormalAnnotationExpr(
                            normalAnno -> normalAnno.getPairs().stream().filter(this::nameIsValue).findAny().ifPresent(
                                    pair -> result.add(new ValidatorDomain().setValidatorType(maxInteger)
                                            .setNote(pair.getValue().toString()))));
                    break;
                case "Min":
                    anno.ifSingleMemberAnnotationExpr(singleAnno -> result
                            .add(new ValidatorDomain().setValidatorType(minInteger)
                                    .setNote(singleAnno.getMemberValue().toString())));
                    anno.ifNormalAnnotationExpr(
                            normalAnno -> normalAnno.getPairs().stream().filter(this::nameIsValue).findAny().ifPresent(
                                    pair -> result.add(new ValidatorDomain().setValidatorType(minInteger)
                                            .setNote(pair.getValue().toString()))));
                    break;
                case "DecimalMax":
                    anno.ifSingleMemberAnnotationExpr(singleAnno -> result
                            .add(new ValidatorDomain().setValidatorType(maxFloat)
                                    .setNote(singleAnno.getMemberValue().toString())));
                    anno.ifNormalAnnotationExpr(
                            normalAnno -> normalAnno.getPairs().stream().filter(this::nameIsValue).findAny().ifPresent(
                                    pair -> result.add(new ValidatorDomain().setValidatorType(maxFloat)
                                            .setNote(pair.getValue().toString()))));
                    break;
                case "DecimalMin":
                    anno.ifSingleMemberAnnotationExpr(singleAnno -> result
                            .add(new ValidatorDomain().setValidatorType(minFloat)
                                    .setNote(singleAnno.getMemberValue().toString())));
                    anno.ifNormalAnnotationExpr(
                            normalAnno -> normalAnno.getPairs().stream().filter(this::nameIsValue).findAny().ifPresent(
                                    pair -> result.add(new ValidatorDomain().setValidatorType(minFloat)
                                            .setNote(pair.getValue().toString()))));
                    break;
                case "Future":
                    result.add(new ValidatorDomain().setValidatorType(future));
                    break;
                case "Past":
                    result.add(new ValidatorDomain().setValidatorType(past));
                    break;
                case "Digits":
                    anno.asNormalAnnotationExpr().getPairs().forEach(pair -> {
                        ValidatorDomain validator = new ValidatorDomain().setNote(pair.getValue().toString());
                        if (nameOf(pair, "integer")) {
                            result.add(validator.setValidatorType(maxIntegralDigits));
                        }
                        if (nameOf(pair, "fraction")) {
                            result.add(validator.setValidatorType(maxFractionalDigits));
                        }
                    });
                    break;
                case "Positive":
                    result.add(new ValidatorDomain().setValidatorType(positive));
                    break;
                case "Pattern":
                    anno.asNormalAnnotationExpr().getPairs().forEach(pair -> {
                        if (nameOf(pair, "regexp")) {
                            result.add(new ValidatorDomain().setValidatorType(regex)
                                    .setNote(pair.getValue().asStringLiteralExpr().asString()));
                        }
                    });
                    break;
                case "ValidEnumValue":
                    result.add(new ValidatorDomain().setValidatorType(enumValue));
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
