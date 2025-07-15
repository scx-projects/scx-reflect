package cool.scx.reflect;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Objects;

import static cool.scx.reflect.ReflectSupport.*;
import static cool.scx.reflect.TypeBindingsImpl.EMPTY_BINDINGS;

/// 为了保证 泛型解析的准确性 这个类实际上相当于一个小型的 TypeInfo
final class TypeKey {

    private final Class<?> rawClass;
    private final TypeBindings bindings;

    private TypeKey(Class<?> rawClass, TypeBindings bindings) {
        this.rawClass = rawClass;
        this.bindings = bindings;
    }

    private TypeKey(TypeInfo typeInfo) {
        switch (typeInfo) {
            case ClassInfo classInfo -> {
                this.rawClass = classInfo.rawClass();
                this.bindings = classInfo.bindings();
            }
            case ArrayTypeInfo arrayTypeInfo -> {
                this.rawClass = arrayTypeInfo.rawClass();
                this.bindings = EMPTY_BINDINGS;
            }
            case PrimitiveTypeInfo primitiveTypeInfo -> {
                this.rawClass = primitiveTypeInfo.rawClass();
                this.bindings = EMPTY_BINDINGS;
            }
        }
    }

    public static TypeKey createTypeKey(Type type, TypeBindings bindings) {
        if (type instanceof Class<?> c) {
            return new TypeKey(c, EMPTY_BINDINGS);
        }
        if (type instanceof ParameterizedType p) {
            return new TypeKey((Class<?>) p.getRawType(), _findBindings(type, bindings));
        }
        if (type instanceof GenericArrayType g) {
            return new TypeKey(_findArrayRawClass(type, _findComponentType(g, bindings)), EMPTY_BINDINGS);
        }
        if (type instanceof TypeVariable<?> t) {
            //尝试从从绑定中获取 否则回退到 上界
            var typeInfo = bindings.get(t);
            if (typeInfo != null) {
                return new TypeKey(typeInfo);
            }
            return createTypeKey(t.getBounds()[0], bindings);
        }
        if (type instanceof WildcardType w) {
            //直接回退到上界
            return createTypeKey(w.getUpperBounds()[0], bindings);
        }
        throw new IllegalArgumentException("unsupported type: " + type);
    }

    public Type rawClass() {
        return rawClass;
    }

    public TypeBindings bindings() {
        return bindings;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof TypeKey typeKey) {
            return Objects.equals(rawClass, typeKey.rawClass) && Objects.equals(bindings, typeKey.bindings);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(rawClass);
        result = 31 * result + Objects.hashCode(bindings);
        return result;
    }

    @Override
    public String toString() {
        //todo 匿名内部类会变成空字符串
        var shortName = rawClass.getSimpleName();
        var typeArgs = Arrays.stream(bindings.typeInfos()).map(TypeInfo::toString).toList();
        if (typeArgs.isEmpty()) {
            return shortName;
        } else {
            return shortName + "<" + String.join(", ", typeArgs) + ">";
        }
    }

}
