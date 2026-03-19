package dev.scx.reflect;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;

/// RecordComponentInfo
///
/// @author scx567888
/// @version 0.0.1
public sealed interface RecordComponentInfo extends AnnotatedElementInfo permits RecordComponentInfoImpl {

    RecordComponent rawRecordComponent();

    ClassInfo declaringClass();

    String name();

    TypeInfo recordComponentType();

    default Object get(Object obj) throws IllegalAccessException, InvocationTargetException {
        return rawRecordComponent().getAccessor().invoke(obj);
    }

    @Override
    default AnnotatedElement annotatedElement() {
        return rawRecordComponent();
    }

}
