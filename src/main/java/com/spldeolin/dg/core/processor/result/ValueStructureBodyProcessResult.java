package com.spldeolin.dg.core.processor.result;

import com.spldeolin.dg.core.enums.FieldJsonType;
import com.spldeolin.dg.core.enums.NumberFormatType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 单纯的值类型结构
 *
 * 整个body是boolean、字符串、数字中的一个。e.g.: @ResponseBody public BigDecaimal ....
 *
 * @author Deolin 2020-01-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
public class ValueStructureBodyProcessResult extends BodyProcessResult {

    /**
     * struct=val时有效
     */
    private FieldJsonType valueStructureJsonType;

    /**
     * struct=val时有效
     */
    private NumberFormatType valueStructureNumberFormat;

}
