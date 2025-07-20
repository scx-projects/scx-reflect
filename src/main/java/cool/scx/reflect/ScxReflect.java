package cool.scx.reflect;

import java.lang.reflect.Type;

import static cool.scx.reflect.TypeBindingsImpl.EMPTY_BINDINGS;
import static cool.scx.reflect.TypeFactory.typeofFromAny;
import static cool.scx.reflect.TypeFactory.typeofFromClass;

/// ScxReflect
///
/// @author scx567888
/// @version 0.0.1
public final class ScxReflect {

    /// 根据 Class 获取 TypeInfo
    public static TypeInfo typeof(Class<?> type) {
        return typeofFromClass(type);
    }

    /// 根据 Type 获取 TypeInfo
    public static TypeInfo typeof(Type type) {
        return typeofFromAny(type, new TypeResolutionContext(EMPTY_BINDINGS));
    }

    /// 根据 TypeReference 获取 TypeInfo
    public static TypeInfo typeof(TypeReference<?> typeReference) {
        return typeofFromAny(typeReference.getType(), new TypeResolutionContext(EMPTY_BINDINGS));
    }

}
