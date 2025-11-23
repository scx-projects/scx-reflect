package dev.scx.reflect;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;

import static dev.scx.reflect.TypeFactory.typeOfAny;
import static dev.scx.reflect.TypeFactory.typeOfClass;

/// ArrayTypeInfoImpl
///
/// @author scx567888
/// @version 0.0.1
final class ArrayTypeInfoImpl implements ArrayTypeInfo {

    private final Class<?> rawClass;
    private final TypeInfo componentType;
    private final int hashCode;

    ArrayTypeInfoImpl(Class<?> arrayClass) {
        // 我们假设 此处 type 已经是 Class.isArray 过滤后的
        this.rawClass = arrayClass;
        this.componentType = typeOfClass(this.rawClass.componentType());
        // 缓存 hashCode
        this.hashCode = this._hashCode();
    }

    ArrayTypeInfoImpl(GenericArrayType type, TypeResolutionContext context) {
        this.componentType = typeOfAny(type.getGenericComponentType(), context);
        // 这里虚拟一个没有泛型的数组类型, 但因为 java 数组是协变的所以问题不大
        this.rawClass = Array.newInstance(this.componentType.rawClass(), 0).getClass();
        // 缓存 hashCode
        this.hashCode = this._hashCode();
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
        if (object instanceof ArrayTypeInfoImpl o) {
            return componentType.equals(o.componentType);
        }
        return false;
    }

    private int _hashCode() {
        var result = ArrayTypeInfoImpl.class.hashCode();
        result = 31 * result + componentType.hashCode();
        return result;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return componentType.toString() + "[]";
    }

}
