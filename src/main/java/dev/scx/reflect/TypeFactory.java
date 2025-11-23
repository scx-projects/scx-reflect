package dev.scx.reflect;

import java.lang.reflect.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static dev.scx.reflect.TypeBindingsImpl.EMPTY_BINDINGS;

/// TypeFactory (线程安全)
///
/// @author scx567888
/// @version 0.0.1
final class TypeFactory {

    // Key 可能是 Class, ParameterizedType, GenericArrayType, ArrayTypeInfo, ClassInfo
    // 其中 ParameterizedType 和 GenericArrayType 是存在 最终推导类型一致但是 本身的 equals 却不一致的情况
    // 举例 某两个 GenericArrayTypeImpl 的 genericComponentType 都是 TypeVariableImpl 类型,
    // 同时这两个 TypeVariableImpl 的 bounds 是相同的, 但是 genericDeclaration 却不同.
    // 这就导致 即使两个 TypeVariableImpl 的最终推导结果一致, 但是二者的 equals 判断为 false (当然这在 TypeVariableImpl 的视角来看是合理的).
    // 这会间接影响外层 GenericArrayTypeImpl 的 等价性判断.
    // 但是这其实无所谓 因为 ParameterizedType 和 GenericArrayType 本质上在此处只是用来加速 查找.
    // 即使没有命中缓存 也会进行推导后类型查找, 不存在重复创建多个本质上完全一致的 TypeInfo 的风险
    public static final Map<Object, TypeInfo> TYPE_CACHE = new ConcurrentHashMap<>();

    // 写入锁
    private static final Lock LOCK = new ReentrantLock();

    // 仅做分发
    public static TypeInfo typeOfAny(Type type, TypeResolutionContext context) {
        return switch (type) {
            case Class<?> c -> typeOfClass(c);
            case ParameterizedType p -> typeOfParameterizedType(p, context);
            case GenericArrayType g -> typeOfGenericArrayType(g, context);
            case TypeVariable<?> t -> typeOfTypeVariable(t, context);
            case WildcardType w -> typeOfWildcardType(w, context);
            default -> throw new IllegalArgumentException("Unsupported type: " + type);
        };
    }

    // Class 永远不存在 bindings
    public static TypeInfo typeOfClass(Class<?> clazz) {
        // 使用原始 Class 作为 key, 后续可以直接通过 Class 进行查找,
        // 这种类型不会携带任何泛型上下文, 天然是可重用的.
        // 因此可以安全地作为缓存 key, 且便于后续快速查找, 避免重复构造.
        // 此处我们并不冗余缓存 ClassInfoImpl, ArrayTypeInfoImpl 之类,
        // 因为在 typeOfParameterizedType 或 typeOfGenericArrayType 中会冗余缓存
        // 我们只要在一个构建路径中冗余缓存就够了
        // 先尝试快速无锁读
        var result = TYPE_CACHE.get(clazz);
        if (result != null) {
            return result;
        }
        LOCK.lock();
        try {
            // 双重检查
            result = TYPE_CACHE.get(clazz);
            if (result != null) {
                return result;
            }
            // 创建新的实例
            if (clazz.isArray()) {
                result = new ArrayTypeInfoImpl(clazz);
            } else if (clazz.isPrimitive()) {
                result = new PrimitiveTypeInfoImpl(clazz);
            } else {
                result = new ClassInfoImpl(clazz);
            }
            TYPE_CACHE.put(clazz, result);
            return result;
        } finally {
            LOCK.unlock();
        }
    }

