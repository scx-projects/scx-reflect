package cool.scx.reflect;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;

import static cool.scx.reflect.ScxReflect.TYPE_CACHE;
import static cool.scx.reflect.ScxReflect.getTypeFromClass;

/// ArrayTypeInfoImpl
///
/// @author scx567888
/// @version 0.0.1
final class ArrayTypeInfoImpl implements ArrayTypeInfo {

    private final Class<?> rawClass;
    private final TypeInfo componentType;

    ArrayTypeInfoImpl(Class<?> arrayClass) {
        if (!arrayClass.isArray()) {
            throw new IllegalArgumentException(arrayClass.getName() + " is not a array");
        }
        this.rawClass = arrayClass;
        this.componentType = getTypeFromClass(this.rawClass.componentType());
    }

    //todo 这个构造函数可能有问题
    /// 根据 GenericArrayType 创建
    ArrayTypeInfoImpl(GenericArrayType type, TypeBindings bindings) {
        TYPE_CACHE.put(TypeKey.createTypeKey(type, bindings), this);

        this.componentType = ScxReflect.getTypeFromAny(type.getGenericComponentType(), bindings);
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
