package dev.scx.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

/// AnnotatedElementInfo
///
/// @author scx567888
/// @version 0.0.1
public sealed interface AnnotatedElementInfo permits ClassInfo, MemberInfo, ParameterInfo, RecordComponentInfo {

    /// annotatedElement
    AnnotatedElement annotatedElement();

    /// 获取指定元素上直接声明的注解.
    /// 注解本质是一种元编程手段, 从语义角度讲, 注解应该严格绑定在它声明的位置,
    /// 不应该隐含任何继承或传播机制. 为了最大限度地减少语义混淆,
    /// 这里仅返回 当前元素上直接声明的注解, 不包含父类, 接口或其他继承层次的注解.
    default Annotation[] annotations() {
        return annotatedElement().getDeclaredAnnotations();
    }

    /// 查找指定类型的注解 (从直接注解上)
    default <T extends Annotation> T findAnnotation(Class<T> annotationClass) {
        return annotatedElement().getDeclaredAnnotation(annotationClass);
    }

    /// 查找所有指定类型的注解 (从直接注解上)
    default <T extends Annotation> T[] findAnnotations(Class<T> annotationClass) {
        return annotatedElement().getDeclaredAnnotationsByType(annotationClass);
    }

}
