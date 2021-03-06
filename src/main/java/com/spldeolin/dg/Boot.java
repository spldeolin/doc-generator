package com.spldeolin.dg;

import java.util.Collection;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.common.collect.Lists;
import com.spldeolin.dg.core.domain.ApiDomain;
import com.spldeolin.dg.core.processor.ApiProcessor;
import com.spldeolin.dg.core.processor.HandlerProcessor;
import com.spldeolin.dg.core.processor.result.HandlerProcessResult;
import com.spldeolin.dg.core.strategy.DefaultHandlerFilter;
import com.spldeolin.dg.core.strategy.ReturnStmtBaseResponseBodyTypeParser;
import com.spldeolin.dg.view.rap.RapConverter;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2019-12-23
 */
@Log4j2
public class Boot {

    public static void main(String[] args) {

        // collect
        Collection<HandlerProcessResult> handlerEntries = new HandlerProcessor().process(new DefaultHandlerFilter() {
            @Override
            public boolean filter(ClassOrInterfaceDeclaration controller) {
                return true;
            }
        }, new ReturnStmtBaseResponseBodyTypeParser());

        // process
        Collection<ApiDomain> apis = Lists.newLinkedList();
        handlerEntries.forEach(entry -> apis.add(new ApiProcessor(entry).process()));

        // convert to view
        String result = new RapConverter().convert(apis);
        log.info(result);
    }

}
