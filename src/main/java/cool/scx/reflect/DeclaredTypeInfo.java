package cool.scx.reflect;

import java.lang.reflect.AnnotatedElement;

/// 声明类型
public sealed interface DeclaredTypeInfo extends TypeInfo, AccessModifierOwner, AnnotatedElementInfo permits ClassTypeInfo, InterfaceTypeInfo, EnumTypeInfo, AnnotationTypeInfo, RecordTypeInfo {

    @Override
    default AnnotatedElement annotatedElement() {
        return rawClass();
    }

}
