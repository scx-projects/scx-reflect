package cool.scx.reflect;

import java.lang.reflect.Type;
import java.util.IdentityHashMap;
import java.util.Map;

/// 解析上下文
///
/// 理论上 是可能存在 一个 ParameterizedType 的泛型参数 是自身的情况
/// 比如一个类定义如下:
/// public class Node<T extends Node<T>> {
///
/// }
///
/// Type type = Node.class.getTypeParameters()[0].getBounds()[0];
///
/// 这时 type 的 泛型参数实际上就是 自引用的
/// 如何解决递归泛型引用 ?
///
/// 1. 自引用解析为自引用 (不推荐)
///
///    我们可以通过 在 ClassInfoImpl 构造函数中 提前缓存半成品 也就是 利用 inProgressTypes 的方式来绕过.
///    当执行到 {@link ReflectSupport#_findBindings} 的时候, 内部会递归调用 {@link TypeFactory#getTypeFromAny}
///    在 {@link TypeFactory#getTypeFromTypeVariable} 和 {@link TypeFactory#getTypeFromWildcardType} 分支中
///    我们可以利用 inProgressTypes 中已存在的半成品对象 直接返回 classInfo, 以此成功构建出一个 带有递归泛型引用的 classInfo.
///    不过这样的弊端在于, 我们虽然解决了对象创建的自引用问题
///    但我们只是把递归泛型引用的问题从 ParameterizedType 中转移到了 classInfo 中, 本质上没有解决任何问题
///    而且这会导致 classInfo 的 hashCode, equals 以及 toString 方法很难处理甚至无法处理
///    所以这种方案放弃 !!!
/// 
/// 2. 自引用解析为无泛型版本 (当前做法)
///    基本流程和方案 1 基本相同, 依旧是需要在 ClassInfoImpl 构造函数中 提前缓存半成品,
///    但在 {@link TypeFactory#getTypeFromTypeVariable} 和 {@link TypeFactory#getTypeFromWildcardType} 分支中
///    我们不直接返回 classInfo, 而是返回一个 rawClass, 也就是没有泛型的版本, 以便彻底消解 泛型递归引用
///    同时这种方案中实际上 classInfo 中是根本不存在自引用的, 所以  hashCode, equals 以及 toString 方法可以按照正常逻辑编写
///
public final class TypeResolutionContext {

    private final TypeBindings bindings;
    // 正在解析的半成品 ClassInfo, 用于解决递归问题
    private Map<Type, ClassInfo> inProgressTypes;

    public TypeResolutionContext(TypeBindings bindings) {
        this.bindings = bindings;
    }

    public TypeBindings bindings() {
        return bindings;
    }

    public Map<Type, ClassInfo> inProgressTypes() {
        if (inProgressTypes == null) {
            inProgressTypes = new IdentityHashMap<>();
        }
        return inProgressTypes;
    }

}
