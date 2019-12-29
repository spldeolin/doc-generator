package com.spldeolin.dg;

import java.nio.file.Path;
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
        Path path = Conf.TARGET_PROJECT_PATH;
        HandlerContainer handlerContainer = HandlerContainer.getInstance(path);

        // process
        Collection<ApiDomain> apis = Lists.newLinkedList();
        handlerContainer.getWithController()
                .forEach(entry -> apis.add(new ApiProcessor(path).process(entry.getController(), entry.getHandler())));

        // convert
        String result = new RapConverter().convert(apis);
        log.info(result);
    }

}
