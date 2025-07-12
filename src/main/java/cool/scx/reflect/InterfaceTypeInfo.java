package cool.scx.reflect;

public sealed interface InterfaceTypeInfo extends DeclaredTypeInfo permits InterfaceTypeInfoImpl {

    /// 访问修饰符
    AccessModifier accessModifier();
    
    /// 静态字段列表
    StaticFieldInfo[] staticFields();
    
    /// 静态方法列表
    StaticMethodInfo[] staticMethods();
    
    /// 实例方法列表
    InstanceMethodInfo[] instanceMethods();

    /// 接口列表
    InterfaceTypeInfo[] interfaces();

    /// 获取继承链上的所有接口 (广度遍历顺序)
    InterfaceTypeInfo[] allInterfaces();
    
}
