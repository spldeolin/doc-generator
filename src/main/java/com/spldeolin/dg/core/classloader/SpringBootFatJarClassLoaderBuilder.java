package com.spldeolin.dg.core.classloader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.commons.io.FileUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2019-12-27
 */
@Log4j2
public class SpringBootFatJarClassLoaderBuilder {

    private final Path jarPath;

    public static final Map<Path, SpringBootFatJarClassLoaderBuilder> cache = Maps.newConcurrentMap();

    private SpringBootFatJarClassLoaderBuilder(Path jarPath) {
        this.jarPath = jarPath;
    }

    public static SpringBootFatJarClassLoaderBuilder getInstance(Path jarPath) {
        SpringBootFatJarClassLoaderBuilder result = cache.get(jarPath);
        if (result == null) {
            result = new SpringBootFatJarClassLoaderBuilder(jarPath);
            cache.put(jarPath, result);
        }
        return result;
    }

    public URLClassLoader build() {
        try {
            Path path = this.decompressJarToTempDir();
            Path bootInf = path.resolve("BOOT-INF");
            List<URL> urls = Lists.newArrayList(bootInf.resolve("classes").toUri().toURL());
            FileUtils.iterateFiles(bootInf.resolve("lib").toFile(), new String[]{"jar"}, true).forEachRemaining(jar -> {
                try {
                    urls.add(jar.toURI().toURL());
                } catch (MalformedURLException e) {
                    log.error(e.getMessage());
                    throw new RuntimeException();
                }
            });
            URLClassLoader urlClassLoader = new URLClassLoader(urls.toArray(new URL[0]));
            return urlClassLoader;
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    private Path decompressJarToTempDir() throws IOException {
        Path tempDir = Files.createTempDirectory("docgen" + LocalDateTime.now().toString());

        try (JarFile jar = new JarFile(jarPath.toFile())) {
            for (JarEntry src : Collections.list(jar.entries())) {
                File dest = tempDir.resolve(src.getName()).toFile();
                // mkdir or copy
                if (src.isDirectory()) {
                    if (!dest.mkdir()) {
                        log.error("mkdir [{}] error", dest.getPath());
                    }
                } else {
                    try (InputStream jarEntryIs = jar.getInputStream(src)) {
                        FileUtils.copyInputStreamToFile(jarEntryIs, dest);
                    }
                }
            }
        }
        return tempDir;
    }

}
