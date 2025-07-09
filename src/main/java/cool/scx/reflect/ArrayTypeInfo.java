package cool.scx.reflect;

/// ArrayTypeInfo
///
/// @author scx567888
/// @version 0.0.1
public sealed interface ArrayTypeInfo extends TypeInfo permits ArrayTypeInfoImpl {

    /// 组件类型, 此处只表示 当前组件的类型, 即对于多维数组 例如 String[][] 再此处拿到的是 String[]
    TypeInfo componentType();

}
