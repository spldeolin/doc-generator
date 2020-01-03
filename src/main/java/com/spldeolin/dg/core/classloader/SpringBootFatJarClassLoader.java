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
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.commons.io.FileUtils;
import com.google.common.collect.Lists;
import com.spldeolin.dg.Conf;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2019-12-27
 */
@Log4j2
public class SpringBootFatJarClassLoader {

    public static final URLClassLoader classLoader;

    static {
        try {
            Path path = decompressJarToTempDir();
            Path bootInf = path.resolve("BOOT-INF");
            List<URL> urls = Lists.newArrayList(bootInf.resolve("classes").toUri().toURL(),
                    new URL(SpringBootFatJarClassLoader.class.getProtectionDomain().getCodeSource().getLocation(),
                            "rt"));

            FileUtils.iterateFiles(bootInf.resolve("lib").toFile(), new String[]{"jar"}, true).forEachRemaining(jar -> {
                try {
                    urls.add(jar.toURI().toURL());
                } catch (MalformedURLException e) {
                    log.error(e.getMessage());
                    throw new RuntimeException();
                }
            });
            classLoader = new URLClassLoader(urls.toArray(new URL[0]));
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    private static Path decompressJarToTempDir() throws IOException {
        Path tempDir = Files.createTempDirectory("docgen" + LocalDateTime.now().toString());
        tempDir.toFile().deleteOnExit();

        try (JarFile jar = new JarFile(Conf.TARGET_SPRING_BOOT_FAT_JAR_PATH.toFile())) {
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
