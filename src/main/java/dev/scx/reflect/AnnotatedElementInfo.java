package dev.scx.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

/// AnnotatedElementInfo
///
/// @author scx567888
/// @version 0.0.1
public sealed interface AnnotatedElementInfo permits ClassInfo, MemberInfo, ParameterInfo, RecordComponentInfo {

    AnnotatedElement annotatedElement();

    /// 返回当前元素上直接声明的注解.
    default Annotation[] annotations() {
        return annotatedElement().getDeclaredAnnotations();
    }

    /// 查找当前元素上直接声明的指定类型注解.
    default <T extends Annotation> T findAnnotation(Class<T> annotationClass) {
        return annotatedElement().getDeclaredAnnotation(annotationClass);
    }

    /// 查找当前元素上直接声明的全部指定类型注解.
    default <T extends Annotation> T[] findAnnotations(Class<T> annotationClass) {
        return annotatedElement().getDeclaredAnnotationsByType(annotationClass);
    }

}
