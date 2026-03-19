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

    public static TypeInfo typeOf(Class<?> clazz) {
        return typeOfClass(clazz);
    }

    public static TypeInfo typeOf(Type type) {
        return typeOfAny(type, new TypeResolutionContext(EMPTY_BINDINGS));
    }

    public static TypeInfo typeOf(TypeReference<?> typeReference) {
        return typeOfAny(typeReference.type(), new TypeResolutionContext(EMPTY_BINDINGS));
    }

}
