package cool.scx.reflect;

import java.lang.reflect.Type;

import static cool.scx.reflect.TypeBindingsImpl.EMPTY_BINDINGS;
import static cool.scx.reflect.TypeFactory.getTypeFromAny;
import static cool.scx.reflect.TypeFactory.getTypeFromClass;

public final class ScxReflect {

    /// 根据 Class 获取 TypeInfo
    public static TypeInfo getType(Class<?> type) {
        var sss= getTypeFromClass(type);
        //todo 测试代码
        if (sss instanceof ClassInfo classInfo) {
            classInfo.allFields();
            classInfo.allMethods();
            classInfo.allInterfaces();
            classInfo.allSuperClasses();    
        }
        return sss;
    }


    /// 根据 Class 获取 TypeInfo
    public static TypeInfo getType(Type type) {
        var sss= getTypeFromAny(type,new TypeResolutionContext(EMPTY_BINDINGS));
        //todo 测试代码
        if (sss instanceof ClassInfo classInfo) {
            classInfo.fields();
            classInfo.methods();
            classInfo.superClass();
            classInfo.interfaces();
        }
        return sss;
    }

    /// 根据 TypeReference 获取 TypeInfo
    public static TypeInfo getType(TypeReference<?> typeReference) {
        var sss= getTypeFromAny(typeReference.getType(), new TypeResolutionContext(EMPTY_BINDINGS));
        //todo 测试代码
        if (sss instanceof ClassInfo classInfo) {
            classInfo.fields();
            classInfo.methods();
            classInfo.superClass();
            classInfo.interfaces();
        }
        return sss;
    }

}
