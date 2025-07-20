package cool.scx.reflect;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;

import static cool.scx.reflect.TypeFactory.typeofFromAny;
import static cool.scx.reflect.TypeFactory.typeofFromClass;

/// ArrayTypeInfoImpl
///
/// @author scx567888
/// @version 0.0.1
final class ArrayTypeInfoImpl implements ArrayTypeInfo {

    private final Class<?> rawClass;
    private final TypeInfo componentType;

    ArrayTypeInfoImpl(Class<?> arrayClass) {
        // 我们假设 此处 type 已经是 Class.isArray 过滤后的
        this.rawClass = arrayClass;
        this.componentType = typeofFromClass(this.rawClass.componentType());
    }

    ArrayTypeInfoImpl(GenericArrayType type, TypeResolutionContext context) {
        this.componentType = typeofFromAny(type.getGenericComponentType(), context);
        // 这里虚拟一个没有泛型的数组类型, 但因为 java 数组是协变的所以问题不大
        this.rawClass = Array.newInstance(this.componentType.rawClass(), 0).getClass();
    }

    @Override
    public Class<?> rawClass() {
        return rawClass;
    }

    @Override
    public TypeInfo componentType() {
        return componentType;
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof ArrayTypeInfoImpl that) {
            return componentType.equals(that.componentType);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = ArrayTypeInfoImpl.class.hashCode();
        result = 31 * result + componentType.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return componentType.toString() + "[]";
    }

}
