package com.spldeolin.dg.core.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-01-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
public class VoidStructureResultEntry extends ResultEntry {

    private Integer outermostWrapper = 1;

}
