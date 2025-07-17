package cool.scx.reflect;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;

import static cool.scx.reflect.TypeBindingsImpl.EMPTY_BINDINGS;

/// 非线程安全
public final class ScxReflect {

    /// 携带泛型的
    static final Map<Object, TypeInfo> TYPE_CACHE = new HashMap<>();

    /// 仅做分发
    static TypeInfo getTypeFromAny(Type type) {
        return switch (type) {
            case Class<?> c -> getTypeFromClass(c);
            case ParameterizedType p -> getTypeFromParameterizedType(p);
            case GenericArrayType g -> getTypeFromGenericArrayType(g);
            case TypeVariable<?> t -> getTypeFromTypeVariable(t);
            case WildcardType w -> getTypeFromWildcardType(w);
            default -> throw new IllegalArgumentException("Unsupported type: " + type);
        };
    }

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

    /// Class 永远不存在 bindings
    static TypeInfo getTypeFromClass(Class<?> clazz) {
        // 使用原始 Class 作为 Key
        var t = TYPE_CACHE.get(clazz);
        if (t != null) {
            return t;
        }
        if (clazz.isArray()) {
            var arrayTypeInfo = new ArrayTypeInfoImpl(clazz);
            TYPE_CACHE.put(clazz, arrayTypeInfo);
            return arrayTypeInfo;
        }
        if (clazz.isPrimitive()) {
            var primitiveTypeInfo = new PrimitiveTypeInfoImpl(clazz);
            TYPE_CACHE.put(clazz, primitiveTypeInfo);
            return primitiveTypeInfo;
        }
        var classInfo = new ClassInfoImpl(clazz);
        TYPE_CACHE.put(clazz, classInfo);
        return classInfo;
    }

    private static TypeInfo getTypeFromParameterizedType(ParameterizedType parameterizedType) {
        // 使用原始 ParameterizedType 作为 Key
        var t = TYPE_CACHE.get(parameterizedType);
        if (t != null) {
            return t;
        }
        return new ClassInfoImpl(parameterizedType);
    }

    private static TypeInfo getTypeFromParameterizedType(ParameterizedType type, TypeBindings contextBindings) {
        //如果上下文 bindings 为 空, 消解为 无上下文 bindings 的版本
        if (contextBindings == EMPTY_BINDINGS) {
            return getTypeFromParameterizedType(type);
        }
        //todo 这里就很复杂了 
        var typeKey = TypeKey.createTypeKey(type, contextBindings);
        var t = TYPE_CACHE.get(typeKey);
        if (t != null) {
            return t;
        }
        return new ClassInfoImpl(type, contextBindings);
    }

    private static TypeInfo getTypeFromGenericArrayType(GenericArrayType genericArrayType) {
        // 使用原始 GenericArrayType 作为 Key
        var t = TYPE_CACHE.get(genericArrayType);
        if (t != null) {
            return t;
        }
        return new ArrayTypeInfoImpl(genericArrayType);
    }

    private static TypeInfo getTypeFromGenericArrayType(GenericArrayType type, TypeBindings contextBindings) {
        //如果上下文 bindings 为 空, 消解为 无上下文 bindings 的版本
        if (contextBindings == EMPTY_BINDINGS) {
            return getTypeFromGenericArrayType(type);
        }
        //todo 这里就很复杂了 
        return new ArrayTypeInfoImpl(type, contextBindings);
    }

    private static TypeInfo getTypeFromTypeVariable(TypeVariable<?> typeVariable) {
        // 因为 没有 bindings 只能回退到上界
        return getTypeFromAny(typeVariable.getBounds()[0]);
    }

    private static TypeInfo getTypeFromTypeVariable(TypeVariable<?> typeVariable, TypeBindings contextBindings) {
        //尝试从从绑定中获取 否则回退到 上界
        var typeInfo = contextBindings.get(typeVariable);
        if (typeInfo != null) {
            return typeInfo;
        }
        return getTypeFromAny(typeVariable.getBounds()[0], contextBindings);
    }

    private static TypeInfo getTypeFromWildcardType(WildcardType wildcardType) {
        // 回退到上界
        return getTypeFromAny(wildcardType.getUpperBounds()[0]);
    }

    private static TypeInfo getTypeFromWildcardType(WildcardType wildcardType, TypeBindings contextBindings) {
        // 回退到上界
        return getTypeFromAny(wildcardType.getUpperBounds()[0], contextBindings);
    }


    //********************* 只向外暴漏两个常用方法 ******************

    /// 根据 Class 获取 TypeInfo
    public static TypeInfo getType(Class<?> type) {
        return getTypeFromClass(type);
    }

    /// 根据 TypeReference 获取 TypeInfo
    public static TypeInfo getType(TypeReference<?> typeReference) {
        return getTypeFromAny(typeReference.getType());
    }

    //todo 待移除
    public static TypeInfo getType(Type type) {
        return getTypeFromAny(type);
    }

}
