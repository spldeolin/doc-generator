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
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import com.google.common.collect.Lists;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2019-12-27
 */
@Log4j2
public class WarClassLoader {

    public static final URLClassLoader classLoader;

    static {
        try {
            Path path = decompressWarToTempDir();
            Path webInf = path.resolve("WEB-INF");

            List<URL> urls = Lists.newLinkedList();
            urls.add(webInf.resolve("classes").toUri().toURL());
            FileUtils.iterateFiles(webInf.resolve("lib").toFile(), new String[]{"jar"}, true).forEachRemaining(jar -> {
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

    public static void main(String[] args) {
        log.info(classLoader);
    }

    private static Path decompressWarToTempDir() throws IOException {
        Path tempDir = Files.createTempDirectory("docgen" + LocalDateTime.now().toString());
        tempDir.toFile().deleteOnExit();

        try (ZipFile zip = new ZipFile(
                "/Users/deolin/Documents/project-repo/spring-mvc-showcase/target/spring-mvc-showcase.war")) {
            for (ZipEntry src : Collections.list(zip.entries())) {
                File dest = tempDir.resolve(src.getName()).toFile();
                // mkdir or copy
                if (src.isDirectory()) {
                    if (!dest.mkdir()) {
                        log.error("mkdir [{}] error", dest.getPath());
                    }
                } else {
                    try (InputStream zipInputStream = zip.getInputStream(src)) {
                        FileUtils.copyInputStreamToFile(zipInputStream, dest);
                    }
                }
            }
        }
        log.info("decompressWarToTempDir=[{}]", tempDir);
        return tempDir;
    }

}
