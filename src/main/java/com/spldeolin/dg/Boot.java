package com.spldeolin.dg;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import com.google.common.collect.Lists;
import com.spldeolin.dg.core.container.ContainerFactory;
import com.spldeolin.dg.core.container.HandlerContainer;
import com.spldeolin.dg.core.domain.ApiDto;
import com.spldeolin.dg.core.processor.ApiProcessor;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2019-12-23
 */
@Log4j2
public class Boot {

    public static void main(String[] args) {

        // collect
        Path path = Paths.get("input target project path here.");
        HandlerContainer handlerContainer = ContainerFactory.handlerContainer(path);

        // process
        Collection<ApiDto> apis = Lists.newLinkedList();
        handlerContainer.getByController().asMap().forEach((controller, handlers) -> handlers
                .forEach(handler -> apis.add(new ApiProcessor(path).process(controller, handler))));

        log.info(apis);
    }

}
