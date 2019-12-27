package com.spldeolin.dg;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import com.google.common.collect.Lists;
import com.spldeolin.dg.convert.rap.RapConverter;
import com.spldeolin.dg.core.container.ContainerFactory;
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
        Path path = Conf.PROJECT_PATH;
        HandlerContainer handlerContainer = ContainerFactory.handlerContainer(path);

        // process
        Collection<ApiDomain> apis = Lists.newLinkedList();
        handlerContainer.getAll().forEach(handler -> apis.add(new ApiProcessor(path).process(handler)));

        // convert
        String result = new RapConverter().convert(apis);
        log.info(result);
    }

}
