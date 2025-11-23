package dev.scx.reflect;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/// TypeReference
///
/// @author scx567888
/// @version 0.0.1
public abstract class TypeReference<T> {

    protected final Type _type;

    protected TypeReference() {
        Type superClass = this.getClass().getGenericSuperclass();
        if (superClass instanceof Class<?>) {
            throw new IllegalArgumentException("Internal error: TypeReference constructed without actual type information");
        } else {
            this._type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
        }
    }

    public final Type getType() {
        return this._type;
    }

}
