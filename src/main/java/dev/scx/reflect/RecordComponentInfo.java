package dev.scx.reflect;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;

///  RecordComponentInfo
///
/// @author scx567888
/// @version 0.0.1
public sealed interface RecordComponentInfo extends AnnotatedElementInfo permits RecordComponentInfoImpl {

    /// 原始 RecordComponent
    RecordComponent rawRecordComponent();

    /// 持有当前 成员的 ClassInfo
    ClassInfo declaringClass();

    /// name
    String name();

    /// 组件的类型
    TypeInfo recordComponentType();

    /// 获取内容
    default Object get(Object obj) throws InvocationTargetException, IllegalAccessException {
        return rawRecordComponent().getAccessor().invoke(obj);
    }

    @Override
    default AnnotatedElement annotatedElement() {
        return rawRecordComponent();
    }

}
