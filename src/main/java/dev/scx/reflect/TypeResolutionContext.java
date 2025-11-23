package dev.scx.reflect;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/// 类型解析上下文, 用于支持 [TypeFactory] 构建复杂泛型结构时的类型变量绑定与递归检测.
///
/// 在 Java 的类型系统中, 理论上允许存在泛型自引用的情况, 例如:
/// ```
/// public class Node<T extends Node<T>>{}
///```
/// ```
/// Type type = Node.class.getTypeParameters()[0].getBounds()[0];
///```
/// 此时 `type` 的上界为 `Node<T>`, 即泛型自引用.
///
/// 如何避免递归泛型引用带来的无限循环问题？我们尝试了两种策略:
///
/// ### 方案 1: 允许自引用 (已放弃)
/// 在 `ClassInfoImpl` 构造函数中使用 `inProgressTypes` 提前缓存半成品对象.
/// 在 `_findBindings` 的递归解析路径中, 当检测递归时返回该半成品 ClassInfo, 从而构建出自引用结构.
/// 但这种方式只是将递归引用循环从 `ParameterizedType` 层转移到了 `ClassInfo` 中,
/// 并没有实质解决问题, 同时会导致 `hashCode`, `equals`, `toString` 等方法复杂甚至爆栈, 因此不推荐.
///
/// ### 方案 2: 退化为原始类型 (当前使用)
/// 仍通过 `inProgressTypes` 缓存构建中的对象, 但在检测到递归时,
/// 返回其原始类 (rawClass) 对应的不带泛型参数的 `TypeInfo`.
/// 该方式可彻底避免 ClassInfo 中的递归引用, 也方便后续逻辑的实现.
///
/// @author scx567888
/// @version 0.0.1
final class TypeResolutionContext {

    private final TypeBindings bindings;
    // 正在解析的半成品 ClassInfo, 用于解决递归问题
    private Map<Type, ClassInfo> inProgressTypes;

    TypeResolutionContext(TypeBindings bindings) {
        this.bindings = bindings;
    }

    public TypeBindings bindings() {
        return bindings;
    }

    // 这个方法实际上永远不可能在多个线程中被调用
    // 所以无需考虑线程安全 也不用加锁
    public Map<Type, ClassInfo> inProgressTypes() {
        if (inProgressTypes == null) {
            // 此处需要保证 equals 相等性 所以不能使用 IdentityHashMap
            // 不然的话会导致 复杂的递归泛型解析出现问题
            inProgressTypes = new HashMap<>();
        }
        return inProgressTypes;
    }

}
