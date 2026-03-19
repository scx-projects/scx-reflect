package dev.scx.reflect;

import java.util.Arrays;

import static dev.scx.reflect.ReflectSupport._findParameterTypes;

/// MethodSignature
///
/// @author scx567888
/// @version 0.0.1
public final class MethodSignature {

    private final String name;
    private final Class<?>[] parameterTypes;

    private final int hashCode;

    MethodSignature(MethodInfo methodInfo) {
        this.name = methodInfo.name();
        this.parameterTypes = _findParameterTypes(methodInfo);
        // 缓存 hashCode
        this.hashCode = this._hashCode();
    }

    public String name() {
        return name;
    }

    public Class<?>[] parameterTypes() {
        return parameterTypes.clone();
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof MethodSignature o) {
            return name.equals(o.name) && Arrays.equals(parameterTypes, o.parameterTypes);
        }
        return false;
    }

    private int _hashCode() {
        int result = name.hashCode();
        result = 31 * result + Arrays.hashCode(parameterTypes);
        return result;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append(name);
        // 参数列表
        sb.append("(");
        for (int i = 0; i < parameterTypes.length; i = i + 1) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(parameterTypes[i].getSimpleName());
        }
        sb.append(")");
        return sb.toString();
    }

}
