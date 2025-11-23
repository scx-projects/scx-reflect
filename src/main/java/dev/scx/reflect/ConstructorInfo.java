package dev.scx.reflect;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/// ConstructorInfo
///
/// @author scx567888
/// @version 0.0.1
public sealed interface ConstructorInfo extends ExecutableInfo permits ConstructorInfoImpl {

    /// 获取原始的 构造函数
    Constructor<?> rawConstructor();

    //************* 简化方法 **************

    @Override
    default void setAccessible(boolean flag) {
        rawConstructor().setAccessible(flag);
    }

    @SuppressWarnings("unchecked")
    default <T> T newInstance(Object... args) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        return (T) rawConstructor().newInstance(args);
    }

    @Override
    default AnnotatedElement annotatedElement() {
        return rawConstructor();
    }

}
