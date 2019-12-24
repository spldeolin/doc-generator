package com.spldeolin.dg.convert.rap;

import java.util.List;
import com.google.common.collect.Lists;
import com.spldeolin.dg.core.domain.ApiDomain;
import lombok.Data;

/**
 * @author Deolin 2019-10-21
 */
@Data
public class ActionListDto {

    private Long pageId;

    private Long id;

    private String requestType;

    private String responseTemplate;

    private String name;

    private String description;

    private String requestUrl;

    private List<ParameterListDto> requestParameterList;

    private List<ParameterListDto> responseParameterList;

    public static ActionListDto build(ApiDomain apiDto) {
        ActionListDto result = new ActionListDto();
        result.setPageId(389L);
        result.setId(-2333L);
        result.setRequestType("2");
        result.setResponseTemplate("");
        result.setName(apiDto.getDescription());
        result.setDescription("");
        result.setRequestUrl(apiDto.getUri());
        result.setRequestParameterList(Lists.newArrayList());
        result.setResponseParameterList(Lists.newArrayList());
        return result;
    }

}