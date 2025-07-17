package cool.scx.reflect;

import java.lang.reflect.ParameterizedType;
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
/// 此处我们 通过 在 ClassInfoImpl 构造函数中 提前缓存半成品 的方式来部分绕过.
final class TypeResolutionContext {

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
