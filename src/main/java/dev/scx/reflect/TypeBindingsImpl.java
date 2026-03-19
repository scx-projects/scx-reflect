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
        // 构造函数只可能被内部调用, 这里无需 copy 数组.
        // 同时我们假设 typeVariables 和 typeInfos 是长度相等, 顺序正确对应的.
        this.typeVariables = typeVariables;
        this.typeInfos = typeInfos;
        // 缓存 hashCode
        this.hashCode = this._hashCode();
    }

    @Override
    public TypeInfo get(TypeVariable<?> typeVariable) {
        // 我们倒序遍历, 原因如下:
        //
        // 当 TypeBindings 表示 allBindings() 合并结果时, 会采用 "外层 -> 内层" 的顺序存储类型变量绑定.
        // 例如对于类型:
        //
        //   Outer<String>.Inner<Integer>
        //
        // allBindings() 的顺序为:
        //
        //   T -> String
        //   U -> Integer
        //
        // 也就是说, 外层类型变量在前, 当前类型变量在后.
        //
        // 在进行查找时, 应当遵循 Java 的词法作用域规则:
        //   当前作用域优先, 外层作用域兜底.
        //
        // 因此查找时需要从后向前遍历:
        //   先检查当前类型的类型变量,
        //   若未命中, 再回退到外层类型的类型变量.
        //
        // 对于 get(TypeVariable<?>) 来说, 由于 TypeVariable 本身具有唯一身份,
        // 顺序通常不会影响正确性. 但为了与 get(String) 的查找语义保持一致,
        // 仍然采用倒序遍历.
        for (int i = typeVariables.length - 1; i >= 0; i = i - 1) {
            var t = typeVariables[i];
            if (t.equals(typeVariable)) {
                return typeInfos[i];
            }
        }
        return null;
    }

    @Override
    public TypeInfo get(String name) {
        // 倒序遍历, 使当前作用域优先匹配, 外层作用域作为兜底.
        for (int i = typeVariables.length - 1; i >= 0; i = i - 1) {
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
