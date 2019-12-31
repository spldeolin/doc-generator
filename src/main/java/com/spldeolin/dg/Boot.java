package com.spldeolin.dg;

import java.util.Collection;
import com.google.common.collect.Lists;
import com.spldeolin.dg.convert.rap.RapConverter;
import com.spldeolin.dg.core.container.HandlerContainer;
import com.spldeolin.dg.core.domain.ApiDomain;
import com.spldeolin.dg.core.processor.ApiProcessor;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2019-12-23
 */
@Log4j2
public class Boot {

    public static void main(String[] args) {

        // collect
        HandlerContainer handlerContainer = HandlerContainer.getInstance(Conf.TARGET_PROJECT_PATH);

        // process
        Collection<ApiDomain> apis = Lists.newLinkedList();
        handlerContainer.getWithController()
                .forEach(entry -> apis.add(new ApiProcessor().process(entry.getController(), entry.getHandler())));

        // convert
        String result = new RapConverter().convert(apis);
        log.info(result);
    }

}
