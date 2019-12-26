package com.spldeolin.dg.core.lcbi;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.google.common.collect.Maps;
import com.spldeolin.dg.Conf;
import com.spldeolin.dg.core.util.Jsons;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2019-12-16
 */
@Log4j2
public class LoadClassBasedInfoImporter {

    private static final Map<String, JsonSchema> jsonSchemas;

    private static final Map<String, String> handlerUris;

    private static final Map<String, String> handlerHttpMethods;

    static {
        File file = Paths.get(Conf.POJO_SCHEMA_PATH).toFile();
        String json = null;
        try {
            json = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            log.info("pojo schema has been import from [{}].", file);
        } catch (IOException e) {
            log.error("FileUtils.readFileToString({})", file, e);
        }
        Collection<PojoSchemaDto> pojoSchemaList = Jsons.toListOfObjects(json, PojoSchemaDto.class);
        jsonSchemas = Maps.newHashMapWithExpectedSize(pojoSchemaList.size());
        pojoSchemaList.forEach(one -> jsonSchemas.put(one.getPojoQualifier(), one.getJsonSchema()));

        file = Paths.get(Conf.HANDLER_MAPPING_PATH).toFile();
        try {
            json = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            log.info("handler mapping has been import from [{}].", file);
        } catch (IOException e) {
            log.error("FileUtils.readFileToString({})", file, e);
        }
        Collection<HandlerMappingDto> handlerMappingList = Jsons.toListOfObjects(json, HandlerMappingDto.class);
        handlerUris = Maps.newHashMapWithExpectedSize(pojoSchemaList.size());
        handlerHttpMethods = Maps.newHashMapWithExpectedSize(pojoSchemaList.size());
        handlerMappingList.forEach(one -> {
            handlerUris.put(one.getMethodQualifierByToString(), one.getUri());
            handlerHttpMethods.put(one.getMethodQualifierByToString(), one.getHttpMethod());
        });
    }

    public static JsonSchema getJsonSchema(String classQualifier) {
        return jsonSchemas.get(classQualifier);
    }

    public static String getHandlerUri(String methodQualifierByToString) {
        return handlerUris.get(methodQualifierByToString);
    }

    public static String getHandlerHttpMethods(String methodQualifierByToString) {
        return handlerHttpMethods.get(methodQualifierByToString);
    }

}
