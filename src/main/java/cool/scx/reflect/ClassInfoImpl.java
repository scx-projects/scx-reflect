package cool.scx.reflect;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;

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

    // 理论上 是可能存在 一个 ParameterizedType 的泛型参数 是自身的情况
    // 比如一个类定义如下:
    // public class Node<T extends Node<T>> {
    //
    // }
    // 
    // Type type = Node.class.getTypeParameters()[0].getBounds()[0];
    //
    // 这时 type 的 泛型参数实际上就是 自引用的
    // 虽然我们可以 通过 在 ClassInfoImpl 构造函数中 提前缓存半成品 的方式来部分绕过, 但是此处我们并不这么做
    // 因为我们在 ScxReflect 中只对外提供了 一个通过 Class 和 一个通过 TypeReference 进行创建 TypeInfo 的方式
    // 用户 仅通过这两个 方法是没办法构建出 一个 存在自引用的 类型的
    // 所以实际上不会触发 递归解析 bindings, 这里就简单处理了
    ClassInfoImpl(ParameterizedType parameterizedType, TypeBindings contextBindings) {
        // 我们假设 ParameterizedType 不是用户自定义的 那么 getRawType 的返回值实际上永远都是 Class, 此处强转安全
        this.rawClass = (Class<?>) parameterizedType.getRawType();
        this.bindings = _findBindings(parameterizedType, contextBindings);

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
        if (object == this) {
            return true;
        }
        if (object instanceof ClassInfoImpl classInfo) {
            return rawClass == classInfo.rawClass && bindings.equals(classInfo.bindings);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = rawClass.hashCode();
        result = 31 * result + bindings.hashCode();
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
