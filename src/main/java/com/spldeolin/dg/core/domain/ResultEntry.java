package com.spldeolin.dg.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * ResponseBody的数据结构
 * 1、不返回
 * 2、值类型结构
 * 3、kv型结构
 * 4、复杂结构
 *
 * @author Deolin 2020-01-03
 */
@Data
@Accessors(fluent = true)
public abstract class ResultEntry {

    private boolean inArray = false;

    private boolean inPage = false;

    public boolean isChaosStructureResultEntry() {
        return getClass() == ChaosStructureResultEntry.class;
    }

    public ChaosStructureResultEntry asChaosStructureResultEntry() {
        return (ChaosStructureResultEntry) this;
    }

    public boolean isKeyValStructureResultEntry() {
        return getClass() == KeyValStructureResultEntry.class;
    }

    public KeyValStructureResultEntry asKeyValStructureResultEntry() {
        return (KeyValStructureResultEntry) this;
    }

    public boolean isValueStructureResultEntry() {
        return getClass() == ValueStructureResultEntry.class;
    }

    public ValueStructureResultEntry asValueStructureResultEntry() {
        return (ValueStructureResultEntry) this;
    }

    public boolean isVoidStructureResultEntry() {
        return getClass() == VoidStructureResultEntry.class;
    }

    public VoidStructureResultEntry asVoidStructureResultEntry() {
        return (VoidStructureResultEntry) this;
    }

}
