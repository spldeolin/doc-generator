package com.spldeolin.dg.core.classloader;

import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * @author Deolin 2019-12-31
 */
public class CustomClassLoaderTypeFactory extends TypeFactory {

    private static final long serialVersionUID = 5102533383636005355L;

    public CustomClassLoaderTypeFactory() {
        super(null);
    }

    @Override
    public ClassLoader getClassLoader() {
        return SpringBootFatJarClassLoader.classLoader;
    }

}
