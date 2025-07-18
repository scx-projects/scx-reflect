package cool.scx.reflect;

import java.lang.reflect.Type;
import java.util.IdentityHashMap;
import java.util.Map;

/// 解析上下文
///
/// 理论上 是可能存在 一个 ParameterizedType 的泛型参数 是自身的情况
/// 比如一个类定义如下:
/// `public class Node<T extends Node<T>> { }`
///
/// `Type type = Node.class.getTypeParameters()[0].getBounds()[0];`
///
/// 这时 `type` 的 泛型参数实际上就是 自引用的
/// 
/// 如何解决递归泛型引用 ?
///
/// 1. 自引用解析为自引用 (不推荐)
///
///    我们可以通过 在 ClassInfoImpl 构造函数中 提前缓存半成品 也就是 利用 inProgressTypes 的方式来绕过.
///    在 {@link ReflectSupport#_findBindings} → {@link TypeFactory#getTypeFromAny} 的解析流程中, 会递归触发对类型变量的绑定解析.
///    在 {@link TypeFactory#getTypeFromTypeVariable} 和 {@link TypeFactory#getTypeFromWildcardType} 分支中
///    我们可以利用 inProgressTypes 中已存在的半成品对象 直接返回正在解析中的 classInfo, 以此成功构建出一个 带有递归泛型引用的 classInfo.
///    不过这种方式虽然能完成对象的构建, 但本质上只是将递归从 ParameterizedType 转移到了 ClassInfo,
///    本质并上没有解决循环引用的问题, 同时会导致 classInfo 的 hashCode, equals, toString 难以实现甚至栈溢出.
///    所以这种方案放弃 !!!
/// 
/// 2. 自引用解析为无泛型版本 (当前做法)
///    基本流程和方案 1 基本相同, 仍使用 inProgressTypes 缓存构建中对象, 但在检测到递归引用时.
///    不返回正在解析中的 classInfo, 而是返回原始类 (rawClass) 对应的 TypeInfo.
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
