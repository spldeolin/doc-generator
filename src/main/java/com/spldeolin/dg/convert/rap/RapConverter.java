package com.spldeolin.dg.convert.rap;

import java.util.Collection;
import java.util.List;
import com.google.common.collect.Lists;
import com.spldeolin.dg.core.domain.ApiDto;
import com.spldeolin.dg.core.domain.FieldDto;
import com.spldeolin.dg.core.util.Jsons;

/**
 * @author Deolin 2019-12-09
 */
public class RapConverter {

    public String convert(Collection<ApiDto> apis) {
        String json = Jsons.toJson(convertApis(apis));

        StringBuilder sb = new StringBuilder(json.length());
        int id = -1;
        String[] parts = json.split("-2333");
        for (int i = 0; i < parts.length; i++) {
            sb.append(parts[i]);
            if (i < parts.length - 1) {
                sb.append(id--);
            }
        }
        return sb.toString();
    }

    private Collection<ActionListDto> convertApis(Collection<ApiDto> apis) {
        Collection<ActionListDto> actions = Lists.newLinkedList();
        apis.forEach(api -> {
            ActionListDto action = ActionListDto.build(api);
            action.setRequestParameterList(convertFields(api.getRequestBodyFileds()));
            action.setResponseParameterList(convertFields(api.getResponseBodyFields()));
            actions.add(action);
        });

        return actions;
    }

    private List<ParameterListDto> convertFields(Collection<FieldDto> fields) {
        List<ParameterListDto> firstFloor = Lists.newArrayList();
        if (fields == null) {
            return Lists.newArrayList();
        }
        for (FieldDto field : fields) {
            ParameterListDto child = ParameterListDto.build(field);
            if (field.getFields() != null && field.getFields().size() > 0) {
                this.convertFields(field.getFields(), child);
            }
            firstFloor.add(child);
        }
        return firstFloor;
    }

    private void convertFields(Collection<FieldDto> fields, ParameterListDto parent) {
        List<ParameterListDto> childrent = Lists.newArrayList();
        for (FieldDto field : fields) {
            ParameterListDto child = ParameterListDto.build(field);
            if (field.getFields() != null && field.getFields().size() > 0) {
                this.convertFields(field.getFields(), child);
            }
            childrent.add(child);
        }
        parent.setParameterList(childrent);
    }

}
