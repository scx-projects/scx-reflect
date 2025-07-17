package cool.scx.reflect;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;

import static cool.scx.reflect.TypeBindingsImpl.EMPTY_BINDINGS;

/// 非线程安全
public final class ScxReflect {

    // Key 可能是 Class, ParameterizedType, GenericArrayType, ArrayTypeInfo, ClassInfo
    private static final Map<Object, TypeInfo> TYPE_CACHE = new HashMap<>();

    // 仅做分发
    static TypeInfo getTypeFromAny(Type type, TypeBindings contextBindings) {
        return switch (type) {
            case Class<?> c -> getTypeFromClass(c);
            case ParameterizedType p -> getTypeFromParameterizedType(p, contextBindings);
            case GenericArrayType g -> getTypeFromGenericArrayType(g, contextBindings);
            case TypeVariable<?> t -> getTypeFromTypeVariable(t, contextBindings);
            case WildcardType w -> getTypeFromWildcardType(w, contextBindings);
            default -> throw new IllegalArgumentException("Unsupported type: " + type);
        };
    }

    // Class 永远不存在 bindings
    static TypeInfo getTypeFromClass(Class<?> clazz) {
        // 直接使用原始 Class 作为 Key, 后续可以直接通过 Class 进行查找.
        // 减少重复构建 key 的成本
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

    private static TypeInfo getTypeFromParameterizedType(ParameterizedType parameterizedType, TypeBindings contextBindings) {
        // 如果上下文 bindings 为 空, 直接使用原始 ParameterizedType 作为 Key, 后续可以直接通过 ParameterizedType 进行查找.
        // 在 bindings 为空的情况下 一般我们认为 ParameterizedType 就已经携带了足够的 泛型信息 (即使是或者 TypeVariable, WildcardType)
        // 也会因为没有 上下文 bindings 而退化为上界, 也就是说 我们可以认为 所有 没有上下文 bindings 的 ParameterizedType 
        // 最终的类型 永远是唯一的, 所以 ParameterizedType 作为 Key 是安全的
        if (contextBindings == EMPTY_BINDINGS) {
            // 使用原始 ParameterizedType 作为 Key
            var t = TYPE_CACHE.get(parameterizedType);
            if (t != null) {
                return t;
            }
            var classInfo = new ClassInfoImpl(parameterizedType, EMPTY_BINDINGS);
            TYPE_CACHE.put(parameterizedType, classInfo);
            return classInfo;
        }
        // 这里有 上下文 bindings , 不能使用简单的 ParameterizedType 来表示 , 因为 替换后的 真实类型 并不确定
        // 我们直接使用一个 ClassInfoImpl 来实现现有类型的查找, 并同时使用这个作为后续存储的 key
        // 注意 ClassInfoImpl 的创建实际上是轻量的, 而且携带了真正完整的 bindings
        // 为了实现严格的 "同一个类型永远可以只获得同一个 TypeInfo",
        // 实际上当代码走到这里的时候 只可能是 正在初始化 ClassInfoImpl 内部的 诸如 FieldInfo, MethodInfo 等.
        // 而这些对象 实际上是会被 ClassInfoImpl 缓存起来的, 这意味着 以下的代码实际上 并不会执行很多次.
        // 性能不至于成为问题
        var classInfo = new ClassInfoImpl(parameterizedType, contextBindings);
        var t = TYPE_CACHE.get(classInfo);
        if (t != null) {
            return t;
        }
        TYPE_CACHE.put(classInfo, classInfo);
        // 这里我们无需像 构建 ArrayTypeInfoImpl 那样尝试优化缓存
        // 因为 任意一个类 只有没有泛型 就永远不可能是 ParameterizedType, 根本不会走到这段代码
        return classInfo;
    }

    private static TypeInfo getTypeFromGenericArrayType(GenericArrayType genericArrayType, TypeBindings contextBindings) {
        //如果上下文 bindings 为 空, 消解为 无上下文 bindings 的版本
        if (contextBindings == EMPTY_BINDINGS) {
            // 使用原始 GenericArrayType 作为 Key, 这里是安全的, 因为我们没有上下文 bindings 也就不会发生替换
            var t = TYPE_CACHE.get(genericArrayType);
            if (t != null) {
                return t;
            }
            var arrayTypeInfo = new ArrayTypeInfoImpl(genericArrayType, EMPTY_BINDINGS);
            // 这里尝试复用或提前缓存, 只有组件类型是没有任何泛型的情况下 我们才可能复用
            return tryOptimizeCache(arrayTypeInfo, genericArrayType);
        }
        // 我们直接使用一个 ArrayTypeInfoImpl 来实现现有类型的查找, 并同时使用这个作为后续存储的 key
        // 注意 ArrayTypeInfoImpl 的创建实际上是轻量的, 而且携带了真正完整的 bindings
        // 为了实现严格的 "同一个类型永远可以只获得同一个 TypeInfo",
        // 实际上当代码走到这里的时候 只可能是 正在初始化 ClassInfoImpl 内部的 诸如 FieldInfo, MethodInfo 等.
        // 而这些对象 实际上是会被 ClassInfoImpl 缓存起来的, 这意味着 以下的代码实际上 并不会执行很多次.
        // 性能不至于成为问题
        var arrayTypeInfo = new ArrayTypeInfoImpl(genericArrayType, contextBindings);
        var typeInfo = TYPE_CACHE.get(arrayTypeInfo);
        if (typeInfo != null) {
            return typeInfo;
        }
        // 这里尝试复用或提前缓存, 只有组件类型是没有任何泛型的情况下 我们才可能复用
        return tryOptimizeCache(arrayTypeInfo, arrayTypeInfo);
    }

    private static boolean canReuseRawClass(ArrayTypeInfo arrayTypeInfo) {
        var componentType = arrayTypeInfo.componentType();
        // 基本类型必不存在泛型
        if (componentType instanceof PrimitiveTypeInfo) {
            return true;
        }
        // 普通类是否存在泛型 
        if (componentType instanceof ClassInfo classInfo) {
            return classInfo.bindings() == EMPTY_BINDINGS;
        }
        // 多维数组需要递归判断
        if (componentType instanceof ArrayTypeInfo innerArray) {
            return canReuseRawClass(innerArray);
        }
        return false;
    }

    private static TypeInfo tryOptimizeCache(ArrayTypeInfoImpl arrayTypeInfo, Object typeKey) {
        var canOptimize = canReuseRawClass(arrayTypeInfo);
        if (canOptimize) {
            var oldTypeInfo = TYPE_CACHE.get(arrayTypeInfo.rawClass());
            // 没有我们缓存两份, 一份 Class 的, 一份 GenericArrayType 的
            if (oldTypeInfo == null) {
                TYPE_CACHE.put(typeKey, arrayTypeInfo);
                TYPE_CACHE.put(arrayTypeInfo.rawClass(), arrayTypeInfo);
                return arrayTypeInfo;
            } else {
                //如果有了 当前的 arrayTypeInfo 就没意义了 直接替换为旧的
                TYPE_CACHE.put(typeKey, oldTypeInfo);
                return oldTypeInfo;
            }
        } else {
            //没有优化的可能 
            TYPE_CACHE.put(typeKey, arrayTypeInfo);
            return arrayTypeInfo;
        }
    }

    private static TypeInfo getTypeFromTypeVariable(TypeVariable<?> typeVariable, TypeBindings contextBindings) {
        //尝试从从绑定中获取 否则回退到 上界
        var typeInfo = contextBindings.get(typeVariable);
        if (typeInfo != null) {
            return typeInfo;
        }
        return getTypeFromAny(typeVariable.getBounds()[0], contextBindings);
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
        return getTypeFromAny(typeReference.getType(), EMPTY_BINDINGS);
    }

}
