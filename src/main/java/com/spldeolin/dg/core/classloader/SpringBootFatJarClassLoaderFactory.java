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
public class SpringBootFatJarClassLoaderFactory {

    private static Map<Path, URLClassLoader> instancesCache = Maps.newHashMap();

    public static URLClassLoader create(Path jarPath) {
        URLClassLoader result = instancesCache.get(jarPath);

        if (result == null) {
            try {
                Path path = decompressJarToTempDir(jarPath);
                Path bootInf = path.resolve("BOOT-INF");
                List<URL> urls = Lists.newArrayList(bootInf.resolve("classes").toUri().toURL(),
                        new URL(SpringBootFatJarClassLoaderFactory.class.getProtectionDomain().getCodeSource()
                                .getLocation(), "rt"));

                FileUtils.iterateFiles(bootInf.resolve("lib").toFile(), new String[]{"jar"}, true)
                        .forEachRemaining(jar -> {
                            try {
                                urls.add(jar.toURI().toURL());
                            } catch (MalformedURLException e) {
                                log.error(e.getMessage());
                                throw new RuntimeException();
                            }
                        });
                result = new URLClassLoader(urls.toArray(new URL[0]));
                instancesCache.put(jarPath, result);
            } catch (IOException e) {
                throw new RuntimeException();
            }
        }
        return result;
    }

    private static Path decompressJarToTempDir(Path jarPath) throws IOException {
        Path tempDir = Files.createTempDirectory("docgen" + LocalDateTime.now().toString());
        tempDir.toFile().deleteOnExit();

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
        log.info("decompressJarToTempDir=[{}]", tempDir);
        return tempDir;
    }

}
