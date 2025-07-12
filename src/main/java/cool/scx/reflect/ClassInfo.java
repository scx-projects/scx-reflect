//package cool.scx.reflect;
//
//import java.lang.reflect.AnnotatedElement;
//
///// ClassInfo
///// 表示所有 "类" 类型, 包括常规类, 接口, 枚举, 注解, 记录类 (record)。
///// 
///// 虽然从语义层面, 这些类型在 Java 中有严格区分, 例如 Interface 没有构造函数, Record 有组件, Enum 有枚举常量等等,
///// 理论上可以将它们拆分为多个更细的接口 (例如 InterfaceInfo, EnumInfo, RecordInfo 等), 这样可通过类型系统在编译期强制区分, 避免误用.
///// 
///// 但是, 当前抽象选择保留一个统一的 ClassInfo, 并通过 {@link ClassInfo} 枚举区分子类型，以换取以下优点:
///// 
/////   - 保持更统一、纯净的「Class」抽象视角，与 JVM 的 Class 表达形式一致
/////   - 接口层级更简单，结构更扁平
/////   - 减少对未来新增种类（如 Sealed Class, Value Class）的侵入性修改
///// 
///// 
///// 这种设计方案虽然导致部分方法在某些特定类型上「无意义」或「不可用」(例如 interface 没有构造函数)，但用户可以通过 [#classKind()] 或 [#isXXX()] 等方法判断，
///// 再安全使用对应逻辑。
///// 
///// ⚠️ 注意：如果你更关注使用时的类型安全（编译期强校验），可以考虑自行拆分出细粒度接口；但为了减少接口复杂度并保持理论统一性，当前版本暂不拆分。
/////
///// @author scx567888
///// @version 0.0.1
//public sealed interface ClassInfo extends TypeInfo, AccessModifierOwner, AnnotatedElementInfo permits ClassInfoImpl {
//
//    /// 泛型绑定
//    TypeBindings bindings();
//
//    //************ 类的基本信息 ***************
//
//    /// 类名
//    String name();
//
//    /// 类的类型
//    ClassKind classKind();
//
//    //************ 类属性 *****************
//
//    boolean isAbstract();
//
//    /// 是否 final 类
//    boolean isFinal();
//
//    /// 是否 静态类
//    boolean isStatic();
//
//    /// 是否 匿名类
//    boolean isAnonymousClass();
//
//    /// 是否 内部类
//    boolean isMemberClass();
//
//    //************* 继承结构 *****************
//
//    /// 父类 可能为空
//    ClassInfo superClass();
//
//    /// 接口列表
//    ClassInfo[] interfaces();
//
//    //************** 类成员 ********************
//
//    /// 构造参数列表
//    ConstructorInfo[] constructors();
//
//    /// 字段列表
//    FieldInfo[] fields();
//
//    /// 方法列表
//    MethodInfo[] methods();
//
//    //************* 辅助方法 **************
//
//    /// 获取类的所有父类 (广度遍历顺序)
//    ClassInfo[] allSuperClasses();
//
//    /// 获取类的所有接口 (广度遍历顺序)
//    ClassInfo[] allInterfaces();
//
//    /// 默认构造函数 (无参构造函数) 可能为空
//    ConstructorInfo defaultConstructor();
//
//    /// Record 规范构造参数 可能为空
//    ConstructorInfo recordConstructor();
//
//    /// 获取类所有字段 包括继承自父类的字段 (顺序 : 子类字段, 父类字段, 父类的父类字段... )
//    FieldInfo[] allFields();
//
//    /// 获取该类「最终可见」的所有方法, 包括继承自父类或接口的方法 (但不包含被覆盖的方法 或 被子类实现的抽象方法).
//    /// 等价于程序员在该类实例中最终能访问到的所有方法列表.
//    MethodInfo[] allMethods();
//
//    /// 枚举类型 (如果类是匿名枚举类的话可以正确获取到真正的枚举类型)
//    ClassInfo enumClass();
//
//    /// 返回指定类型的 父级 ClassInfo 支持常规类,抽象类,接口
//    default ClassInfo findSuperType(Class<?> rawTarget) {
//        if (rawTarget == this.rawClass()) {
//            return this;
//        }
//        // Check super interfaces first:
//        if (rawTarget.isInterface()) {
//            for (var anInterface : interfaces()) {
//                var type = anInterface.findSuperType(rawTarget);
//                if (type != null) {
//                    return type;
//                }
//            }
//        }
//        // and if not found, super class and its supertypes
//        if (superClass() != null) {
//            return superClass().findSuperType(rawTarget);
//        }
//        return null;
//    }
//
//    @Override
//    default AnnotatedElement annotatedElement() {
//        return rawClass();
//    }
//
//}
