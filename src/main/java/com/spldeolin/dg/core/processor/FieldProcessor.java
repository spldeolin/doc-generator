package com.spldeolin.dg.core.processor;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ArraySchema;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ValueTypeSchema;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.types.ResolvedArrayType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.spldeolin.dg.core.container.ContainerFactory;
import com.spldeolin.dg.core.domain.ApiDto;
import com.spldeolin.dg.core.domain.FieldDto;
import com.spldeolin.dg.core.enums.NumberFormat;
import com.spldeolin.dg.core.enums.RequestBodyType;
import com.spldeolin.dg.core.enums.ResponseBodyType;
import com.spldeolin.dg.core.enums.StringFormat;
import com.spldeolin.dg.core.enums.TypeName;
import com.spldeolin.dg.core.jsonschema.PojoJsonSchemaImporter;
import com.spldeolin.dg.core.util.Strings;
import lombok.extern.log4j.Log4j2;

/**
 * TODO 处理类似 @NotNull List<@Max(11) Long> 的校验情况
 * TODO path参数
 * TODO query参数
 * TODO requestBody或responseBody是value而不是object的情况
 *
 * @author Deolin 2019-12-03
 */
@Log4j2
public class FieldProcessor {

    private final Path path;

    public FieldProcessor(Path path) {
        this.path = path;
    }

    public Collection<FieldDto> processUriPathFields(NodeList<Parameter> parameters) {
        // ignore now
        return Lists.newArrayList();
    }

    public Collection<FieldDto> processUriQueryFields(NodeList<Parameter> parameters) {
        // ignore now
        return Lists.newArrayList();
    }

    public void processRequestBody(NodeList<Parameter> parameters, ApiDto api) {
        String parameterTypeName = getRequestBodyTypeName(parameters);
        if (parameterTypeName == null) {
            return;
        }
        // requestBodyType
        if (Strings.isSurroundedBy(parameterTypeName, "List<", ">")) {
            api.setRequestBodyType(RequestBodyType.objectArray);
            parameterTypeName = Strings.removeSurround(parameterTypeName, "List<", ">");
        } else {
            api.setRequestBodyType(RequestBodyType.object);
        }

        // requestBodyFields
        tryGetClassQulifier(parameterTypeName).ifPresent(parameterTypeQulifier -> {
            Pair<Collection<FieldDto>, Collection<FieldDto>> pair = parseZeroFloorFields(parameterTypeQulifier, false);
            api.setRequestBodyFileds(pair.getLeft());
            api.setRequestBodyFiledsFlatly(pair.getRight());
        });
    }

    public void processResponseBody(String resultTypeName, ApiDto api) {
        // responseBodyType
        if (Strings.isSurroundedBy(resultTypeName, "List<", ">")) {
            api.setResponseBodyType(ResponseBodyType.objectArray);
            resultTypeName = Strings.removeSurround(resultTypeName, "List<", ">");
        } else if (Strings.isSurroundedBy(resultTypeName, "PageInfo<", ">")) {
            api.setResponseBodyType(ResponseBodyType.objectPage);
            resultTypeName = Strings.removeSurround(resultTypeName, "PageInfo<", ">");
        } else {
            api.setResponseBodyType(ResponseBodyType.object);
        }
        // responseBodyFields
        tryGetClassQulifier(resultTypeName).ifPresent(resultTypeQulifier -> {
            Pair<Collection<FieldDto>, Collection<FieldDto>> pair = parseZeroFloorFields(resultTypeQulifier, true);
            api.setResponseBodyFields(pair.getLeft());
            api.setResponseBodyFieldsFlatly(pair.getRight());
        });

    }

    private String getRequestBodyTypeName(NodeList<Parameter> parameters) {
        for (Parameter parameter : parameters) {
            if (parameter.getAnnotationByName("RequestBody").isPresent()) {
                try {
                    ResolvedType type = parameter.getType().resolve();
                    if (type.isArray()) {
                        type = recurrenceElementType(type.asArrayType());
                    }
                    if (!type.isReferenceType()) {
                        throw new RuntimeException("unknow ResolvedType " + type.getClass().getSimpleName());
                    }
                } catch (UnsolvedSymbolException e) {
                    log.warn(e.getName());
                }
                return parameter.getTypeAsString();
            }
        }
        return null;
    }

    private ResolvedType recurrenceElementType(ResolvedArrayType arrayType) {
        ResolvedType componentType = arrayType.getComponentType();
        if (componentType.isArray()) {
            recurrenceElementType(componentType.asArrayType());
        }
        return componentType;
    }

    private Pair<Collection<FieldDto>, Collection<FieldDto>> parseZeroFloorFields(String classQulifier,
            boolean isResponseBody) {
        List<FieldDto> flatList = Lists.newArrayList();
        ObjectSchema zeroSchema = PojoJsonSchemaImporter.getJsonSchema(classQulifier).asObjectSchema();
        Collection<FieldDto> zeroFloorFields = parseFieldTypes(zeroSchema, false, new FieldDto(), flatList).getFields();
        zeroFloorFields.forEach(fieldDto -> fieldDto.setParentField(null));

        if (isResponseBody) {
            flatList.forEach(fieldDto -> {
                fieldDto.setNullable(null);
                fieldDto.setValids(null);
            });
        }

        return Pair.of(zeroFloorFields, flatList);
    }

