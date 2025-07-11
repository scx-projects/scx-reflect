package cool.scx.reflect;

/// 表示 可执行的 比如 普通方法 或 构造方法
///
///  注意 : 为了性能考虑 所有数组返回值都是直接暴漏的内部值 请不要修改
///
/// @author scx567888
/// @version 0.0.1
public sealed interface ExecutableInfo extends MemberInfo permits ConstructorInfo, MethodInfo {

    ParameterInfo[] parameters();

}
