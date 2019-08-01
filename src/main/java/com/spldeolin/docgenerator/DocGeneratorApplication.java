package com.spldeolin.docgenerator;

import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.spldeolin.docgenerator.scaner.JavaFileScaner;
import lombok.extern.log4j.Log4j2;

@SpringBootApplication
@Log4j2
public class DocGeneratorApplication {

    public static void main(String[] args) {
//		SpringApplication.run(DoGeneratorApplication.class, args);

        List<CompilationUnit> units = JavaFileScaner
                .scanAndParseRecursively("/Users/deolin/Documents/project-repo/motherbuy");
        List<ClassOrInterfaceDeclaration> controllers = listControllers(units);
        List<MethodDeclaration> handlers = listHandlers(controllers);

        for (MethodDeclaration handler : handlers) {
            String url = concatUrl(handler);

        }
    }

    private static List<ClassOrInterfaceDeclaration> listControllers(List<CompilationUnit> units) {
        List<ClassOrInterfaceDeclaration> controllers = Lists.newArrayList();
        for (CompilationUnit unit : units) {
            Node node = Iterables.getLast(unit.getChildNodes());
            if (node instanceof ClassOrInterfaceDeclaration) {
                ClassOrInterfaceDeclaration clazz = (ClassOrInterfaceDeclaration) node;
                if (clazz.isPublic()) {
                    if (clazz.getAnnotations().stream().anyMatch(one -> StringUtils.equalsAny(one.getNameAsString(),
                            "Controller", "RestController"))) {
                        if (isSupportToParse(clazz)) {
                            controllers.add(clazz);
                        }
                    }
                }
            }
        }
        return controllers;
    }

    private static boolean isSupportToParse(ClassOrInterfaceDeclaration clazz) {
        // TODO controller同时声明了@Controller与@RestController时暂不支持
        // TODO @RequestMapping value指定String数组时暂不支持
        // TODO @RequestMapping 同时指定了值时暂不支持
        return false;
    }

    private static List<MethodDeclaration> listHandlers(List<ClassOrInterfaceDeclaration> controllers) {
        List<MethodDeclaration> handlers = Lists.newArrayList();
        for (ClassOrInterfaceDeclaration controller : controllers) {
            for (MethodDeclaration method : controller.getMethods()) {
                if (method.getAnnotations().stream().anyMatch(one -> StringUtils.equalsAny(one.getNameAsString(),
                        "RequestMapping", "GetMppping", "PostMapping", "PutMapping", "DeleteMapping"))) {
                    if (isSupportToParse(method)) {
                        handlers.add(method);
                    }
                }
            }
        }
        return handlers;
    }

    private static boolean isSupportToParse(MethodDeclaration method) {
        // TODO handler声明了"RequestMapping", "GetMppping", "PostMapping", "PutMapping", "DeleteMapping"中的至少2个时
        // TODO handler与controller都声明了requestMethod并且不一致时
        // TODO @RequestMapping value指定String数组时暂不支持
        // TODO @RequestMapping 同时指定了值时暂不支持
        return false;
    }

    private static String concatUrl(MethodDeclaration handler) {
        ClassOrInterfaceDeclaration controller = (ClassOrInterfaceDeclaration) handler.getParentNode()
                .orElseThrow(() -> new RuntimeException("Handler不应没有parentNode"));
        return Joiner.on("").join(getControllerUrl(controller), getHandlerUrl(handler));
    }

    private static String getControllerUrl(ClassOrInterfaceDeclaration controller) {
        Optional<AnnotationExpr> requestMappingOpt = controller.getAnnotationByClass(RequestMapping.class);
        String result;
        if (requestMappingOpt.isPresent()) {
            result = getPathValueFromAnnotation(requestMappingOpt.get());
        } else {
            result = "";
        }

        if (!result.startsWith("/")) {
            result = "/" + result;
        }
        if (!result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    private static String getHandlerUrl(MethodDeclaration handler) {
        NodeList<AnnotationExpr> annotations = handler.getAnnotations();
        if (annotations.size() == 0) {
            return "";
        }
        for (AnnotationExpr annotation : annotations) {
            if (StringUtils.equalsAny(annotation.getNameAsString(), "RequestMapping", "GetMapping", "PostMapping",
                    "PutMapping", "DeleteMapping")) {
                return getPathValueFromAnnotation(annotation);
            }
        }

        return null;
    }

    private static String getPathValueFromAnnotation(AnnotationExpr annotation) {
        String result = "";
        if (annotation.isSingleMemberAnnotationExpr()) {
            // e.g.: @RequestMapping("a")
            result = annotation.asSingleMemberAnnotationExpr().getMemberValue().asLiteralStringValueExpr()
                    .getValue();
        } else if (annotation.isNormalAnnotationExpr()) {
            // e.g.: @RequestMapping(path = "/a", method = RequestMethod.GET)
            boolean hasValuePair = false;
            for (MemberValuePair pair : annotation.asNormalAnnotationExpr().getPairs()) {
                if ("value".equals(pair.getNameAsString())) {
                    hasValuePair = true;
                    result = pair.getValue().asLiteralStringValueExpr().getValue();
                } else if ("path".equals(pair.getNameAsString())) {
                    if (hasValuePair) {
                        throw new UnsupportedOperationException("@RequestMapping的value与path"
                                + "属性互为别名，无法解析同时在@RequestMapping中指定了value"
                                + "和path属性的controller");
                    }
                    result = pair.getValue().asLiteralStringValueExpr().getValue();
                } else {
                    result = "";
                }
            }
        } else {
            // e.g.: @RequestMapping
            result = "";
        }
        return result;
    }

}
