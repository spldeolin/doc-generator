package com.spldeolin.dg.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-01-03
 */
@Data
@Accessors(fluent = true)
public abstract class ResultEntry {

    /**
     * 1none, 2array, 3page
     */
    private Integer outermostWrapper;

}
