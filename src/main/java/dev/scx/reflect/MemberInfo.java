package dev.scx.reflect;

/// MemberInfo
///
/// @author scx567888
/// @version 0.0.1
public sealed interface MemberInfo extends AccessModifierOwner, AnnotatedElementInfo permits ExecutableInfo, FieldInfo {

    /// 持有当前 成员的 ClassInfo
    ClassInfo declaringClass();

    /// 允许访问
    void setAccessible(boolean flag);

}
