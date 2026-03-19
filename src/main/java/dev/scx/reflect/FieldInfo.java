package dev.scx.reflect;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;

/// FieldInfo
///
/// @author scx567888
/// @version 0.0.1
public sealed interface FieldInfo extends MemberInfo permits FieldInfoImpl {

    Field rawField();

    String name();

    boolean isStatic();

    boolean isFinal();

    TypeInfo fieldType();

    // ************* 便捷方法 **************

    default Object get(Object obj) throws IllegalArgumentException, IllegalAccessException {
        return rawField().get(obj);
    }

    default void set(Object obj, Object value) throws IllegalArgumentException, IllegalAccessException {
        rawField().set(obj, value);
    }

    @Override
    default void setAccessible(boolean flag) {
        rawField().setAccessible(flag);
    }

    @Override
    default AnnotatedElement annotatedElement() {
        return rawField();
    }

}
