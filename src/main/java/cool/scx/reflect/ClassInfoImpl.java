package cool.scx.reflect;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static cool.scx.reflect.ReflectSupport.*;
import static cool.scx.reflect.TypeBindingsImpl.EMPTY_BINDINGS;
import static java.lang.reflect.AccessFlag.*;

/// ClassInfoImpl
///
/// @author scx567888
/// @version 0.0.1
final class ClassInfoImpl implements ClassInfo {

    // TypeInfo
    private final Class<?> rawClass;
    private final TypeBindings bindings;

    // 类的基本信息
    private final String name;
    private final AccessModifier accessModifier;
    private final ClassKind classKind;

    // 类属性
    private final boolean isAbstract;
    private final boolean isFinal;
    private final boolean isStatic;
    private final boolean isAnonymousClass;
    private final boolean isMemberClass;

    // 继承结构
    private boolean superClassLoaded;
    private ClassInfo superClass;
    private ClassInfo[] interfaces;

    // 类成员
    private ConstructorInfo[] constructors;
    private FieldInfo[] fields;
    private MethodInfo[] methods;

    //快捷属性
    private ClassInfo[] allSuperClasses;
    private ClassInfo[] allInterfaces;
    private boolean defaultConstructorLoaded;
    private ConstructorInfo defaultConstructor;
    private boolean recordConstructorLoaded;
    private ConstructorInfo recordConstructor;
    private FieldInfo[] allFields;
    private MethodInfo[] allMethods;
    private ClassInfo enumClass;
    private RecordComponentInfo[] recordComponents;

    ClassInfoImpl(Class<?> clazz) {
        // 我们假设 此处 type 已经是 !Class.isArray 并且 !Class.isPrimitive 过滤后的
        this.rawClass = clazz;
        this.bindings = EMPTY_BINDINGS;

        this.name = this.rawClass.getName();

        var accessFlags = this.rawClass.accessFlags();
        this.accessModifier = _findAccessModifier(accessFlags);
        this.classKind = _findClassKind(this.rawClass, accessFlags);
        this.isAbstract = accessFlags.contains(ABSTRACT);
        this.isFinal = accessFlags.contains(FINAL);
        this.isStatic = accessFlags.contains(STATIC);
        this.isAnonymousClass = this.rawClass.isAnonymousClass();
        this.isMemberClass = this.rawClass.isMemberClass();

    }

    ClassInfoImpl(ParameterizedType parameterizedType, TypeResolutionContext context) {
        //添加半成品对象, 用于支持 _findBindings 中的递归泛型解析
        context.inProgressTypes().put(parameterizedType, this);

        // 我们假设 ParameterizedType 不是用户自定义的 那么 getRawType 的返回值实际上永远都是 Class, 此处强转安全
        this.rawClass = (Class<?>) parameterizedType.getRawType();
        this.bindings = _findBindings(parameterizedType, context);

        this.name = this.rawClass.getName();

        var accessFlags = this.rawClass.accessFlags();
        this.accessModifier = _findAccessModifier(accessFlags);
        this.classKind = _findClassKind(this.rawClass, accessFlags);
        this.isAbstract = accessFlags.contains(ABSTRACT);
        this.isFinal = accessFlags.contains(FINAL);
        this.isStatic = accessFlags.contains(STATIC);
        this.isAnonymousClass = this.rawClass.isAnonymousClass();
        this.isMemberClass = this.rawClass.isMemberClass();

    }

    @Override
    public Class<?> rawClass() {
        return rawClass;
    }

