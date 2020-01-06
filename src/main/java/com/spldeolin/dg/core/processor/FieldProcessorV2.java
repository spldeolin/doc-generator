package com.spldeolin.dg.core.processor;

import java.util.Collection;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ArraySchema;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ValueTypeSchema;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.google.common.collect.Lists;
import com.spldeolin.dg.Conf;
import com.spldeolin.dg.core.container.FieldContainer;
import com.spldeolin.dg.core.domain.ApiDomain;
import com.spldeolin.dg.core.domain.FieldDomain;
import com.spldeolin.dg.core.enums.JsonType;
import com.spldeolin.dg.core.enums.NumberFormatType;
import com.spldeolin.dg.core.enums.StringFormatType;
import com.spldeolin.dg.core.util.Javadocs;
import lombok.extern.log4j.Log4j2;

/**
 * TODO 处理类似 @NotNull List<@Max(11) Long> 的校验情况
 * TODO path参数
 * TODO query参数
 *
 * @author Deolin 2019-12-03
 */
@Log4j2
public class FieldProcessorV2 {

    public void processResponseBody(ObjectSchema objectSchema, ApiDomain api) {
        Pair<Collection<FieldDomain>, Collection<FieldDomain>> pair = parseZeroFloorFields(objectSchema);
        api.responseBodyFields(pair.getLeft());
        api.responseBodyFieldsFlatly(pair.getRight());
    }

    private Pair<Collection<FieldDomain>, Collection<FieldDomain>> parseZeroFloorFields(ObjectSchema zeroSchema) {
        List<FieldDomain> flatList = Lists.newArrayList();
        Collection<FieldDomain> zeroFloorFields = parseFieldTypes(zeroSchema, false, new FieldDomain(), flatList)
                .fields();
        zeroFloorFields.forEach(fieldDto -> fieldDto.parentField(null));

        return Pair.of(zeroFloorFields, flatList);
    }

    private FieldDomain parseFieldTypes(ObjectSchema schema, boolean isObjectInArray, FieldDomain parent,
            List<FieldDomain> flatList) {
        if (isObjectInArray) {
            parent.jsonType(JsonType.objectArray);
        } else {
            parent.jsonType(JsonType.object);
        }


        List<FieldDomain> children = Lists.newArrayList();
        schema.getProperties().forEach((childFieldName, childSchema) -> {
            FieldDomain childFieldDto = new FieldDomain();
            String fieldVarQualifier =
                    StringUtils.removeStart(schema.getId(), "urn:jsonschema:").replace(':', '.') + "." + childFieldName;
            FieldDeclaration fieldDeclaration = FieldContainer.getInstance(Conf.TARGET_PROJECT_PATH)
                    .getByFieldVarQualifier().get(fieldVarQualifier);
            if (fieldDeclaration == null) {
                /*
                被JsonSchema认为是个field，但不存在field时，会出现这种fieldDeclaration=null的情况，目前已知的有：
                    com.nebulapaas.base.po.PagePO.offset
                忽略它们即可
                 */
                return;
            }

            childFieldDto.fieldName(childFieldName);

            String comment = Javadocs.extractFirstLine(
                    FieldContainer.getInstance(Conf.TARGET_PROJECT_PATH).getByFieldVarQualifier()
                            .get(fieldVarQualifier));
            childFieldDto.description(comment);

            childFieldDto.nullable(true);
            if (fieldDeclaration.getAnnotationByName("NotNull").isPresent() || fieldDeclaration
                    .getAnnotationByName("NotEmpty").isPresent() || fieldDeclaration.getAnnotationByName("NotBlank")
                    .isPresent()) {
                childFieldDto.nullable(false);
            }

            childFieldDto.validators(new ValidatorProcessor().process(fieldDeclaration));

            if (childSchema.isValueTypeSchema()) {
                childFieldDto.jsonType(calcValueDataType(childSchema.asValueTypeSchema(), false));
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
                    childFieldDto.jsonType(calcValueDataType(eleSchema.asValueTypeSchema(), true));
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

            if (childFieldDto.jsonType() == JsonType.number) {
                String javaType = FieldContainer.getInstance(Conf.TARGET_PROJECT_PATH).getVarByFieldVarQualifier()
                        .get(fieldVarQualifier).getTypeAsString();
                childFieldDto.numberFormat(calcNumberFormat(javaType));
            }

            if (childFieldDto.jsonType() == JsonType.string) {
                childFieldDto.stringFormat(StringFormatType.normal.getValue());
                fieldDeclaration.getAnnotationByClass(JsonFormat.class)
                        .ifPresent(jsonFormat -> jsonFormat.asNormalAnnotationExpr().getPairs().forEach(pair -> {
                            if (pair.getNameAsString().equals("pattern")) {
                                childFieldDto.stringFormat(
                                        String.format(StringFormatType.datetime.getValue(), pair.getValue()));
                            }
                        }));
            }

            childFieldDto.parentField(parent);
            children.add(childFieldDto);
        });

        parent.fields(children);
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

}
