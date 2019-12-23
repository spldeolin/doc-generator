package com.spldeolin.dg.core.processor;

import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2019-12-23
 */
@Log4j2
public class UriProcessor {

    public String process(ClassOrInterfaceDeclaration controller, MethodDeclaration handler) {
        try {
            StringBuilder sb = new StringBuilder(64);
            controller.getAnnotationByName("RequestMapping").ifPresent(reqMapping -> getUrlPart(reqMapping, sb));
            handler.getAnnotations().stream().filter(anno -> StringUtils
                    .equalsAny(anno.getNameAsString(), "GetMapping", "PostMapping", "PutMapping", "PatchMapping",
                            "DeleteMapping", "RequestMapping")).forEach(anno -> getUrlPart(anno, sb));
            return sb.toString();
        } catch (Exception e) {
            log.info("无法解析URL[{}#{}]", controller.getNameAsString(), handler.getNameAsString());
            return "";
        }

    }

    private void getUrlPart(AnnotationExpr anno, StringBuilder sb) {
        anno.ifNormalAnnotationExpr(normalAnno -> normalAnno.getPairs().forEach(pair -> {
            if (StringUtils.equalsAny(pair.getNameAsString(), "value", "path")) {
                sb.append(pair.getValue().asStringLiteralExpr().getValue());
            }
        }));
        anno.ifSingleMemberAnnotationExpr(
                singleAnno -> sb.append(singleAnno.getMemberValue().asStringLiteralExpr().getValue()));
    }

}
