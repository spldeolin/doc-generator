package com.spldeolin.dg.convert.rap;

import java.util.Collection;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.spldeolin.dg.core.domain.FieldDto;
import com.spldeolin.dg.core.enums.StringFormat;
import com.spldeolin.dg.core.enums.ValidEnum;
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
     * @see RapDataTypeEnum
     */
    private String dataType;

    public static ParameterListDto build(FieldDto fieldDto) {
        ParameterListDto result = new ParameterListDto();
        result.setId(-2333L);
        result.setIdentifier(fieldDto.getFieldName());
        result.setName(fieldDto.getDescription());
        result.setValidator("");
        result.setDataType(RapDataTypeEnum.convert(fieldDto.getTypeName()).getName());
        result.setParameterList(Lists.newArrayList());

        StringBuilder remark = new StringBuilder(64);
        if (fieldDto.getStringFormat() != null && !StringFormat.normal.getValue().equals(fieldDto.getStringFormat())) {
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

        Collection<Pair<ValidEnum, String>> valids = fieldDto.getValids();
        if (valids != null && valids.size() > 0) {
            Collection<String> parts = Lists.newLinkedList();
            valids.forEach(pair -> {
                parts.add(pair.getLeft().getDescription());
                parts.add(pair.getRight());
            });
            Joiner.on("　").skipNulls().appendTo(remark, parts);
        }

        result.setRemark(remark.toString());
        return result;
    }

}