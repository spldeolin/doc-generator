package com.spldeolin.dg.convert.rap;

import java.util.Collection;
import java.util.List;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.spldeolin.dg.core.domain.FieldDomain;
import com.spldeolin.dg.core.domain.ValidatorDomain;
import com.spldeolin.dg.core.enums.StringFormatType;
import lombok.Data;

/**
 * @author Deolin 2019-10-21
 */
@Data
public class ParameterListDto {

    private Long id;

    private String identifier;

    private String name;

    private String remark;

    private List<ParameterListDto> parameterList;

    /**
     * unknown
     */
    private String validator;

    /**
     * @see RapJsonType
     */
    private String dataType;

    public static ParameterListDto build(FieldDomain fieldDto) {
        ParameterListDto result = new ParameterListDto();
        result.setId(-2333L);
        result.setIdentifier(fieldDto.getFieldName());
        result.setName(fieldDto.getDescription());
        result.setValidator("");
        result.setDataType(RapJsonType.convert(fieldDto.getJsonType()).getName());
        result.setParameterList(Lists.newArrayList());

        StringBuilder remark = new StringBuilder(64);
        if (fieldDto.getStringFormat() != null && !StringFormatType.normal.getValue()
                .equals(fieldDto.getStringFormat())) {
            remark.append("格式：");
            remark.append(fieldDto.getStringFormat());
            remark.append("　");
        }
        if (fieldDto.getNumberFormat() != null) {
            remark.append("格式：");
            remark.append(fieldDto.getNumberFormat().getValue());
            remark.append("　");
        }

        if (Boolean.FALSE.equals(fieldDto.getNullable())) {
            remark.append("必填");
            remark.append("　");
        }

        Collection<ValidatorDomain> validators = fieldDto.getValidators();
        if (validators != null && validators.size() > 0) {
            Collection<String> parts = Lists.newLinkedList();
            validators.forEach(validator -> {
                parts.add(validator.getValidatorType().getDescription());
                parts.add(validator.getNote());
            });
            Joiner.on("　").skipNulls().appendTo(remark, parts);
        }

        result.setRemark(remark.toString());
        return result;
    }

}