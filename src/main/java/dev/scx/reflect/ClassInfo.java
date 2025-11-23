package dev.scx.reflect;

import java.lang.reflect.AnnotatedElement;

/// ClassInfo
///
/// @author scx567888
/// @version 0.0.1
public sealed interface ClassInfo extends TypeInfo, AccessModifierOwner, AnnotatedElementInfo permits ClassInfoImpl {

    /// 泛型绑定
    TypeBindings bindings();

    //************ 类的基本信息 ***************

    /// 类名
    String name();

    /// 类的类型
    ClassKind classKind();

    //************ 类属性 *****************

    boolean isAbstract();

    /// 是否 final 类
    boolean isFinal();

    /// 是否 静态类
    boolean isStatic();

    /// 是否 匿名类
    boolean isAnonymousClass();

    /// 是否 内部类
    boolean isMemberClass();

    //************* 继承结构 *****************

    /// 父类 可能为空
    ClassInfo superClass();

    /// 接口列表
    ClassInfo[] interfaces();

    //************** 类成员 ********************

    /// 构造参数列表
    ConstructorInfo[] constructors();

    /// 字段列表
    FieldInfo[] fields();

    /// 方法列表
    MethodInfo[] methods();

    //************* 辅助方法 **************

    /// 获取类的所有父类 (广度遍历顺序)
    ClassInfo[] allSuperClasses();

    /// 获取类的所有接口 (广度遍历顺序)
    ClassInfo[] allInterfaces();

    /// 默认构造函数 (无参构造函数) 可能为空
    ConstructorInfo defaultConstructor();

    /// Record 规范构造参数 可能为空
    ConstructorInfo recordConstructor();

    /// 获取类所有字段 包括继承自父类的字段 (顺序 : 子类字段, 父类字段, 父类的父类字段... )
    FieldInfo[] allFields();

    /// 获取该类「最终可见」的所有方法, 包括继承自父类或接口的方法 (但不包含被覆盖的方法 或 被子类实现的抽象方法).
    /// 等价于程序员在该类实例中最终能访问到的所有方法列表.
    MethodInfo[] allMethods();

    /// 枚举类型 (如果类是枚举的 匿名子类 的话, 可以正确获取到真正的枚举类型)
    ClassInfo enumClass();

    /// Record 组件列表 (如果是 Record 类型)
    RecordComponentInfo[] recordComponents();

    /// 返回指定类型的 父级 ClassInfo 支持常规类,抽象类,接口
    default ClassInfo findSuperType(Class<?> rawTarget) {
        if (rawTarget == this.rawClass()) {
            return this;
        }
        // Check super interfaces first:
        if (rawTarget.isInterface()) {
            for (var anInterface : interfaces()) {
                var type = anInterface.findSuperType(rawTarget);
                if (type != null) {
                    return type;
                }
            }
        }
        // and if not found, super class and its supertypes
        if (superClass() != null) {
            return superClass().findSuperType(rawTarget);
        }
        return null;
    }

    @Override
    default AnnotatedElement annotatedElement() {
        return rawClass();
    }

}
