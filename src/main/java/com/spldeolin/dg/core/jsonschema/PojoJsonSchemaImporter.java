package com.spldeolin.dg.core.jsonschema;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.google.common.collect.Maps;
import com.spldeolin.dg.core.util.Jsons;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2019-12-16
 */
@Log4j2
public class PojoJsonSchemaImporter {

    public static final Map<String, JsonSchema> jsonSchemas;

    static {
        File file = Paths.get("pojo-schema.json").toFile();
        String json = null;
        try {
            json = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            log.info("result has been import from [{}].", file);
        } catch (IOException e) {
            log.error("FileUtils.readFileToString({})", file, e);
        }
        Collection<PojoJsonSchemaData> pojoJsonSchemas = Jsons.toListOfObjects(json, PojoJsonSchemaData.class);

        jsonSchemas = Maps.newHashMapWithExpectedSize(pojoJsonSchemas.size());
        pojoJsonSchemas.forEach(one -> jsonSchemas.put(one.getPojoQualifier(), one.getJsonSchema()));
    }

    public static JsonSchema getJsonSchema(String classQualifier) {
        return jsonSchemas.get(classQualifier);
    }

}
