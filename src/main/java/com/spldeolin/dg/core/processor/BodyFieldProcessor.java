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
import com.spldeolin.dg.ast.container.FieldContainer;
import com.spldeolin.dg.ast.container.FieldVariableContainer;
import com.spldeolin.dg.core.domain.ApiDomain;
import com.spldeolin.dg.core.domain.BodyFieldDomain;
import com.spldeolin.dg.core.enums.FieldJsonType;
import com.spldeolin.dg.core.enums.NumberFormatType;
import com.spldeolin.dg.core.enums.StringFormatType;
import com.spldeolin.dg.core.util.Javadocs;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2019-12-03
 */
@Log4j2
public class BodyFieldProcessor {

    public void process(ObjectSchema objectSchema, ApiDomain api) {
        Pair<Collection<BodyFieldDomain>, Collection<BodyFieldDomain>> pair = parseZeroFloorFields(objectSchema);
        api.responseBodyFields(pair.getLeft());
        api.responseBodyFieldsFlatly(pair.getRight());
    }

    private Pair<Collection<BodyFieldDomain>, Collection<BodyFieldDomain>> parseZeroFloorFields(
            ObjectSchema zeroSchema) {
        List<BodyFieldDomain> flatList = Lists.newArrayList();
        Collection<BodyFieldDomain> zeroFloorFields = parseFieldTypes(zeroSchema, false, new BodyFieldDomain(),
                flatList).fields();
        zeroFloorFields.forEach(fieldDto -> fieldDto.parentField(null));

        return Pair.of(zeroFloorFields, flatList);
    }

    private BodyFieldDomain parseFieldTypes(ObjectSchema schema, boolean isObjectInArray, BodyFieldDomain parent,
            List<BodyFieldDomain> flatList) {
        if (isObjectInArray) {
            parent.jsonType(FieldJsonType.objectArray);
        } else {
            parent.jsonType(FieldJsonType.object);
        }

        List<BodyFieldDomain> children = Lists.newArrayList();
        schema.getProperties().forEach((childFieldName, childSchema) -> {
            BodyFieldDomain childFieldDto = new BodyFieldDomain();
            String fieldVarQualifier =
                    StringUtils.removeStart(schema.getId(), "urn:jsonschema:").replace(':', '.') + "." + childFieldName;
            FieldDeclaration fieldDeclaration = FieldContainer.getInstance(Conf.PROJECT_PATH)
                    .getByVariableQualifier().get(fieldVarQualifier);
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
                    FieldContainer.getInstance(Conf.PROJECT_PATH).getByVariableQualifier().get(fieldVarQualifier));
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

            if (childFieldDto.jsonType() == FieldJsonType.number) {
                String javaType = FieldVariableContainer.getInstance(Conf.PROJECT_PATH).getByQualifier()
                        .get(fieldVarQualifier).getTypeAsString();
                childFieldDto.numberFormat(calcNumberFormat(javaType));
            }

            if (childFieldDto.jsonType() == FieldJsonType.string) {
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


    private FieldJsonType calcValueDataType(ValueTypeSchema vSchema, boolean isValueInArray) {
        if (vSchema.isNumberSchema()) {
            if (isValueInArray) {
                return FieldJsonType.numberArray;
            } else {
                return FieldJsonType.number;
            }
        } else if (vSchema.isStringSchema()) {
            if (isValueInArray) {
                return FieldJsonType.stringArray;
            } else {
                return FieldJsonType.string;
            }
        } else if (vSchema.isBooleanSchema()) {
            if (isValueInArray) {
                return FieldJsonType.booleanArray;
            } else {
                return FieldJsonType.bool;
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
