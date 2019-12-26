package com.spldeolin.dg.core.lcbi;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * @author Deolin 2019-12-26
 */
public class UriProcessor {

    /**
     * AbstractHandlerMethodMapping#detectHandlerMethods(java.lang.Object)
     */
    public Collection<HandlerMappingDto> process(Class<?> clazz) {
        Map<Method, RequestMappingInfo> methods = MethodIntrospector
                .selectMethods(clazz, (MethodIntrospector.MetadataLookup<RequestMappingInfo>) method -> {
                    try {
                        return getMappingForMethod(method, clazz);
                    } catch (Throwable ex) {
                        throw new IllegalStateException(
                                "Invalid mapping on handler class [" + clazz.getName() + "]: " + method, ex);
                    }
                });

        Collection<HandlerMappingDto> result = Lists.newLinkedList();

        methods.forEach((method, requestMappingInfo) -> {
            HandlerMappingDto handlerMapping = new HandlerMappingDto();
            Set<RequestMethod> httpMethods = requestMappingInfo.getMethodsCondition().getMethods();
            Set<String> uris = requestMappingInfo.getPatternsCondition().getPatterns();
            handlerMapping.setMethodQualifierByToString(method.toString());
            handlerMapping.setHttpMethod(Iterables.getFirst(httpMethods, RequestMethod.GET).name());
            handlerMapping.setUri(Iterables.getFirst(uris, ""));
            result.add(handlerMapping);
        });

        return result;
    }

    protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
        RequestMappingInfo info = createRequestMappingInfo(method);
        if (info != null) {
            RequestMappingInfo typeInfo = createRequestMappingInfo(handlerType);
            if (typeInfo != null) {
                info = typeInfo.combine(info);
            }
            // ignore common prefix
//            String prefix = getPathPrefix(handlerType);
//            if (prefix != null) {
//                info = RequestMappingInfo.paths(prefix).options(this.config).build().combine(info);
//            }
        }
        return info;
    }

    private RequestMappingInfo createRequestMappingInfo(AnnotatedElement element) {
        RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(element, RequestMapping.class);
        RequestCondition<?> condition = (element instanceof Class ? getCustomTypeCondition((Class<?>) element)
                : getCustomMethodCondition((Method) element));
        return (requestMapping != null ? createRequestMappingInfo(requestMapping, condition) : null);
    }

    protected RequestCondition<?> getCustomTypeCondition(Class<?> handlerType) {
        return null;
    }

    protected RequestCondition<?> getCustomMethodCondition(Method method) {
        return null;
    }

    protected RequestMappingInfo createRequestMappingInfo(RequestMapping requestMapping,
            RequestCondition<?> customCondition) {

        RequestMappingInfo.Builder builder = RequestMappingInfo.paths(requestMapping.path())
                .methods(requestMapping.method()).params(requestMapping.params()).headers(requestMapping.headers())
                .consumes(requestMapping.consumes()).produces(requestMapping.produces())
                .mappingName(requestMapping.name());
        if (customCondition != null) {
            builder.customCondition(customCondition);
        }
        return builder.build();
    }

}
