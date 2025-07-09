package cool.scx.reflect;

import cool.scx.reflect.ScxReflect.TypeKey;

import java.util.Map;

import static cool.scx.reflect.ScxReflect.TYPE_CACHE;

/// PrimitiveTypeInfoImpl
///
/// @author scx567888
/// @version 0.0.1
final class PrimitiveTypeInfoImpl implements PrimitiveTypeInfo {

    private final Class<?> primitiveClass;

    PrimitiveTypeInfoImpl(Class<?> primitiveClass) {
        TYPE_CACHE.put(new TypeKey(primitiveClass, Map.of()), this);

        // 我们假设 此处 primitiveClass 已经是过滤后的
        this.primitiveClass = primitiveClass;
    }

    @Override
    public Class<?> primitiveClass() {
        return primitiveClass;
    }

    @Override
    public String toString() {
        return primitiveClass.getName();
    }

}
