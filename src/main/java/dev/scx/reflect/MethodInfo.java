package dev.scx.reflect;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/// MethodInfo
///
/// @author scx567888
/// @version 0.0.1
public sealed interface MethodInfo extends ExecutableInfo permits MethodInfoImpl {

    Method rawMethod();

    String name();

    boolean isStatic();

    boolean isFinal();

    boolean isAbstract();

    boolean isDefault();

    boolean isNative();

    TypeInfo returnType();

    MethodSignature signature();

    // ************* 辅助方法 **************

    /// 当前方法在继承关系中直接对应的父方法集合.
    MethodInfo[] superMethods();

    /// 当前方法在继承关系中对应的全部父方法集合 (广度遍历顺序).
    MethodInfo[] allSuperMethods();

    default Object invoke(Object obj, Object... args) throws InvocationTargetException, IllegalAccessException {
        return rawMethod().invoke(obj, args);
    }

    @Override
    default void setAccessible(boolean flag) {
        rawMethod().setAccessible(flag);
    }

    @Override
    default AnnotatedElement annotatedElement() {
        return rawMethod();
    }

}
