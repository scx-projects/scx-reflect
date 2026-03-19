package dev.scx.reflect;

import java.lang.reflect.Constructor;

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
        // 缓存 hashCode
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
        if (object == this) {
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
        sb.append(accessModifier.text());

        // 名称 (类名)
        sb.append(" ");
        sb.append(declaringClass.rawClass().getSimpleName());

        // 参数列表
        sb.append("(");
        for (int i = 0; i < parameters.length; i = i + 1) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(parameters[i].toString());
        }
        sb.append(")");

        return sb.toString();
    }

}