    private FieldDto parseFieldTypes(ObjectSchema schema, boolean isObjectInArray, FieldDto parent,
            List<FieldDto> flatList) {
        if (isObjectInArray) {
            parent.setTypeName(TypeName.objectArray);
        } else {
            parent.setTypeName(TypeName.object);
        }


        List<FieldDto> children = Lists.newArrayList();
        schema.getProperties().forEach((childFieldName, childSchema) -> {
            FieldDto childFieldDto = new FieldDto();
            String fieldVarQualifier =
                    StringUtils.removeStart(schema.getId(), "urn:jsonschema:").replace(':', '.') + "." + childFieldName;
            FieldDeclaration fieldDeclaration = ContainerFactory.fieldContainer(path).getByFieldVarQualifier()
                    .get(fieldVarQualifier);
            if (fieldDeclaration == null) {
                /*
                被JsonSchema认为是个field，但不存在field时，会出现这种fieldDeclaration=null的情况，目前已知的有：
                    com.nebulapaas.base.po.PagePO.offset
                忽略它们即可
                 */
                return;
            }

            childFieldDto.setFieldName(childFieldName);

            String comment = ContainerFactory.fieldContainer(path).getCmtByFieldVarQualifier()
                    .get(fieldVarQualifier);
            if (StringUtils.isBlank(comment)) {
                log.warn("comment absent [{}]", fieldVarQualifier);
            }
            childFieldDto.setDescription(comment);

            childFieldDto.setNullable(true);
            if (fieldDeclaration.getAnnotationByName("NotNull").isPresent() || fieldDeclaration
                    .getAnnotationByName("NotEmpty").isPresent() || fieldDeclaration.getAnnotationByName("NotBlank")
                    .isPresent()) {
                childFieldDto.setNullable(false);
            }

            childFieldDto.setValids(new ValidProcessor(path).process(fieldDeclaration));

            if (childSchema.isValueTypeSchema()) {
                childFieldDto.setTypeName(calcValueDataType(childSchema.asValueTypeSchema(), false));
            } else if (childSchema.isObjectSchema()) {
                parseFieldTypes(childSchema.asObjectSchema(), false, childFieldDto, flatList);
            } else if (childSchema.isArraySchema()) {
                ArraySchema aSchema = childSchema.asArraySchema();
                if (aSchema.getItems() == null) {
                    log.warn("无法解析JSONArray，忽略");
                    return;
                }
                if (aSchema.getItems().isArrayItems()) {
                    log.warn("rap不支持类似于 List<List<String>> 数组直接嵌套数组的参数，忽略");
                    return;
                }

                JsonSchema eleSchema = aSchema.getItems().asSingleItems().getSchema();
                if (eleSchema.isValueTypeSchema()) {
                    childFieldDto.setTypeName(calcValueDataType(eleSchema.asValueTypeSchema(), true));
                } else if (eleSchema.isObjectSchema()) {
                    parseFieldTypes(eleSchema.asObjectSchema(), true, childFieldDto, flatList);
                } else {
                    // 可能是因为 1. 类中存在不支持类型的field 2. 这是个通过Jackson映射到CSV的DTO 3. 类中存在多个相同类型的ObjectSchema
                    return;
                }
            } else {
                // 可能是因为 1. 类中存在不支持类型的field 2. 这是个通过Jackson映射到CSV的DTO 3. 类中存在多个相同类型的ObjectSchema
                return;
            }

            if (childFieldDto.getTypeName() == TypeName.number) {
                String javaType = ContainerFactory.fieldContainer(path).getVarByFieldVarQualifier()
                        .get(fieldVarQualifier).getTypeAsString();
                childFieldDto.setNumberFormat(calcNumberFormat(javaType));
            }

            if (childFieldDto.getTypeName() == TypeName.string) {
                childFieldDto.setStringFormat(StringFormat.normal.getValue());
                fieldDeclaration.getAnnotationByClass(JsonFormat.class)
                        .ifPresent(jsonFormat -> jsonFormat.asNormalAnnotationExpr().getPairs().forEach(pair -> {
                            if (pair.getNameAsString().equals("pattern")) {
                                childFieldDto.setStringFormat(
                                        String.format(StringFormat.datetime.getValue(), pair.getValue()));
                            }
                        }));
            }

            childFieldDto.setParentField(parent);
            children.add(childFieldDto);
        });

        parent.setFields(children);
        flatList.addAll(children);
        return parent;
    }


    private TypeName calcValueDataType(ValueTypeSchema vSchema, boolean isValueInArray) {
        if (vSchema.isNumberSchema()) {
            if (isValueInArray) {
                return TypeName.numberArray;
            } else {
                return TypeName.number;
            }
        } else if (vSchema.isStringSchema()) {
            if (isValueInArray) {
                return TypeName.stringArray;
            } else {
                return TypeName.string;
            }
        } else if (vSchema.isBooleanSchema()) {
            if (isValueInArray) {
                return TypeName.booleanArray;
            } else {
                return TypeName.bool;
            }
        } else {
            throw new IllegalArgumentException(vSchema.toString());
        }
    }

    private NumberFormat calcNumberFormat(String javaTypeName) {
        if (StringUtils.equalsAnyIgnoreCase(javaTypeName, "Float", "Double", "BigDecimal")) {
            return NumberFormat.f1oat;
        } else if (StringUtils.equalsAnyIgnoreCase(javaTypeName, "Long", "BigInteger")) {
            return NumberFormat.int64;
        } else {
            return NumberFormat.int32;
        }
    }

    private Optional<String> tryGetClassQulifier(String className) {
        Collection<String> classQulifiers = ContainerFactory.coidContainer(path).getCoidQulifierByCoidName()
                .get(className);
        if (classQulifiers.size() == 0) {
            if (StringUtils.isNotEmpty(className)) {
                log.info("找不到Class[{}]", className);
            }
            return Optional.empty();
        }
        if (classQulifiers.size() > 1) {
            log.warn("Class[{}]存在多个同名Class[{}]，无法进行加载", className, classQulifiers);
            return Optional.empty();
        }
        return Optional.of(Iterables.getOnlyElement(classQulifiers));
    }

}
