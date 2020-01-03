package com.spldeolin.dg.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Deolin 2019-12-03
 */
@AllArgsConstructor
@Getter
public enum ResponseBodyMode {

    /**
     * 没有返回值
     */
    nothing("nothing"),

    /**
     * 单纯的值
     *
     * 整个body是boolean、字符串、数字中的一个。e.g.: @ResponseBody public BigDecaimal ....
     */
    val("value"),

    /**
     * 最外层是数组，次外层是单纯的值
     *
     * e.g.: @ResponseBody public List<String>
     */
    arrayValue("arrayValue"),

    /**
     * 最外层是kv对象
     *
     * e.g.: @ResponseBody public UserVo ....
     */
    object("object"),

    /**
     * 最外层是数组，次外层是kv对象
     *
     * e.g.: @ResponseBody public Set<UserVo> ...
     * e.g.: @ResponseBody public UserVo[] ...
     */
    arrayObject("arrayObject"),

    /**
     * 最外层是分页对象，分页的数据域是个kv对象
     *
     * e.g.: @ResponseBody public YourPageWrapper<UserVo> ...
     */
    pageObject("pageObject"),

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
    mazy("mazy");

    private String value;

}
