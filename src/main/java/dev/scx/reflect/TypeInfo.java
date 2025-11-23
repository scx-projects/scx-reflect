package dev.scx.reflect;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

/// 在一个支持泛型的语言 (例如 Java) 中, 如果仅从理论 (语义) 层面来看,
/// 一个类型 (Type) 可以被视为「类型构造器」 (Type Constructor) 与「类型参数」 (Type Arguments) 的组合 :
///
///    `Type = Type Constructor + Type Arguments`
///
/// 不过, 因为 Java 的泛型在实现层面采用了类型擦除 (Type Erasure), 所以我们无法完全按照此理论模型来表达 Java 中的类型系统.
/// 同时, 理论上, 类型变量 (TypeVariable) 和通配符 (WildcardType) 并不是严格意义上的「类型」，
/// 它们仅仅是 Java 泛型实现中为了描述占位或约束而引入的语法结构,
/// 在一个完整, 可推导的类型系统中应被 `消解为具体类型` 或 `通过上下文推导` 得到, 不应长期存在.
/// 为了构建一个更纯粹, 符合类型理论的类型体系, 本抽象不再包含 TypeVariable 和 WildcardType,
/// 只保留如下三种核心子类型:
///
/// 1. {@link ClassInfo} (具象类型)
///
///    表示一个已具体化的, 完整的具体类型, 它更接近理论层面中定义的「Type」.
///    例如: `String`, `List<String>`, `Map<Integer, String>` 等.
///    在当前环境中 可以看作 「Class + 类型参数」.
///
/// 2. {@link PrimitiveTypeInfo} (基本类型)
///
///    表示 Java 中的基本数据类型, 例如 `int`, `long`, `boolean` 等.
///    由于它们在 Java 中的特殊性 (不是引用类型), 我们将其单独抽象出来.
///
/// 3. {@link ArrayTypeInfo} (数组类型)
///
///    表示 数组类型, 例如 `String[]` , `int[]` 或 `T[](泛型数组)` 等.
///    因为数组在 Java 中是协变的, 且运行时保留元素类型信息 (不同于泛型的类型擦除), 我们将其单独抽象出来.
///
///  简要对照 Java 反射中的 Type 实现 :
///
/// {@link Class} / {@link ParameterizedType}  -->  {@link ClassInfo}
///
/// {@link Class} (isPrimitive = true)  -->  {@link PrimitiveTypeInfo}
///
/// {@link GenericArrayType} / {@link Class} (isArray = true)  -->  {@link ArrayTypeInfo}
///
/// {@link TypeVariable}  -->  尝试推导或回退到上界 --> {@link TypeInfo}
///
/// {@link WildcardType}  -->  回退到上界 --> {@link TypeInfo}
///
/// @author scx567888
/// @version 0.0.1
public sealed interface TypeInfo permits ArrayTypeInfo, ClassInfo, PrimitiveTypeInfo {

    /// 获取原始类
    Class<?> rawClass();

}
