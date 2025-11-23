package dev.scx.reflect;

import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/// TypeBindingsImpl
///
/// @author scx567888
/// @version 0.0.1
final class TypeBindingsImpl implements TypeBindings {

    public static final TypeBindings EMPTY_BINDINGS = new TypeBindingsImpl(new TypeVariable[0], new TypeInfo[0]);

    private final TypeVariable<?>[] typeVariables;
    private final TypeInfo[] typeInfos;
    private final int hashCode;

    TypeBindingsImpl(TypeVariable<?>[] typeVariables, TypeInfo[] typeInfos) {
        // 此处我们假设 typeVariables 和 typeInfos 是长度相等 顺序正确对应的
        this.typeVariables = typeVariables;
        this.typeInfos = typeInfos;
        this.hashCode = this._hashCode();
    }

    @Override
    public TypeInfo get(TypeVariable<?> typeVariable) {
        for (int i = 0; i < typeVariables.length; i = i + 1) {
            var t = typeVariables[i];
            if (t.equals(typeVariable)) {
                return typeInfos[i];
            }
        }
        return null;
    }

    @Override
    public TypeInfo get(String name) {
        for (int i = 0; i < typeVariables.length; i = i + 1) {
            var t = typeVariables[i];
            if (t.getName().equals(name)) {
                return typeInfos[i];
            }
        }
        return null;
    }

    @Override
    public TypeInfo get(int index) {
        if (index >= 0 && index < typeInfos.length) {
            return typeInfos[index];
        }
        return null;
    }

    @Override
    public TypeVariable<?>[] typeVariables() {
        return typeVariables.clone();
    }

    @Override
    public TypeInfo[] typeInfos() {
        return typeInfos.clone();
    }

    @Override
    public int size() {
        return typeVariables.length;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public Iterator<Map.Entry<TypeVariable<?>, TypeInfo>> iterator() {
        return new TypeBindingsIterator(this);
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof TypeBindingsImpl o) {
            return Arrays.equals(typeVariables, o.typeVariables) && Arrays.equals(typeInfos, o.typeInfos);
        }
        return false;
    }

    private int _hashCode() {
        int result = Arrays.hashCode(typeVariables);
        result = 31 * result + Arrays.hashCode(typeInfos);
        return result;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append('{');
        for (int i = 0; i < typeVariables.length; i = i + 1) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(typeVariables[i].getName()).append("=").append(typeInfos[i].toString());
        }
        sb.append('}');
        return sb.toString();
    }

    private static final class TypeBindingsIterator implements Iterator<Map.Entry<TypeVariable<?>, TypeInfo>> {

        private final TypeBindingsImpl bindings;
        private int index;

        private TypeBindingsIterator(TypeBindingsImpl bindings) {
            this.bindings = bindings;
            this.index = 0;
        }

        @Override
        public boolean hasNext() {
            return index < bindings.typeVariables.length;
        }

        @Override
        public Map.Entry<TypeVariable<?>, TypeInfo> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            var key = bindings.typeVariables[index];
            var value = bindings.typeInfos[index];
            index = index + 1;
            return Map.entry(key, value);
        }

    }

}
