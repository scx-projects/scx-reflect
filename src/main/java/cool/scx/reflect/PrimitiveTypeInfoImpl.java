package cool.scx.reflect;

import cool.scx.reflect.ScxReflect.TypeKey;

import java.util.Map;

import static cool.scx.reflect.ScxReflect.TYPE_CACHE;

/// PrimitiveTypeInfoImpl
///
/// @author scx567888
/// @version 0.0.1
final class PrimitiveTypeInfoImpl implements PrimitiveTypeInfo {

    private final Class<?> rawClass;

    PrimitiveTypeInfoImpl(Class<?> primitiveClass) {
        TYPE_CACHE.put(new TypeKey(primitiveClass, Map.of()), this);

        // 我们假设 此处 primitiveClass 已经是 Class.isPrimitive 过滤后的
        this.rawClass = primitiveClass;
    }

    @Override
    public Class<?> rawClass() {
        return rawClass;
    }

    @Override
    public String toString() {
        return rawClass.getName();
    }

}
