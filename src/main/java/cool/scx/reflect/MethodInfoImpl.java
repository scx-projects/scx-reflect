package cool.scx.reflect;

import java.lang.reflect.Method;
import java.util.Arrays;

import static cool.scx.reflect.ReflectSupport.*;
import static cool.scx.reflect.TypeFactory.typeofFromAny;
import static java.lang.reflect.AccessFlag.*;

/// MethodInfoImpl
///
/// @author scx567888
/// @version 0.0.1
final class MethodInfoImpl implements MethodInfo {

    private final Method rawMethod;
    private final ClassInfo declaringClass;

    private final String name;
    private final AccessModifier accessModifier;
    private final boolean isAbstract;
    private final boolean isFinal;
    private final boolean isStatic;
    private final boolean isNative;
    private final boolean isDefault;
    private final ParameterInfo[] parameters;
    private final TypeInfo returnType;

    private boolean superMethodLoaded;
    private MethodInfo superMethod;

    MethodInfoImpl(Method method, ClassInfo declaringClass) {
        this.rawMethod = method;
        this.declaringClass = declaringClass;
        this.name = this.rawMethod.getName();
        var accessFlags = this.rawMethod.accessFlags();
        this.accessModifier = _findAccessModifier(accessFlags);
        this.isAbstract = accessFlags.contains(ABSTRACT);
        this.isFinal = accessFlags.contains(FINAL);
        this.isStatic = accessFlags.contains(STATIC);
        this.isNative = accessFlags.contains(NATIVE);
        this.isDefault = this.rawMethod.isDefault();
        this.parameters = _findParameters(this.rawMethod, this);
        this.returnType = typeofFromAny(this.rawMethod.getGenericReturnType(), new TypeResolutionContext(this.declaringClass.bindings()));
    }

    @Override
    public Method rawMethod() {
        return rawMethod;
    }

    @Override
    public ClassInfo declaringClass() {
        return declaringClass;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public AccessModifier accessModifier() {
        return accessModifier;
    }

    @Override
    public boolean isAbstract() {
        return isAbstract;
    }

    @Override
    public boolean isFinal() {
        return isFinal;
    }

    @Override
    public boolean isStatic() {
        return isStatic;
    }

    @Override
    public boolean isNative() {
        return isNative;
    }

    @Override
    public boolean isDefault() {
        return isDefault;
    }

    @Override
    public ParameterInfo[] parameters() {
        return parameters.clone();
    }

    @Override
    public TypeInfo returnType() {
        return returnType;
    }

    @Override
    public MethodInfo superMethod() {
        if (!superMethodLoaded) {
            superMethod = _findSuperMethod(this);
            superMethodLoaded = true;
        }
        return superMethod;
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();

        // 修饰符
        sb.append(accessModifier.name().toLowerCase());

        if (isDefault) {
            sb.append(" default");
        }
        if (isStatic) {
            sb.append(" static");
        }
        if (isAbstract) {
            sb.append(" abstract");
        }
        if (isFinal) {
            sb.append(" final");
        }

        // 返回类型
        sb.append(" ").append(returnType.toString());

        // 类名 + 方法名
        sb.append(" ").append(name);

        // 参数列表
        sb.append("(");
        var paramsStr = Arrays.stream(parameters).map(Object::toString).toList();
        sb.append(String.join(", ", paramsStr));
        sb.append(")");

        return sb.toString();
    }

}
