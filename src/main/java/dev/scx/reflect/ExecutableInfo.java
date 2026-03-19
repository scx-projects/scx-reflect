package dev.scx.reflect;

/// ExecutableInfo
///
/// @author scx567888
/// @version 0.0.1
public sealed interface ExecutableInfo extends MemberInfo permits ConstructorInfo, MethodInfo {

    ParameterInfo[] parameters();

}
