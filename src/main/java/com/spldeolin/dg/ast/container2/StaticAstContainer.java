package com.spldeolin.dg.ast.container2;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.google.common.collect.Maps;
import com.spldeolin.dg.Conf;

/**
 * 存放抽象语法树节点的容器的静态访问口
 *
 * @author Deolin 2020-02-03
 */
public class StaticAstContainer {

    private static AstContainer fromConfigPath = new AstContainer(Conf.PROJECT_PATH);

    private static Map<Path, AstContainer> fromCustomPath = Maps.newHashMap();

    public Path getPath() {
        return fromConfigPath.getPath();
    }

    public Collection<CompilationUnit> getCompilationUnits() {
        return fromConfigPath.getCompilationUnits();
    }

    public Collection<ClassOrInterfaceDeclaration> getClassOrInterfaceDeclarations() {
        return fromConfigPath.getClassOrInterfaceDeclarations();
    }

    public ClassOrInterfaceDeclaration getClassOrInterfaceDeclaration(String qualifier) {
        return fromConfigPath.getClassOrInterfaceDeclaration(qualifier);
    }

    public Collection<EnumDeclaration> getEnumDeclarations() {
        return fromConfigPath.getEnumDeclarations();
    }

    public EnumDeclaration getEnumDeclaration(String qualifier) {
        return fromConfigPath.getEnumDeclaration(qualifier);
    }

    public Collection<VariableDeclarator> getFieldVariableDeclarators() {
        return fromConfigPath.getFieldVariableDeclarators();
    }

    public VariableDeclarator getFieldVariableDeclarator(String qualifier) {
        return fromConfigPath.getFieldVariableDeclarator(qualifier);
    }

    public AstContainer getAstContainerByCustomPath(Path path) {
        AstContainer container = fromCustomPath.get(path);
        if (container == null) {
            container = new AstContainer(path);
            fromCustomPath.put(path, container);
        }
        return container;
    }

}