    @Override
    public TypeBindings bindings() {
        return bindings;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public AccessModifier accessModifier() {
        return accessModifier;
    }

    @Override
    public ClassKind classKind() {
        return classKind;
    }

    @Override
    public boolean isAbstract() {
        return isAbstract;
    }

    @Override
    public boolean isFinal() {
        return isFinal;
    }

    @Override
    public boolean isStatic() {
        return isStatic;
    }

    @Override
    public boolean isAnonymousClass() {
        return isAnonymousClass;
    }

    @Override
    public boolean isMemberClass() {
        return isMemberClass;
    }

    @Override
    public ClassInfo superClass() {
        //此处 superClass == null 无法确定是 未加载 还是 没有 所以使用 单独字段
        if (!superClassLoaded) {
            superClass = _findSuperClass(this.rawClass, this.bindings);
            superClassLoaded = true;
        }
        return superClass;
    }

    @Override
    public ClassInfo[] interfaces() {
        if (interfaces == null) {
            interfaces = _findInterfaces(this.rawClass, this.bindings);
        }
        return interfaces.clone();
    }

    @Override
    public ConstructorInfo[] constructors() {
        if (constructors == null) {
            constructors = _findConstructors(this.rawClass, this);
        }
        return constructors.clone();
    }

    @Override
    public FieldInfo[] fields() {
        if (fields == null) {
            fields = _findFields(this.rawClass, this);
        }
        return fields.clone();
    }

    @Override
    public MethodInfo[] methods() {
        if (methods == null) {
            methods = _findMethods(this.rawClass, this);
        }
        return methods.clone();
    }

    @Override
    public ClassInfo[] allSuperClasses() {
        if (allSuperClasses == null) {
            allSuperClasses = _findAllSuperClasses(this);
        }
        return allSuperClasses.clone();
    }

    @Override
    public ClassInfo[] allInterfaces() {
        if (allInterfaces == null) {
            allInterfaces = _findAllInterfaces(this);
        }
        return allInterfaces.clone();
    }

    @Override
    public ConstructorInfo defaultConstructor() {
        if (!defaultConstructorLoaded) {
            defaultConstructor = _findDefaultConstructor(this);
            defaultConstructorLoaded = true;
        }
        return defaultConstructor;
    }

    @Override
    public ConstructorInfo recordConstructor() {
        if (!recordConstructorLoaded) {
            recordConstructor = _findRecordConstructor(this);
            recordConstructorLoaded = true;
        }
        return recordConstructor;
    }

    @Override
    public FieldInfo[] allFields() {
        if (allFields == null) {
            allFields = _findAllFields(this);
        }
        return allFields.clone();
    }

    @Override
    public MethodInfo[] allMethods() {
        if (allMethods == null) {
            allMethods = _findAllMethods(this);
        }
        return allMethods.clone();
    }

    @Override
    public ClassInfo enumClass() {
        if (enumClass == null) {
            enumClass = _findEnumClass(this);
        }
        return enumClass;
    }

    @Override
    public RecordComponentInfo[] recordComponents() {
        if (recordComponents == null) {
            recordComponents = _findRecordComponents(this);
        }
        return recordComponents.clone();
    }

    //todo 有问题
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof ClassInfoImpl classInfo) {
            return rawClass == classInfo.rawClass && bindings.equals(classInfo.bindings);
        }
        return false;
    }

    //todo 有问题
    @Override
    public int hashCode() {
        int result = rawClass.hashCode();
        result = 31 * result + bindings.hashCode();
        return result;
    }

    //todo 有问题
    @Override
    public String toString() {
        return toString(new HashSet<>());
    }

    //todo 有问题
    private String toString(Set<TypeInfo> visited) {
        //内部类使用全名
        if (isAnonymousClass) {
            return name;
        }
        //没有 bindings 使用短名
        var shortName = rawClass.getSimpleName();
        if (bindings.isEmpty()) {
            return shortName;
        }

        // 避免递归
        if (!visited.add(this)) {
            return null;
        }
        try {
            var typeArgs = new ArrayList<String>();
            var typeInfos = bindings.typeInfos();
            for (int i = 0; i < typeInfos.length; i = i + 1) {
                var typeInfo = typeInfos[i];
                if (typeInfo instanceof ClassInfoImpl c) {
                    var str = c.toString(visited); // 递归调用 with visited
                    // 表示递归引用了 这里使用 泛型参数名称替换
                    if (str == null) {
                        typeArgs.add(bindings.typeVariables()[i].getName());
                    } else {
                        typeArgs.add(str);
                    }
                } else {
                    typeArgs.add(typeInfo.toString());
                }
            }
            return shortName + "<" + String.join(", ", typeArgs) + ">";
        } finally {
            visited.remove(this);
        }
    }

}
