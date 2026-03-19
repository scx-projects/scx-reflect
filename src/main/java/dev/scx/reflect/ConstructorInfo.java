package dev.scx.reflect;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/// ConstructorInfo
///
/// @author scx567888
/// @version 0.0.1
public sealed interface ConstructorInfo extends ExecutableInfo permits ConstructorInfoImpl {

    Constructor<?> rawConstructor();

    // ************* 便捷方法 **************

    default Object newInstance(Object... args) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return rawConstructor().newInstance(args);
    }

    @Override
    default void setAccessible(boolean flag) {
        rawConstructor().setAccessible(flag);
    }

    @Override
    default AnnotatedElement annotatedElement() {
        return rawConstructor();
    }

}
