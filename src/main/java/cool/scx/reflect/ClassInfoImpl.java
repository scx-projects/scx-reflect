package cool.scx.reflect;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

import static cool.scx.reflect.ReflectSupport.*;
import static cool.scx.reflect.ScxReflect.TYPE_CACHE;
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

    ClassInfoImpl(Type type, TypeBindings bindings) {
        TYPE_CACHE.put(new TypeKey(type, bindings), this);

        this.rawClass = _findRawClass(type);
        this.bindings = _findBindings(type, bindings);

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

    @Override
    public boolean equals(Object object) {
        if (object instanceof ClassInfoImpl classInfo) {
            return Objects.equals(rawClass, classInfo.rawClass) && Objects.equals(bindings, classInfo.bindings);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(rawClass);
        result = 31 * result + Objects.hashCode(bindings);
        return result;
    }

    @Override
    public String toString() {
        //todo 匿名内部类会变成空字符串
        var shortName = rawClass.getSimpleName();
        var typeArgs = Arrays.stream(bindings.typeInfos()).map(TypeInfo::toString).toList();
        if (typeArgs.isEmpty()) {
            return shortName;
        } else {
            return shortName + "<" + String.join(", ", typeArgs) + ">";
        }
    }

}
