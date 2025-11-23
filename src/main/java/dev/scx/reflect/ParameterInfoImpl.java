package dev.scx.reflect;

import java.lang.reflect.Parameter;

import static dev.scx.reflect.TypeFactory.typeOfAny;

/// ParameterInfoImpl
///
/// @author scx567888
/// @version 0.0.1
final class ParameterInfoImpl implements ParameterInfo {

    private final Parameter rawParameter;
    private final ExecutableInfo declaringExecutable;
    private final String name;
    private final TypeInfo parameterType;
    private final int hashCode;

    ParameterInfoImpl(Parameter parameter, ExecutableInfo declaringExecutable) {
        this.rawParameter = parameter;
        this.declaringExecutable = declaringExecutable;
        this.name = this.rawParameter.getName();
        this.parameterType = typeOfAny(this.rawParameter.getParameterizedType(), new TypeResolutionContext(this.declaringExecutable.declaringClass().bindings()));
        this.hashCode = this._hashCode();
    }

    @Override
    public Parameter rawParameter() {
        return rawParameter;
    }

    @Override
    public ExecutableInfo declaringExecutable() {
        return declaringExecutable;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public TypeInfo parameterType() {
        return parameterType;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof ParameterInfoImpl o) {
            return rawParameter.equals(o.rawParameter);
        }
        return false;
    }

    private int _hashCode() {
        int result = ParameterInfoImpl.class.hashCode();
        result = 31 * result + rawParameter.hashCode();
        return result;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return parameterType.toString() + " " + name();
    }

}
