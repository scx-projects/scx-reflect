package dev.scx.reflect;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/// TypeReference
///
/// @author scx567888
/// @version 0.0.1
public abstract class TypeReference<T> {

    protected final Type type;

    protected TypeReference() {
        Type superClass = this.getClass().getGenericSuperclass();
        if (superClass instanceof Class<?>) {
            throw new IllegalArgumentException("TypeReference must be instantiated with an actual type argument");
        } else {
            this.type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
        }
    }

    public final Type type() {
        return this.type;
    }

}
