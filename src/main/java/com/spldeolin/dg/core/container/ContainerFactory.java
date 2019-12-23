package com.spldeolin.dg.core.container;

import java.nio.file.Path;
import java.util.Map;
import com.google.common.collect.Maps;

/**
 * @author Deolin 2019-12-09
 */
public class ContainerFactory {

    private static final Map<Path, CuContainer> compilationUnitContainers = Maps.newConcurrentMap();

    private static final Map<Path, CoidContainer> coidContainers = Maps.newConcurrentMap();

    private static final Map<Path, EnumContainer> enumContainers = Maps.newConcurrentMap();

    private static final Map<Path, HandlerContainer> handlerContainers = Maps.newHashMap();

    private static final Map<Path, FieldContainer> fieldContainers = Maps.newConcurrentMap();

    public static CuContainer cuContainer(Path path) {
        CuContainer result = compilationUnitContainers.get(path);
        if (result == null) {
            result = new CuContainer(path);
            compilationUnitContainers.put(path, result);
        }
        return result;
    }

    public static CoidContainer coidContainer(Path path) {
        CoidContainer result = coidContainers.get(path);
        if (result == null) {
            result = new CoidContainer(path);
            coidContainers.put(path, result);
        }
        return result;
    }

    public static EnumContainer enumContainer(Path path) {
        EnumContainer result = enumContainers.get(path);
        if (result == null) {
            result = new EnumContainer(path);
            enumContainers.put(path, result);
        }
        return result;
    }

    public static HandlerContainer handlerContainer(Path path) {
        HandlerContainer result = handlerContainers.get(path);
        if (result == null) {
            result = new HandlerContainer(path);
            handlerContainers.put(path, result);
        }
        return result;
    }

    public static FieldContainer fieldContainer(Path path) {
        FieldContainer result = fieldContainers.get(path);
        if (result == null) {
            result = new FieldContainer(path);
            fieldContainers.put(path, result);
        }
        return result;
    }

}
