package cool.scx.reflect;

import java.lang.reflect.Type;
import java.util.Objects;

import static cool.scx.reflect.ReflectSupport._findArrayRawClass;
import static cool.scx.reflect.ReflectSupport._findComponentType;
import static cool.scx.reflect.ScxReflect.TYPE_CACHE;

/// ArrayTypeInfoImpl
///
/// @author scx567888
/// @version 0.0.1
final class ArrayTypeInfoImpl implements ArrayTypeInfo {

    private final Class<?> rawClass;
    private final TypeInfo componentType;

    ArrayTypeInfoImpl(Type type, TypeBindings bindings) {
        TYPE_CACHE.put(TypeKey.createTypeKey(type, bindings), this);

        // 我们假设 此处 type 已经是 Class.isArray 过滤后的 或 GenericArrayType 
        this.componentType = _findComponentType(type, bindings);
        this.rawClass = _findArrayRawClass(type, this.componentType);
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
        if (object instanceof ArrayTypeInfoImpl that) {
            return Objects.equals(componentType, that.componentType);
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
