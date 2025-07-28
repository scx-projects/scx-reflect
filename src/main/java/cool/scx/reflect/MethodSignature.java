package cool.scx.reflect;

import java.util.Arrays;
import java.util.Objects;

public final class MethodSignature {

    private final String name;
    private final Class<?>[] parameterTypes;
    private final int hashCode;

    public MethodSignature(MethodInfo methodInfo) {
        this.name = methodInfo.name();
        this.parameterTypes = methodInfo.rawMethod().getParameterTypes();
        this.hashCode = this._hashCode();
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

}
