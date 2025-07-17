package cool.scx.reflect;

import java.lang.reflect.ParameterizedType;
import java.util.IdentityHashMap;
import java.util.Map;

/// 解析上下文
final class TypeResolutionContext {

    private final TypeBindings bindings;
    //正在解析的半成品 ClassInfo, 用于解决递归问题
    private Map<ParameterizedType, ClassInfo> inProgressTypes;

    public TypeResolutionContext(TypeBindings bindings) {
        this.bindings = bindings;
    }

    public TypeBindings bindings() {
        return bindings;
    }

    public Map<ParameterizedType, ClassInfo> inProgressTypes() {
        if (inProgressTypes == null) {
            inProgressTypes = new IdentityHashMap<>();
        }
        return inProgressTypes;
    }

}
