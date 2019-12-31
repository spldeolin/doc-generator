package com.spldeolin.dg.core.classloader;

import java.nio.file.Path;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * @author Deolin 2019-12-31
 */
public class CustomClassLoaderTypeFactory extends TypeFactory {

    private static final long serialVersionUID = 5102533383636005355L;

    private final Path path;

    public CustomClassLoaderTypeFactory(Path path) {
        super(null);
        this.path = path;
    }

    @Override
    public ClassLoader getClassLoader() {
        return SpringBootFatJarClassLoaderFactory.create(path);
    }

}
