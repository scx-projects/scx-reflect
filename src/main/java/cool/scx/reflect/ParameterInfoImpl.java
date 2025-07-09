package cool.scx.reflect;

import java.lang.reflect.Parameter;

/// ParameterInfoImpl
///
/// @author scx567888
/// @version 0.0.1
final class ParameterInfoImpl implements ParameterInfo {

    private final Parameter rawParameter;
    private final ExecutableInfo declaringExecutable;
    private final String name;
    private final TypeInfo parameterType;

    ParameterInfoImpl(Parameter parameter, ExecutableInfo declaringExecutable) {
        this.rawParameter = parameter;
        this.declaringExecutable = declaringExecutable;
        this.name = this.rawParameter.getName();
        this.parameterType = ScxReflect.getType(this.rawParameter.getParameterizedType(), this.declaringExecutable.declaringClass().bindings());
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
    public String toString() {
        return parameterType.toString() + " " + name();
    }

}
