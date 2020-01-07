package com.spldeolin.dg.core.processor.result;

import java.util.Set;
import com.spldeolin.dg.core.enums.MethodType;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-01-06
 */
@Data
@Accessors(fluent = true)
public class RequestMappingProcessResult {

    private Set<MethodType> methodTypes;

    private Set<String> uris;

}
