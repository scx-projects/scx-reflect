package cool.scx.reflect;

import static cool.scx.reflect.ScxReflect.TYPE_CACHE;
import static cool.scx.reflect.TypeBindingsImpl.EMPTY_BINDINGS;

/// PrimitiveTypeInfoImpl
///
/// @author scx567888
/// @version 0.0.1
final class PrimitiveTypeInfoImpl implements PrimitiveTypeInfo {

    private final Class<?> rawClass;

    PrimitiveTypeInfoImpl(Class<?> primitiveClass) {
        TYPE_CACHE.put(TypeKey.createTypeKey(primitiveClass, EMPTY_BINDINGS), this);

        // 我们假设 此处 primitiveClass 已经是 Class.isPrimitive 过滤后的
        this.rawClass = primitiveClass;
    }

    @Override
    public Class<?> rawClass() {
        return rawClass;
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof PrimitiveTypeInfoImpl that) {
            return rawClass == that.rawClass;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = PrimitiveTypeInfoImpl.class.hashCode();
        result = 31 * result + rawClass.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return rawClass.getName();
    }

}
