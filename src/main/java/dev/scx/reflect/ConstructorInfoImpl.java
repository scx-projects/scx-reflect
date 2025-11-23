package dev.scx.reflect;

import java.lang.reflect.Constructor;
import java.util.Arrays;

import static dev.scx.reflect.ReflectSupport._findAccessModifier;
import static dev.scx.reflect.ReflectSupport._findParameters;

/// ConstructorInfoImpl
///
/// @author scx567888
/// @version 0.0.1
final class ConstructorInfoImpl implements ConstructorInfo {

    private final Constructor<?> rawConstructor;
    private final ClassInfo declaringClass;
    private final AccessModifier accessModifier;
    private final ParameterInfo[] parameters;
    private final int hashCode;

    ConstructorInfoImpl(Constructor<?> constructor, ClassInfo declaringClass) {
        this.rawConstructor = constructor;
        this.declaringClass = declaringClass;
        var accessFlags = this.rawConstructor.accessFlags();
        this.accessModifier = _findAccessModifier(accessFlags);
        this.parameters = _findParameters(this.rawConstructor, this);
        this.hashCode = this._hashCode();
    }

    @Override
    public Constructor<?> rawConstructor() {
        return rawConstructor;
    }

    @Override
    public ClassInfo declaringClass() {
        return declaringClass;
    }

    @Override
    public AccessModifier accessModifier() {
        return accessModifier;
    }

    @Override
    public ParameterInfo[] parameters() {
        return parameters.clone();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof ConstructorInfoImpl o) {
            return rawConstructor.equals(o.rawConstructor);
        }
        return false;
    }

    private int _hashCode() {
        int result = ConstructorInfoImpl.class.hashCode();
        result = 31 * result + rawConstructor.hashCode();
        return result;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();

        // 修饰符
        sb.append(accessModifier.name().toLowerCase());

        // 名称（类名）
        sb.append(" ");
        sb.append(declaringClass.rawClass().getSimpleName());

        // 参数列表
        sb.append("(");
        var paramsStr = Arrays.stream(parameters).map(Object::toString).toList();
        sb.append(String.join(", ", paramsStr));
        sb.append(")");

        return sb.toString();
    }

}
