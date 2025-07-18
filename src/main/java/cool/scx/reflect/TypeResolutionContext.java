package cool.scx.reflect;

import java.lang.reflect.Type;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * 类型解析上下文，用于支持 {@link TypeFactory} 构建复杂泛型结构时的类型变量绑定与递归检测。
 *
 * <p>在 Java 的类型系统中，理论上存在泛型自引用的情况，例如：
 *
 * <pre>{@code
 * public class Node<T extends Node<T>> {}
 * Type type = Node.class.getTypeParameters()[0].getBounds()[0];
 * }</pre>
 *
 * 此时 {@code type} 的上界为 {@code Node<T>}，即自引用。
 *
 * <p>如何避免递归泛型引用带来的无限循环问题？我们尝试了两种策略：
 *
 * <h3>方案 1：允许自引用（已放弃）</h3>
 * 在 {@link ClassInfoImpl} 构造函数中使用 {@code inProgressTypes} 提前缓存半成品对象。
 * 在 {@link ReflectSupport#_findBindings} → {@link TypeFactory#getTypeFromAny} → {@link TypeFactory#getTypeFromTypeVariable} 或 {@link TypeFactory#getTypeFromWildcardType}
 * 等递归路径中返回该半成品 ClassInfo，从而构建出自引用结构。
 *
 * 但这种方式只是将循环从 {@link java.lang.reflect.ParameterizedType} 层转移到了 {@link ClassInfo} 中，
 * 并没有实质解决问题，会导致 {@code hashCode}、{@code equals}、{@code toString} 等方法复杂甚至爆栈，因此不推荐。
 *
 * <h3>方案 2：退化为原始类型（当前使用）</h3>
 * 仍通过 {@code inProgressTypes} 缓存构建中的对象，但在检测到递归时，
 * 返回其原始类（rawClass）对应的不带泛型参数的 {@code TypeInfo}。
 * 该方式可彻底避免 ClassInfo 中的循环结构，从而保证后续逻辑的稳定与简洁实现。
 */
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
