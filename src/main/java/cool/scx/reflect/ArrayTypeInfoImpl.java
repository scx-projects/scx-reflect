package cool.scx.reflect;

import cool.scx.reflect.ScxReflect.TypeKey;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;

import static cool.scx.reflect.ReflectSupport._findComponentType;
import static cool.scx.reflect.ScxReflect.TYPE_CACHE;

/// ArrayTypeInfoImpl
///
/// @author scx567888
/// @version 0.0.1
final class ArrayTypeInfoImpl implements ArrayTypeInfo {

    private final TypeInfo componentType;

    ArrayTypeInfoImpl(Type type, Map<TypeVariable<?>, TypeInfo> bindings) {
        TYPE_CACHE.put(new TypeKey(type, bindings), this);

        // 我们假设 此处 type 已经是过滤后的
        this.componentType = _findComponentType(type, bindings);
    }

    @Override
    public TypeInfo componentType() {
        return componentType;
    }

    @Override
    public String toString() {
        return componentType.toString() + "[]";
    }

}
