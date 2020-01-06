package com.spldeolin.dg.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ResponseBody的数据结构
 * 1、不返回
 * 2、值类型结构
 * 3、kv型结构
 * 4、复杂结构
 *
 * @author Deolin 2019-12-03
 */
@AllArgsConstructor
@Getter
public enum ResponseBodyStructure {

    /**
     * 没有返回值
     */
    v0id("void"),

    /**
     * 单纯的值类型结构
     *
     * 整个body是boolean、字符串、数字中的一个。e.g.: @ResponseBody public BigDecaimal ....
     */
    valueLike("valueLike"),

    /**
     * kv型数据结构
     *
     * e.g.: @ResponseBody public UserVo ....
     */
    keyValLike("keyValLike"),

    /**
     * 复杂的Body
     *
     * <pre>
     *  1. 可能是难以解析每个field之间的关系
     *  e.g.: @ResponseBody public Map<UserVo, AccountVo> ...
     *
     *  2. 可能是合法的，但在常规web开发中极少出现的返回类型，或是解析的性能成本较高
     *  e.g.: @ResponseBody public List<Long>[][][][][] ...
     *
     *  3. 可能是在非运行期无法确定类型的泛化返回类型
     *  e.g.: @ResponseBody public FoodVo<OrangeDetailVo> ....
     *  e.g.: @ResponseBody public JSONObject ....
     * </pre>
     *
     * 这类情况不会出现太多，解析成field的成本也比较高，解析出来后也无法以主流的表格形式来描述field之间的关系，所以只提供一个Json Schema作为特殊处理
     */
    chaos("chaos");

    private String value;

}
