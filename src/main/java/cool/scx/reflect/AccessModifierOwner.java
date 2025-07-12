package cool.scx.reflect;

/// 具有访问修饰符的元素
///
/// @author scx567888
/// @version 0.0.1
public sealed interface AccessModifierOwner permits DeclaredTypeInfo, MemberInfo {

    AccessModifier accessModifier();

}
