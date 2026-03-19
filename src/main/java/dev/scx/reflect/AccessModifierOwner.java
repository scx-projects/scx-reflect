package dev.scx.reflect;

/// AccessModifierOwner
///
/// @author scx567888
/// @version 0.0.1
public sealed interface AccessModifierOwner permits MemberInfo, ClassInfo {

    AccessModifier accessModifier();

}
