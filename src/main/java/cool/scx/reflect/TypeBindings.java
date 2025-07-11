package cool.scx.reflect;

import java.lang.reflect.TypeVariable;
import java.util.Map;

/// TypeBindings
///
/// 注意 : 为了性能考虑 所有数组返回值都是直接暴漏的内部值 请不要修改
///
/// @author scx567888
/// @version 0.0.1
public sealed interface TypeBindings extends Iterable<Map.Entry<TypeVariable<?>, TypeInfo>> permits TypeBindingsImpl {

    TypeInfo get(TypeVariable<?> typeVariable);

    TypeInfo get(String name);

    TypeInfo get(int index);

    TypeVariable<?>[] typeVariables();

    TypeInfo[] typeInfos();

    int size();

    boolean isEmpty();

}