    public static TypeInfo typeOfParameterizedType(ParameterizedType parameterizedType, TypeResolutionContext context) {
        // 如果上下文 bindings 为空, 则可直接使用原始 ParameterizedType 作为 key.
        // 这是安全的, 因为即使其中包含 TypeVariable 或 WildcardType, 也会因 bindings 为空而退化为其上界, 结果是确定的.
        // 因此, 在无上下文 bindings 的场景下, 同一个 ParameterizedType 实例总是可以映射到同一个 TypeInfo.
        // 此处直接使用 ParameterizedType 作为缓存 key 是安全有效的 并且简化了缓存结构.
        if (context.bindings() == EMPTY_BINDINGS) {
            // 使用原始 ParameterizedType 作为 Key
            // 快速无锁读
            var result = TYPE_CACHE.get(parameterizedType);
            if (result != null) {
                return result;
            }
            LOCK.lock();
            try {
                // 双重检查
                result = TYPE_CACHE.get(parameterizedType);
                if (result != null) {
                    return result;
                }
                var classInfo = new ClassInfoImpl(parameterizedType, context);
                // 检测有可能已经有对应的 ClassInfo
                var oldTypeInfo = TYPE_CACHE.get(classInfo);
                if (oldTypeInfo != null) {
                    // 如果有了 当前的 classInfo 就没意义了 直接替换为旧的
                    TYPE_CACHE.put(parameterizedType, oldTypeInfo);
                    return oldTypeInfo;
                }
                // 没有我们缓存两份, 一份 ParameterizedType 的, 一份 ClassInfo 的
                TYPE_CACHE.put(parameterizedType, classInfo);
                TYPE_CACHE.put(classInfo, classInfo);
                return classInfo;
            } finally {
                LOCK.unlock();
            }
        }
        // 当存在上下文 bindings 时, ParameterizedType 中可能包含被替换的 TypeVariable, 因此不能直接使用 ParameterizedType 作为 key.
        // 为了实现严格的 "同一个类型 永远只对应同一个 TypeInfo",
        // 我们使用包含上下文的 ClassInfoImpl 作为 key. 它携带了真正完整的 bindings, 同时正确的实现了 equals 和 hashCode.
        // 虽然构建 ClassInfoImpl 看似重复, 但它创建是轻量的, 并且后续可以作为 cache key 和最终值双重使用, 避免多次构建.
        // 而且 实际上当代码走到这里的时候 只可能是 正在初始化 ClassInfoImpl 内部的对象, 诸如 FieldInfo, MethodInfo 等.
        // 而这些对象 实际上是会被 ClassInfoImpl 内部缓存起来的, 这意味着 以下的代码实际上 并不会执行很多次, 性能不至于成为问题.
        var classInfo = new ClassInfoImpl(parameterizedType, context);
        // 快速无锁读
        var result = TYPE_CACHE.get(classInfo);
        if (result != null) {
            return result;
        }
        LOCK.lock();
        try {
            //双重检查
            result = TYPE_CACHE.get(classInfo);
            if (result != null) {
                return result;
            }
            TYPE_CACHE.put(classInfo, classInfo);
            // 这里我们无需像 构建 ArrayTypeInfoImpl 那样尝试优化缓存
            // 因为 任意一个类 只有没有泛型 就永远不可能是 ParameterizedType, 根本不会走到这段代码
            return classInfo;
        } finally {
            LOCK.unlock();
        }
    }

