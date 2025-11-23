package dev.scx.reflect;

import java.util.Arrays;
import java.util.Objects;

import static dev.scx.reflect.ReflectSupport._findParameterTypes;

/// MethodSignature
///
/// @author scx567888
/// @version 0.0.1
public final class MethodSignature {

    private final MethodInfo methodInfo;
    private final String name;
    private final Class<?>[] parameterTypes;
    private final int hashCode;

    public MethodSignature(MethodInfo methodInfo) {
        this.methodInfo = methodInfo;
        this.name = methodInfo.name();
        this.parameterTypes = _findParameterTypes(methodInfo);
        this.hashCode = this._hashCode();
    }

    public MethodInfo methodInfo() {
        return methodInfo;
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
        int result = Objects.hashCode(name);
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
        var paramsStr = Arrays.stream(parameterTypes).map(Class::getSimpleName).toList();
        sb.append(String.join(", ", paramsStr));
        sb.append(")");
        return sb.toString();
    }

}
