package cool.scx.reflect;

import static cool.scx.reflect.TypeBindingsImpl.EMPTY_BINDINGS;
import static cool.scx.reflect.TypeFactory.getTypeFromAny;
import static cool.scx.reflect.TypeFactory.getTypeFromClass;

public final class ScxReflect {

    /// 根据 Class 获取 TypeInfo
    public static TypeInfo getType(Class<?> type) {
        return getTypeFromClass(type);
    }

    /// 根据 TypeReference 获取 TypeInfo
    public static TypeInfo getType(TypeReference<?> typeReference) {
        return getTypeFromAny(typeReference.getType(), EMPTY_BINDINGS);
    }

}