    public static TypeInfo typeOfGenericArrayType(GenericArrayType genericArrayType, TypeResolutionContext context) {
        // 如果上下文 bindings 为空, 则可直接使用原始 GenericArrayType 作为 key.
        // 这是安全的, 因为即使其中包含 TypeVariable 或 WildcardType, 也会因 bindings 为空而退化为其上界, 结果是确定的.
        // 因此, 在无上下文 bindings 的场景下, 同一个 GenericArrayType 实例总是可以映射到同一个 TypeInfo.
        // 此处直接使用 GenericArrayType 作为缓存 key 是安全有效的 并且简化了缓存结构.
        if (context.bindings() == EMPTY_BINDINGS) {
            // 快速 get
            var result = TYPE_CACHE.get(genericArrayType);
            if (result != null) {
                return result;
            }
            LOCK.lock();
            try {
                // 双重检查
                result = TYPE_CACHE.get(genericArrayType);
                if (result != null) {
                    return result;
                }

                var arrayTypeInfo = new ArrayTypeInfoImpl(genericArrayType, context);
                // 检测有可能已经有对应的 ArrayTypeInfo
                var oldArrayTypeInfo = TYPE_CACHE.get(arrayTypeInfo);
                if (oldArrayTypeInfo != null) {
                    // 如果有了 当前的 classInfo 就没意义了 直接替换为旧的
                    // 这里无需 优化 rawClass 因为在构建 oldArrayTypeInfo 的过程中必定已经优化过了
                    TYPE_CACHE.put(genericArrayType, oldArrayTypeInfo);
                    return oldArrayTypeInfo;
                }
                //判断能否优化
                var canOptimize = canReuseRawClass(arrayTypeInfo);
                //没有优化的可能 缓存两份
                if (!canOptimize) {
                    TYPE_CACHE.put(genericArrayType, arrayTypeInfo);
                    TYPE_CACHE.put(arrayTypeInfo, arrayTypeInfo);
                    return arrayTypeInfo;
                }
                //这里继续尝试使用 rawClass 查找
                oldArrayTypeInfo = TYPE_CACHE.get(arrayTypeInfo.rawClass());
                if (oldArrayTypeInfo != null) {
                    //如果有了 当前的 arrayTypeInfo 就没意义了 直接替换为旧的, 这里缓存两份
                    TYPE_CACHE.put(genericArrayType, oldArrayTypeInfo);
                    TYPE_CACHE.put(arrayTypeInfo, oldArrayTypeInfo);
                    return oldArrayTypeInfo;
                }
                // 全部没有我们缓存三份, 一份 Class 的, 一份 GenericArrayType 的, 一份 ArrayTypeInfoImpl
                TYPE_CACHE.put(genericArrayType, arrayTypeInfo);
                TYPE_CACHE.put(arrayTypeInfo, arrayTypeInfo);
                TYPE_CACHE.put(arrayTypeInfo.rawClass(), arrayTypeInfo);
                return arrayTypeInfo;
            } finally {
                LOCK.unlock();
            }
        }
        // 为了实现严格的 "同一个类型 永远只对应同一个 TypeInfo", 我们使用包含上下文的 ArrayTypeInfoImpl 作为 key.
        // 它携带了真正完整的 bindings, 同时正确的实现了 equals 和 hashCode. (只比较 componentType)
        // 虽然构建 ArrayTypeInfoImpl 看似重复, 但它创建是轻量的, 并且后续可以作为 cache key 和最终值双重使用, 避免多次构建.
        // 而且 实际上当代码走到这里的时候 只可能是 正在初始化 ClassInfoImpl 内部的对象, 诸如 FieldInfo, MethodInfo 等.
        // 而这些对象 实际上是会被 ClassInfoImpl 内部缓存起来的, 这意味着 以下的代码实际上 并不会执行很多次, 性能不至于成为问题.
        var arrayTypeInfo = new ArrayTypeInfoImpl(genericArrayType, context);
        // 快速 get
        var result = TYPE_CACHE.get(arrayTypeInfo);
        if (result != null) {
            return result;
        }
        LOCK.lock();
        try {
            // 双重检查
            result = TYPE_CACHE.get(arrayTypeInfo);
            if (result != null) {
                return result;
            }

            // 这里尝试复用或提前缓存, 只有组件类型是没有任何泛型的情况下 我们才可能复用
            // 如果可以优化, 我们会将 arrayTypeInfo 同时缓存为 typeKey 和 rawClass 两份.
            // 如果 rawClass 已经存在缓存, 说明此前已有等价类型被缓存, 我们直接复用旧的.
            // 否则, 将当前类型写入两个 key.
            // 如果无法优化, 则仅以 typeKey 进行缓存.
            // 这个优化不单单是 为了性能, 同时也保证了 同一个类型拿到的 TypeInfo 永远是一致的,
            // 无论是先通过 Class 创建, 还是先通过 GenericArrayType 创建, 最终的 TypeInfo 是一致的.
            var canOptimize = canReuseRawClass(arrayTypeInfo);
            if (!canOptimize) {
                //没有优化的可能
                TYPE_CACHE.put(arrayTypeInfo, arrayTypeInfo);
                return arrayTypeInfo;
            }
            var oldArrayTypeInfo = TYPE_CACHE.get(arrayTypeInfo.rawClass());
            if (oldArrayTypeInfo != null) {
                //如果有了 当前的 arrayTypeInfo 就没意义了 直接替换为旧的
                TYPE_CACHE.put(arrayTypeInfo, oldArrayTypeInfo);
                return oldArrayTypeInfo;
            }
            // 没有我们缓存两份, 一份 Class 的, 一份 GenericArrayType 的
            TYPE_CACHE.put(arrayTypeInfo, arrayTypeInfo);
            TYPE_CACHE.put(arrayTypeInfo.rawClass(), arrayTypeInfo);
            return arrayTypeInfo;

        } finally {
            LOCK.unlock();
        }

    }

