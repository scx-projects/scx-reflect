package dev.scx.reflect;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Parameter;

/// ParameterInfo
///
/// @author scx567888
/// @version 0.0.1
public sealed interface ParameterInfo extends AnnotatedElementInfo permits ParameterInfoImpl {

    Parameter rawParameter();

    ExecutableInfo declaringExecutable();

    String name();

    TypeInfo parameterType();

    @Override
    default AnnotatedElement annotatedElement() {
        return rawParameter();
    }

}
