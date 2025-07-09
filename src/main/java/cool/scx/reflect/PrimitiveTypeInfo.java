package cool.scx.reflect;

/// PrimitiveTypeInfo
///
/// @author scx567888
/// @version 0.0.1
public sealed interface PrimitiveTypeInfo extends TypeInfo permits PrimitiveTypeInfoImpl {

    /// 基本类型的 Class
    Class<?> primitiveClass();

}
