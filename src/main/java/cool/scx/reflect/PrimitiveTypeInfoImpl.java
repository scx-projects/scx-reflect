package cool.scx.reflect;

/// PrimitiveTypeInfoImpl
///
/// @author scx567888
/// @version 0.0.1
final class PrimitiveTypeInfoImpl implements PrimitiveTypeInfo {

    private final Class<?> rawClass;

    PrimitiveTypeInfoImpl(Class<?> primitiveClass) {
        if (!primitiveClass.isPrimitive()) {
            throw new IllegalArgumentException(primitiveClass.getName() + " is not a primitive");
        }
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
