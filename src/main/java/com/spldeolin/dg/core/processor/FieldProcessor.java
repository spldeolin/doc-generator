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
import com.spldeolin.dg.Conf;
import com.spldeolin.dg.core.container.ContainerFactory;
import com.spldeolin.dg.core.domain.ApiDomain;
import com.spldeolin.dg.core.domain.FieldDomain;
import com.spldeolin.dg.core.enums.JsonType;
import com.spldeolin.dg.core.enums.NumberFormatType;
import com.spldeolin.dg.core.enums.RequestBodyType;
import com.spldeolin.dg.core.enums.ResponseBodyType;
import com.spldeolin.dg.core.enums.StringFormatType;
import com.spldeolin.dg.core.lcbi.LoadClassBasedInfoImporter;
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

    public Collection<FieldDomain> processUriPathFields(NodeList<Parameter> parameters) {
        // ignore now
        return Lists.newArrayList();
    }

    public Collection<FieldDomain> processUriQueryFields(NodeList<Parameter> parameters) {
        // ignore now
        return Lists.newArrayList();
    }

    public void processRequestBody(NodeList<Parameter> parameters, ApiDomain api) {
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
            Pair<Collection<FieldDomain>, Collection<FieldDomain>> pair = parseZeroFloorFields(parameterTypeQulifier,
                    false);
            api.setRequestBodyFileds(pair.getLeft());
            api.setRequestBodyFiledsFlatly(pair.getRight());
        });
    }

    public void processResponseBody(String resultTypeName, ApiDomain api) {
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
            Pair<Collection<FieldDomain>, Collection<FieldDomain>> pair = parseZeroFloorFields(resultTypeQulifier,
                    true);
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

    private Pair<Collection<FieldDomain>, Collection<FieldDomain>> parseZeroFloorFields(String classQulifier,
            boolean isResponseBody) {
        List<FieldDomain> flatList = Lists.newArrayList();
        JsonSchema jsonSchema = LoadClassBasedInfoImporter.getJsonSchema(classQulifier);
        if (jsonSchema == null) {
            log.warn("文件[{}]中找不到[{}]", Conf.POJO_SCHEMA_PATH, classQulifier);
            return Pair.of(Lists.newArrayList(), Lists.newArrayList());
        }
        ObjectSchema zeroSchema = jsonSchema.asObjectSchema();
        Collection<FieldDomain> zeroFloorFields = parseFieldTypes(zeroSchema, false, new FieldDomain(), flatList)
                .getFields();
        zeroFloorFields.forEach(fieldDto -> fieldDto.setParentField(null));

        if (isResponseBody) {
            flatList.forEach(fieldDto -> {
                fieldDto.setNullable(null);
                fieldDto.setValidators(null);
            });
        }

        return Pair.of(zeroFloorFields, flatList);
    }

    private FieldDomain parseFieldTypes(ObjectSchema schema, boolean isObjectInArray, FieldDomain parent,
            List<FieldDomain> flatList) {
        if (isObjectInArray) {
            parent.setJsonType(JsonType.objectArray);
        } else {
            parent.setJsonType(JsonType.object);
        }


        List<FieldDomain> children = Lists.newArrayList();
        schema.getProperties().forEach((childFieldName, childSchema) -> {
            FieldDomain childFieldDto = new FieldDomain();
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

            String comment = ContainerFactory.fieldContainer(path).getCmtByFieldVarQualifier().get(fieldVarQualifier);
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

            childFieldDto.setValidators(new ValidatorProcessor(path).process(fieldDeclaration));

            if (childSchema.isValueTypeSchema()) {
                childFieldDto.setJsonType(calcValueDataType(childSchema.asValueTypeSchema(), false));
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
                    childFieldDto.setJsonType(calcValueDataType(eleSchema.asValueTypeSchema(), true));
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

            if (childFieldDto.getJsonType() == JsonType.number) {
                String javaType = ContainerFactory.fieldContainer(path).getVarByFieldVarQualifier()
                        .get(fieldVarQualifier).getTypeAsString();
                childFieldDto.setNumberFormat(calcNumberFormat(javaType));
            }

            if (childFieldDto.getJsonType() == JsonType.string) {
                childFieldDto.setStringFormat(StringFormatType.normal.getValue());
                fieldDeclaration.getAnnotationByClass(JsonFormat.class)
                        .ifPresent(jsonFormat -> jsonFormat.asNormalAnnotationExpr().getPairs().forEach(pair -> {
                            if (pair.getNameAsString().equals("pattern")) {
                                childFieldDto.setStringFormat(
                                        String.format(StringFormatType.datetime.getValue(), pair.getValue()));
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


    private JsonType calcValueDataType(ValueTypeSchema vSchema, boolean isValueInArray) {
        if (vSchema.isNumberSchema()) {
            if (isValueInArray) {
                return JsonType.numberArray;
            } else {
                return JsonType.number;
            }
        } else if (vSchema.isStringSchema()) {
            if (isValueInArray) {
                return JsonType.stringArray;
            } else {
                return JsonType.string;
            }
        } else if (vSchema.isBooleanSchema()) {
            if (isValueInArray) {
                return JsonType.booleanArray;
            } else {
                return JsonType.bool;
            }
        } else {
            throw new IllegalArgumentException(vSchema.toString());
        }
    }

    private NumberFormatType calcNumberFormat(String javaTypeName) {
        if (StringUtils.equalsAnyIgnoreCase(javaTypeName, "Float", "Double", "BigDecimal")) {
            return NumberFormatType.f1oat;
        } else if (StringUtils.equalsAnyIgnoreCase(javaTypeName, "Long", "BigInteger")) {
            return NumberFormatType.int64;
        } else {
            return NumberFormatType.int32;
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
