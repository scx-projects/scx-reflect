package dev.scx.reflect;

import java.lang.reflect.AnnotatedElement;

import static dev.scx.reflect.ClassKind.ENUM;

/// ClassInfo
///
/// @author scx567888
/// @version 0.0.1
public sealed interface ClassInfo extends TypeInfo, AccessModifierOwner, AnnotatedElementInfo permits ClassInfoImpl {

    TypeBindings bindings();

    ClassInfo declaringClass();

    // ************ 类的基本信息 ***************

    String name();

    ClassKind classKind();

    // ************ 类属性 *****************

    boolean isStatic();

    boolean isFinal();

    boolean isAbstract();

    boolean isAnonymousClass();

    boolean isMemberClass();

    boolean isLocalClass();

    // ************* 继承结构 *****************

    ClassInfo superClass();

    ClassInfo[] interfaces();

    // ************** 类成员 ********************

    ConstructorInfo[] constructors();

    FieldInfo[] fields();

    MethodInfo[] methods();

    RecordComponentInfo[] recordComponents();

    // ************* 辅助方法 **************

    /// 类在声明链中可见的全部类型绑定.
    TypeBindings allBindings();

    /// 类的所有父类.
    ClassInfo[] allSuperClasses();

    /// 类的所有接口 (广度遍历顺序).
    ClassInfo[] allInterfaces();

    /// 类及其继承体系中声明的全部字段.
    FieldInfo[] allFields();

    /// 类及其继承体系中未被 Override 的全部方法.
    MethodInfo[] allMethods();

    /// 无参 构造函数, 可能为空.
    ConstructorInfo defaultConstructor();

    /// Record 规范构造函数, 可能为空.
    ConstructorInfo recordConstructor();

    /// 当前类型对应的枚举类型. 如果当前类型不是枚举, 则返回 null.
    default ClassInfo enumClass() {
        if (classKind() != ENUM) {
            return null;
        }
        return isAnonymousClass() ? superClass() : this;
    }

    /// 在当前类型的继承体系中查找指定 raw type 对应的 ClassInfo.
    default ClassInfo findSuperType(Class<?> rawTarget) {
        if (rawTarget == rawClass()) {
            return this;
        }

        if (rawTarget.isInterface()) {
            for (var i : allInterfaces()) {
                if (i.rawClass() == rawTarget) {
                    return i;
                }
            }
            return null;
        }

        for (var c : allSuperClasses()) {
            if (c.rawClass() == rawTarget) {
                return c;
            }
        }
        return null;
    }

    @Override
    default boolean isRaw() {
        return allBindings().isEmpty();
    }

    @Override
    default AnnotatedElement annotatedElement() {
        return rawClass();
    }

}
