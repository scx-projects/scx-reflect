package dev.scx.reflect;

/// MemberInfo
///
/// @author scx567888
/// @version 0.0.1
public sealed interface MemberInfo extends AccessModifierOwner, AnnotatedElementInfo permits ExecutableInfo, FieldInfo {

    ClassInfo declaringClass();

    void setAccessible(boolean flag);

}
