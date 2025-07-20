package cool.scx.reflect;

import java.lang.reflect.Type;

import static cool.scx.reflect.TypeBindingsImpl.EMPTY_BINDINGS;
import static cool.scx.reflect.TypeFactory.typeOfFromAny;
import static cool.scx.reflect.TypeFactory.typeOfFromClass;

/// ScxReflect
///
/// @author scx567888
/// @version 0.0.1
public final class ScxReflect {

    /// 根据 Class 获取 TypeInfo
    public static TypeInfo typeOf(Class<?> type) {
        return typeOfFromClass(type);
    }

    /// 根据 Type 获取 TypeInfo
    public static TypeInfo typeOf(Type type) {
        return typeOfFromAny(type, new TypeResolutionContext(EMPTY_BINDINGS));
    }

    /// 根据 TypeReference 获取 TypeInfo
    public static TypeInfo typeOf(TypeReference<?> typeReference) {
        return typeOfFromAny(typeReference.getType(), new TypeResolutionContext(EMPTY_BINDINGS));
    }

}
