package cool.scx.reflect;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;

import static cool.scx.reflect.TypeBindingsImpl.EMPTY_BINDINGS;

/// 非线程安全
public final class ScxReflect {

    /// Class 的快速路径
    private static final ClassValue<TypeInfo> CLASS_CACHE = new ClassValue<>() {
        @Override
        protected TypeInfo computeValue(Class<?> clazz) {
            if (clazz.isArray()) {
                return new ArrayTypeInfoImpl(clazz);
            }
            if (clazz.isPrimitive()) {
                return new PrimitiveTypeInfoImpl(clazz);
            }
            return new ClassInfoImpl(clazz);
        }
    };

    /// 携带泛型的
    static final Map<Object, TypeInfo> TYPE_CACHE = new HashMap<>();

    /// 仅做分发
    static TypeInfo getTypeFromAny(Type type, TypeBindings bindings) {
        return switch (type) {
            case Class<?> c -> getTypeFromClass(c);
            case ParameterizedType p -> getTypeFromParameterizedType(p, bindings);
            case GenericArrayType g -> getTypeFromGenericArrayType(g, bindings);
            case TypeVariable<?> t -> getTypeFromTypeVariable(t, bindings);
            case WildcardType w -> getTypeFromWildcardType(w, bindings);
            default -> throw new IllegalArgumentException("Unsupported type: " + type);
        };
    }

    private static TypeInfo getTypeFromTypeVariable(TypeVariable<?> typeVariable, TypeBindings bindings) {
        //尝试从从绑定中获取 否则回退到 上界
        var typeInfo = bindings.get(typeVariable);
        if (typeInfo != null) {
            return typeInfo;
        }
        return getTypeFromAny(typeVariable.getBounds()[0], bindings);
    }

    private static TypeInfo getTypeFromWildcardType(WildcardType wildcardType, TypeBindings bindings) {
        return getTypeFromAny(wildcardType.getUpperBounds()[0], bindings);
    }

    /// 
    private static TypeInfo getTypeFromParameterizedType(ParameterizedType type, TypeBindings bindings) {
        var typeKey = TypeKey.createTypeKey(type, bindings);
        var t = TYPE_CACHE.get(typeKey);
        if (t != null) {
            return t;
        }
        return new ClassInfoImpl(type, bindings);
    }

    /// 
    private static TypeInfo getTypeFromGenericArrayType(GenericArrayType type, TypeBindings bindings) {
        return new ArrayTypeInfoImpl(type, bindings);
    }

    /// 快速路径
    static TypeInfo getTypeFromClass(Class<?> type) {
        return CLASS_CACHE.get(type);
    }

    //********************* 只向外暴漏两个常用方法 ******************

    /// 根据 Class 获取 TypeInfo
    public static TypeInfo getType(Class<?> type) {
        return getTypeFromClass(type);
    }

    /// 根据 TypeReference 获取 TypeInfo
    public static TypeInfo getType(TypeReference<?> typeReference) {
        return getTypeFromAny(typeReference.getType(), EMPTY_BINDINGS);
    }

}
