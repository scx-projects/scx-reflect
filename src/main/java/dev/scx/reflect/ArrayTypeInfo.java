package dev.scx.reflect;

import java.lang.reflect.Array;

/// ArrayTypeInfo
///
/// @author scx567888
/// @version 0.0.1
public sealed interface ArrayTypeInfo extends TypeInfo permits ArrayTypeInfoImpl {

    TypeInfo componentType();

    default Object newArray(int length) {
        return Array.newInstance(componentType().rawClass(), length);
    }

    @Override
    default boolean isRaw() {
        return componentType().isRaw();
    }

}
