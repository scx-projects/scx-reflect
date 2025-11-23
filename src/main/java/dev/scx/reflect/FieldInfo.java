package dev.scx.reflect;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;

/// FieldInfo
///
/// @author scx567888
/// @version 0.0.1
public sealed interface FieldInfo extends MemberInfo permits FieldInfoImpl {

    /// 原始 Field
    Field rawField();

    /// 字段名称
    String name();

    /// 是否为 final 字段
    boolean isFinal();

    /// 是否 静态 字段
    boolean isStatic();

    /// 字段本身的类型
    TypeInfo fieldType();

    //************* 简化操作 *****************

    @Override
    default void setAccessible(boolean flag) {
        rawField().setAccessible(flag);
    }

    default void set(Object obj, Object value) throws IllegalAccessException {
        rawField().set(obj, value);
    }

    default Object get(Object obj) throws IllegalAccessException {
        return rawField().get(obj);
    }

    @Override
    default AnnotatedElement annotatedElement() {
        return rawField();
    }

}
