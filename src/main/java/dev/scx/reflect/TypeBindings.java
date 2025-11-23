package dev.scx.reflect;

import java.lang.reflect.TypeVariable;
import java.util.Map;

/// TypeBindings
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
