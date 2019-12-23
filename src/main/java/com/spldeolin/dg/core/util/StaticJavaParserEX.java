package com.spldeolin.dg.core.util;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.utils.CodeGenerationUtils;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2019-11-14
 */
@Log4j2
public class StaticJavaParserEX {

    public static FieldDeclaration parseField(String code) {
        if (!code.endsWith(";")) {
            code += ";";
        }
        return StaticJavaParser.parseBodyDeclaration(code).asFieldDeclaration();
    }

    public static AnnotationExpr parseAnno(String code) {
        return StaticJavaParser.parseAnnotation(code);
    }

    public static ClassOrInterfaceDeclaration parseCoid(String code) {
        return StaticJavaParser.parseTypeDeclaration(code).asClassOrInterfaceDeclaration();
    }

    public static Statement parseStmt(String code) {
        return StaticJavaParser.parseStatement(code);
    }

    public static Parameter parseParameter(String code) {
        return StaticJavaParser.parseParameter(code);
    }

    public static void main(String[] args) {
        log.info(parseField("private CurrentUserDto currentUser;"));
        log.info("");
        log.info(parseAnno("@Data"));
        log.info("");
        log.info(parseCoid("@Data public class DaaEntity {}"));
        log.info("");
        log.info(CodeGenerationUtils.packageToPath("src.main.java."));
        log.info("");
        log.info(parseParameter(String.format("@RequestBody @Valid %s req", "Naaa")));
    }

}
