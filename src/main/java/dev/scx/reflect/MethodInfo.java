package dev.scx.reflect;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/// MethodInfo
///
/// @author scx567888
/// @version 0.0.1
public sealed interface MethodInfo extends ExecutableInfo permits MethodInfoImpl {

    /// 原始 Method
    Method rawMethod();

    /// 名称
    String name();

    /// isAbstract
    boolean isAbstract();

    /// 是否为 final 方法
    boolean isFinal();

    /// 是否为 static 方法
    boolean isStatic();

    /// isNative
    boolean isNative();

    /// isDefault
    boolean isDefault();

    /// 返回值
    TypeInfo returnType();

    /// 方法签名 (仅建议用来进行重载比较)
    MethodSignature signature();

    /// 获取当前方法重写的父类的方法 (最近的一层)
    MethodInfo[] superMethods();

    /// 获取当前方法重写的所有方法 (广度遍历顺序)
    MethodInfo[] allSuperMethods();

    //************ 简化操作 **************

    @Override
    default void setAccessible(boolean flag) {
        rawMethod().setAccessible(flag);
    }

    default Object invoke(Object obj, Object... args) throws InvocationTargetException, IllegalAccessException {
        return rawMethod().invoke(obj, args);
    }

    @Override
    default AnnotatedElement annotatedElement() {
        return rawMethod();
    }

}
