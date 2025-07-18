package cool.scx.reflect;

import java.lang.reflect.Type;

import static cool.scx.reflect.TypeBindingsImpl.EMPTY_BINDINGS;
import static cool.scx.reflect.TypeFactory.getTypeFromAny;
import static cool.scx.reflect.TypeFactory.getTypeFromClass;

public final class ScxReflect {

    /// 根据 Class 获取 TypeInfo
    public static TypeInfo getType(Class<?> type) {
        return getTypeFromClass(type);
    }

    /// 根据 Type 获取 TypeInfo
    public static TypeInfo getType(Type type) {
        return getTypeFromAny(type, new TypeResolutionContext(EMPTY_BINDINGS));
    }

    /// 根据 TypeReference 获取 TypeInfo
    public static TypeInfo getType(TypeReference<?> typeReference) {
        return getTypeFromAny(typeReference.getType(), new TypeResolutionContext(EMPTY_BINDINGS));
    }

}
