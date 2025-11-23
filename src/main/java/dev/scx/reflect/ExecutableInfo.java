package dev.scx.reflect;

/// 表示 可执行的 比如 普通方法 或 构造方法
///
/// @author scx567888
/// @version 0.0.1
public sealed interface ExecutableInfo extends MemberInfo permits ConstructorInfo, MethodInfo {

    ParameterInfo[] parameters();

}
