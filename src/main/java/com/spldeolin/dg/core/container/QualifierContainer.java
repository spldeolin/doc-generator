package com.spldeolin.dg.core.container;

import java.nio.file.Path;
import java.util.Map;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.spldeolin.dg.core.exception.QualifierAbsentException;
import lombok.Getter;

/**
 * @author Deolin 2019-12-30
 */
public class QualifierContainer {

    private Multimap<String, String> byClassName = ArrayListMultimap.create(ClassContainer.EXPECTED, 1);

    private Multimap<String, String> byEnumName = ArrayListMultimap.create(EnumContainer.EXPECTED, 1);

    private Multimap<String, String> byInterfaceName = ArrayListMultimap.create(InterfaceContainer.EXPECTED, 1);

    @Getter
    private final Path path;

    private static Map<Path, QualifierContainer> instances = Maps.newConcurrentMap();

    public static QualifierContainer getInstance(Path path) {
        QualifierContainer result = instances.get(path);
        if (result == null) {
            result = new QualifierContainer(path);
            instances.put(path, result);
        }
        return result;
    }

    private QualifierContainer(Path path) {
        this.path = path;
    }

    public Multimap<String, String> getByClassName() {
        if (byClassName.size() == 0) {
            ClassContainer.getInstance(path).getAll().forEach(classDeclaration -> byClassName
                    .put(classDeclaration.getNameAsString(),
                            classDeclaration.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new)));
        }
        return byClassName;
    }

    public Multimap<String, String> getByEnumName() {
        if (byEnumName.size() == 0) {
            EnumContainer.getInstance(path).getAll().forEach(enumDeclaration -> byEnumName
                    .put(enumDeclaration.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new),
                            enumDeclaration.getNameAsString()));
        }
        return byEnumName;
    }

    public Multimap<String, String> getByInterfaceName() {
        if (byInterfaceName.size() == 0) {
            InterfaceContainer.getInstance(path).getAll().forEach(iinterface -> byInterfaceName
                    .put(iinterface.getNameAsString(),
                            iinterface.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new)));
        }
        return byInterfaceName;
    }

}
