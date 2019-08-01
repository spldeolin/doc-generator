package com.spldeolin.docgenerator.scaner;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.io.FileUtils;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.google.common.collect.Lists;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2019-07-31
 */
@Log4j2
public class JavaFileScaner {

    public static void main(String[] args) {
        System.out.println(scanRecursively("/Users/deolin/Documents/project-repo/motherbuy"));
    }

    public static List<String> scanRecursively(String dirPath) {
        File dir = new File(dirPath);
        Iterator<File> files = FileUtils.iterateFiles(dir, new String[]{"java"}, true);
        List<String> codes = Lists.newArrayList();
        while (files.hasNext()) {
            String code;
            try {
                code = FileUtils.readFileToString(files.next(), StandardCharsets.UTF_8);
                // 项目中存在没有任何内容的java文件，依然是可以运行的，这里需要排除掉这些空文件
                if (code.length() > 0) {
                    codes.add(code);
                }
            } catch (IOException e) {
                log.error(e);
            }
        }
        return codes;
    }

    public static List<CompilationUnit> scanAndParseRecursively(String dirPath) {
        List<String> codes = JavaFileScaner.scanRecursively(dirPath);
        List<CompilationUnit> units = Lists.newArrayListWithCapacity(codes.size());
        for (String code : codes) {
            units.add(StaticJavaParser.parse(code));
        }
        return units;
    }

}
