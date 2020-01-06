package com.spldeolin.dg.core.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 没有返回值
 *
 * @author Deolin 2020-01-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
public class VoidStructureResultEntry extends ResultEntry {
}
