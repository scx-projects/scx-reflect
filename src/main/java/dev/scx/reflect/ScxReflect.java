package dev.scx.reflect;

import java.lang.reflect.Type;

import static dev.scx.reflect.TypeBindingsImpl.EMPTY_BINDINGS;
import static dev.scx.reflect.TypeFactory.typeOfAny;
import static dev.scx.reflect.TypeFactory.typeOfClass;

/// ScxReflect
///
/// @author scx567888
/// @version 0.0.1
public final class ScxReflect {

    /// 根据 Class 获取 TypeInfo
    public static TypeInfo typeOf(Class<?> type) {
        return typeOfClass(type);
    }

    /// 根据 Type 获取 TypeInfo
    public static TypeInfo typeOf(Type type) {
        return typeOfAny(type, new TypeResolutionContext(EMPTY_BINDINGS));
    }

    /// 根据 TypeReference 获取 TypeInfo
    public static TypeInfo typeOf(TypeReference<?> typeReference) {
        return typeOfAny(typeReference.getType(), new TypeResolutionContext(EMPTY_BINDINGS));
    }

}