    public static TypeInfo typeOfTypeVariable(TypeVariable<?> typeVariable, TypeResolutionContext context) {
        // 尝试从上下文 bindings 获取已绑定的类型变量类型, 若无法获取，则使用其上界 (第一个 bound) 进行退化处理.
        // 这种退化是合理且安全的, 因为在 Java 泛型系统中也是这么退化的.
        var typeInfo = context.bindings().get(typeVariable);
        if (typeInfo != null) {
            return typeInfo;
        }
        var bound = typeVariable.getBounds()[0];
        // 这里我们检测是否发生了递归泛型引用
        var classInfo = context.inProgressTypes().get(bound);
        if (classInfo != null) {
            // 这里我们 不直接返回 classInfo, 因为这样实际上并不会解决递归泛型引用的问题
            // 我们只是把递归泛型引用的问题从 ParameterizedType 中转移到了 classInfo 中,
            // 本质上没有解决任何问题, 所以此处返回 rawClass, 也就是没有泛型的版本, 以便彻底消解 泛型递归引用
            return typeOfClass(classInfo.rawClass());
        }
        return typeOfAny(bound, context);
    }

    public static TypeInfo typeOfWildcardType(WildcardType wildcardType, TypeResolutionContext context) {
        // 通配符类型理论上 还具有下界, 但是我们在反射系统中通常只想知道 "这个类型到底能存储什么".
        // 所以此处忽略下界, 直接退化为上界.
        var bound = wildcardType.getUpperBounds()[0];
        // 处理可能发生的递归泛型引用
        var classInfo = context.inProgressTypes().get(bound);
        // 这里我们检测是否发生了递归泛型引用
        if (classInfo != null) {
            // 这里我们 不直接返回 classInfo, 因为这样实际上并不会解决递归泛型引用的问题
            // 我们只是把递归泛型引用的问题从 ParameterizedType 中转移到了 classInfo 中,
            // 本质上没有解决任何问题, 所以此处返回 rawClass, 也就是没有泛型的版本, 以便彻底消解 泛型递归引用
            return typeOfClass(classInfo.rawClass());
        }
        return typeOfAny(bound, context);
    }

    public static boolean canReuseRawClass(ArrayTypeInfo arrayTypeInfo) {
        var componentType = arrayTypeInfo.componentType();
        // 基本类型必不存在泛型
        return switch (componentType) {
            case PrimitiveTypeInfo _ -> true;
            // 普通类是否存在泛型
            case ClassInfo classInfo -> classInfo.bindings() == EMPTY_BINDINGS;
            // 多维数组需要递归判断
            case ArrayTypeInfo innerArray -> canReuseRawClass(innerArray);
        };
    }

}
