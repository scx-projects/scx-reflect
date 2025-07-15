package cool.scx.reflect;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;

import static cool.scx.reflect.TypeBindingsImpl.EMPTY_BINDINGS;

/// 非线程安全
public final class ScxReflect {

    static final Map<TypeKey, TypeInfo> TYPE_CACHE = new HashMap<>();

    static TypeInfo getType(Type type, TypeBindings bindings) {
        var t = TYPE_CACHE.get(new TypeKey(type, bindings));
        if (t != null) {
            return t;
        }
        if (type instanceof Class<?> c) {
            if (c.isArray()) {
                return new ArrayTypeInfoImpl(c, bindings);
            }
            if (c.isPrimitive()) {
                return new PrimitiveTypeInfoImpl(c);
            }
            return new ClassInfoImpl(c, bindings);
        }
        if (type instanceof ParameterizedType p) {
            return new ClassInfoImpl(p, bindings);
        }
        if (type instanceof GenericArrayType g) {
            return new ArrayTypeInfoImpl(g, bindings);
        }
        if (type instanceof TypeVariable<?> typeVariable) {
            //尝试从从绑定中获取 否则回退到 上界
            var typeInfo = bindings.get(typeVariable);
            if (typeInfo != null) {
                return typeInfo;
            }
            return getType(typeVariable.getBounds()[0], bindings);
        }
        if (type instanceof WildcardType wildcardType) {
            //直接回退到上界
            return getType(wildcardType.getUpperBounds()[0], bindings);
        }
        throw new IllegalArgumentException("unsupported type: " + type);
    }

    public static TypeInfo getType(TypeReference<?> typeReference) {
        return getType(typeReference.getType());
    }

    public static TypeInfo getType(Type type) {
        return getType(type, EMPTY_BINDINGS);
    }

}
