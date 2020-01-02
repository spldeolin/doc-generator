package com.spldeolin.dg;

import java.util.Collection;
import com.google.common.collect.Lists;
import com.spldeolin.dg.convert.rap.RapConverter;
import com.spldeolin.dg.core.container.ClassContainer;
import com.spldeolin.dg.core.domain.ApiDomain;
import com.spldeolin.dg.core.domain.HandlerEntry;
import com.spldeolin.dg.core.processor.ApiProcessor;
import com.spldeolin.dg.core.processor.HandlerProcessor;
import com.spldeolin.dg.core.strategy.ReturnStmtParser;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2019-12-23
 */
@Log4j2
public class Boot {

    public static void main(String[] args) {

        // collect
        Collection<HandlerEntry> handlerEntries = new HandlerProcessor()
                .process(ClassContainer.getInstance(Conf.TARGET_PROJECT_PATH).getAll(), null, new ReturnStmtParser());

        // process
        Collection<ApiDomain> apis = Lists.newLinkedList();
        handlerEntries.forEach(entry -> apis.add(new ApiProcessor().process(entry)));

        // convert
        String result = new RapConverter().convert(apis);
        log.info(result);
    }

}
