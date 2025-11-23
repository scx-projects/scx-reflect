package dev.scx.reflect;

/// PrimitiveTypeInfoImpl
///
/// @author scx567888
/// @version 0.0.1
final class PrimitiveTypeInfoImpl implements PrimitiveTypeInfo {

    private final Class<?> rawClass;
    private final int hashCode;

    PrimitiveTypeInfoImpl(Class<?> primitiveClass) {
        // 我们假设 此处 primitiveClass 已经是 Class.isPrimitive 过滤后的
        this.rawClass = primitiveClass;
        this.hashCode = this._hashCode();
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
        if (object instanceof PrimitiveTypeInfoImpl o) {
            return rawClass == o.rawClass;
        }
        return false;
    }

    private int _hashCode() {
        int result = PrimitiveTypeInfoImpl.class.hashCode();
        result = 31 * result + rawClass.hashCode();
        return result;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return rawClass.getName();
    }

}
