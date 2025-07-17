package cool.scx.reflect;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;

import static cool.scx.reflect.TypeBindingsImpl.EMPTY_BINDINGS;

/// 非线程安全
public final class ScxReflect {

//    /// Class 的快速路径
//    private static final ClassValue<TypeInfo> CLASS_CACHE = new ClassValue<>() {
//        @Override
//        protected TypeInfo computeValue(Class<?> clazz) {
//            if (clazz.isArray()) {
//                return new ArrayTypeInfoImpl(clazz);
//            }
//            if (clazz.isPrimitive()) {
//                return new PrimitiveTypeInfoImpl(clazz);
//            }
//            return new ClassInfoImpl(clazz);
//        }
//    };

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
            return new ArrayTypeInfoImpl(clazz);
        }
        if (clazz.isPrimitive()) {
            return new PrimitiveTypeInfoImpl(clazz);
        }
        return new ClassInfoImpl(clazz);
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

    private static TypeInfo getTypeFromTypeVariable(TypeVariable<?> typeVariable) {
        // 因为 没有 bindings 只能回退到上界
        return getTypeFromAny(typeVariable.getBounds()[0]);
    }

    private static TypeInfo getTypeFromWildcardType(WildcardType wildcardType) {
        // 回退到上界
        return getTypeFromAny(wildcardType.getUpperBounds()[0]);
    }

    private static TypeInfo getTypeFromWildcardType(WildcardType wildcardType, TypeBindings bindings) {
        // 回退到上界
        return getTypeFromAny(wildcardType.getUpperBounds()[0], bindings);
    }

    ///
    private static TypeInfo getTypeFromGenericArrayType(GenericArrayType type, TypeBindings bindings) {
        return new ArrayTypeInfoImpl(type, bindings);
    }


    private static TypeInfo getTypeFromTypeVariable(TypeVariable<?> typeVariable, TypeBindings bindings) {
        //尝试从从绑定中获取 否则回退到 上界
        var typeInfo = bindings.get(typeVariable);
        if (typeInfo != null) {
            return typeInfo;
        }
        return getTypeFromAny(typeVariable.getBounds()[0], bindings);
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
