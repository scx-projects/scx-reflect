package dev.scx.reflect;

import java.lang.reflect.ParameterizedType;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static dev.scx.reflect.ReflectSupport.*;
import static dev.scx.reflect.TypeBindingsImpl.EMPTY_BINDINGS;
import static java.lang.reflect.AccessFlag.*;

/// ClassInfoImpl
///
/// @author scx567888
/// @version 0.0.1
final class ClassInfoImpl implements ClassInfo {

    // TypeInfo
    private final Class<?> rawClass;
    private final TypeBindings bindings;
    private final ClassInfo declaringClass;

    // 访问修饰符
    private final AccessModifier accessModifier;

    // 类的基本信息
    private final ClassKind classKind;

    // 类属性
    private final boolean isStatic;
    private final boolean isFinal;
    private final boolean isAbstract;

    // 锁
    private final Lock LOCK;

    // 缓存的 hashCode
    private final int hashCode;

    // 继承结构
    private volatile boolean superClassLoaded;
    private volatile ClassInfo superClass;
    private volatile ClassInfo[] interfaces;

    // 类成员
    private volatile ConstructorInfo[] constructors;
    private volatile FieldInfo[] fields;
    private volatile MethodInfo[] methods;
    private volatile RecordComponentInfo[] recordComponents;

    // 快捷属性
    private volatile TypeBindings allBindings;
    private volatile ClassInfo[] allSuperClasses;
    private volatile ClassInfo[] allInterfaces;
    private volatile FieldInfo[] allFields;
    private volatile MethodInfo[] allMethods;
    private volatile boolean defaultConstructorLoaded;
    private volatile ConstructorInfo defaultConstructor;
    private volatile boolean recordConstructorLoaded;
    private volatile ConstructorInfo recordConstructor;

    ClassInfoImpl(Class<?> clazz) {

        // 我们假设 此处 clazz 已经是 !Class.isArray 并且 !Class.isPrimitive 过滤后的
        this.rawClass = clazz;
        this.bindings = EMPTY_BINDINGS;
        this.declaringClass = _findDeclaringClass(clazz, null);

        var accessFlags = this.rawClass.accessFlags();

        this.accessModifier = _findAccessModifier(accessFlags);
        this.classKind = _findClassKind(this.rawClass, accessFlags);
        this.isStatic = accessFlags.contains(STATIC);
        this.isFinal = accessFlags.contains(FINAL);
        this.isAbstract = accessFlags.contains(ABSTRACT);

        this.LOCK = new ReentrantLock();
        // 缓存 hashCode
        this.hashCode = this._hashCode();
    }

    ClassInfoImpl(ParameterizedType parameterizedType, TypeResolutionContext context) {
        // 添加半成品对象, 用于支持 _findBindings/_findDeclaringClass 中的递归泛型解析
        context.inProgressTypes().put(parameterizedType, this);

        // 我们假设 ParameterizedType 不是用户自定义的 那么 getRawType 的返回值实际上永远都是 Class, 此处强转安全
        this.rawClass = (Class<?>) parameterizedType.getRawType();
        this.bindings = _findBindings(parameterizedType, context);
        this.declaringClass = _findDeclaringClass(parameterizedType, context);

        // 移除半成品对象.
        context.inProgressTypes().remove(parameterizedType);

        var accessFlags = this.rawClass.accessFlags();

        this.accessModifier = _findAccessModifier(accessFlags);
        this.classKind = _findClassKind(this.rawClass, accessFlags);
        this.isStatic = accessFlags.contains(STATIC);
        this.isFinal = accessFlags.contains(FINAL);
        this.isAbstract = accessFlags.contains(ABSTRACT);

        this.LOCK = new ReentrantLock();
        // 缓存 hashCode
        this.hashCode = this._hashCode();
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
    public ClassInfo declaringClass() {
        return declaringClass;
    }

    @Override
    public AccessModifier accessModifier() {
        return accessModifier;
    }

    @Override
    public String name() {
        return rawClass.getName();
    }

    @Override
    public ClassKind classKind() {
        return classKind;
    }

    @Override
    public boolean isStatic() {
        return isStatic;
    }

    @Override
    public boolean isFinal() {
        return isFinal;
    }

    @Override
    public boolean isAbstract() {
        return isAbstract;
    }

    @Override
    public boolean isAnonymousClass() {
        return this.rawClass.isAnonymousClass();
    }

    @Override
    public boolean isMemberClass() {
        return this.rawClass.isMemberClass();
    }

    @Override
    public boolean isLocalClass() {
        return this.rawClass.isLocalClass();
    }

    @Override
    public ClassInfo superClass() {
        // 此处 superClass == null 无法确定是 未加载 还是 不存在, 所以使用 superClassLoaded 字段标识.
        if (!superClassLoaded) {
            LOCK.lock();
            try {
                if (!superClassLoaded) {
                    superClass = _findSuperClass(this.rawClass, this.allBindings());
                    superClassLoaded = true;
                }
            } finally {
                LOCK.unlock();
            }
        }
        return superClass;
    }

    @Override
    public ClassInfo[] interfaces() {
        if (interfaces == null) {
            LOCK.lock();
            try {
                if (interfaces == null) {
                    interfaces = _findInterfaces(this.rawClass, this.allBindings());
                }
            } finally {
                LOCK.unlock();
            }
        }
        return interfaces.clone();
    }

    @Override
    public ConstructorInfo[] constructors() {
        if (constructors == null) {
            LOCK.lock();
            try {
                if (constructors == null) {
                    constructors = _findConstructors(this.rawClass, this);
                }
            } finally {
                LOCK.unlock();
            }
        }
        return constructors.clone();
    }

    @Override
    public FieldInfo[] fields() {
        if (fields == null) {
            LOCK.lock();
            try {
                if (fields == null) {
                    fields = _findFields(this.rawClass, this);
                }
            } finally {
                LOCK.unlock();
            }
        }
        return fields.clone();
    }

    @Override
    public MethodInfo[] methods() {
        if (methods == null) {
            LOCK.lock();
            try {
                if (methods == null) {
                    methods = _findMethods(this.rawClass, this);
                }
            } finally {
                LOCK.unlock();
            }
        }
        return methods.clone();
    }

    @Override
    public RecordComponentInfo[] recordComponents() {
        if (recordComponents == null) {
            LOCK.lock();
            try {
                if (recordComponents == null) {
                    recordComponents = _findRecordComponents(this);
                }
            } finally {
                LOCK.unlock();
            }
        }
        return recordComponents.clone();
    }

    @Override
    public TypeBindings allBindings() {
        if (allBindings == null) {
            LOCK.lock();
            try {
                if (allBindings == null) {
                    allBindings = _findAllBindings(this);
                }
            } finally {
                LOCK.unlock();
            }
        }
        return allBindings;
    }

    @Override
    public ClassInfo[] allSuperClasses() {
        if (allSuperClasses == null) {
            LOCK.lock();
            try {
                if (allSuperClasses == null) {
                    allSuperClasses = _findAllSuperClasses(this);
                }
            } finally {
                LOCK.unlock();
            }
        }
        return allSuperClasses.clone();
    }

    @Override
    public ClassInfo[] allInterfaces() {
        if (allInterfaces == null) {
            LOCK.lock();
            try {
                if (allInterfaces == null) {
                    allInterfaces = _findAllInterfaces(this);
                }
            } finally {
                LOCK.unlock();
            }
        }
        return allInterfaces.clone();
    }

    @Override
    public FieldInfo[] allFields() {
        if (allFields == null) {
            LOCK.lock();
            try {
                if (allFields == null) {
                    allFields = _findAllFields(this);
                }
            } finally {
                LOCK.unlock();
            }
        }
        return allFields.clone();
    }

    @Override
    public MethodInfo[] allMethods() {
        if (allMethods == null) {
            LOCK.lock();
            try {
                if (allMethods == null) {
                    allMethods = _findAllMethods(this);
                }
            } finally {
                LOCK.unlock();
            }
        }
        return allMethods.clone();
    }

    @Override
    public ConstructorInfo defaultConstructor() {
        if (!defaultConstructorLoaded) {
            LOCK.lock();
            try {
                if (!defaultConstructorLoaded) {
                    defaultConstructor = _findDefaultConstructor(this);
                    defaultConstructorLoaded = true;
                }
            } finally {
                LOCK.unlock();
            }
        }
        return defaultConstructor;
    }

    @Override
    public ConstructorInfo recordConstructor() {
        if (!recordConstructorLoaded) {
            LOCK.lock();
            try {
                if (!recordConstructorLoaded) {
                    recordConstructor = _findRecordConstructor(this);
                    recordConstructorLoaded = true;
                }
            } finally {
                LOCK.unlock();
            }
        }
        return recordConstructor;
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof ClassInfoImpl o) {
            return rawClass == o.rawClass &&
                bindings.equals(o.bindings) &&
                Objects.equals(declaringClass, o.declaringClass);
        }
        return false;
    }

    private int _hashCode() {
        int result = rawClass.hashCode();
        result = 31 * result + bindings.hashCode();
        result = 31 * result + Objects.hashCode(declaringClass);
        return result;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        // 匿名类使用全名, 否则使用短名.
        var baseName = isAnonymousClass() ? rawClass.getName() : rawClass.getSimpleName();

        // 有 bindings 拼接泛型
        if (!bindings.isEmpty()) {
            var sb = new StringBuilder(baseName);
            sb.append('<');
            var typeInfos = bindings.typeInfos();
            for (int i = 0; i < typeInfos.length; i = i + 1) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(typeInfos[i].toString());
            }
            sb.append(">");
            baseName = sb.toString();
        }

        if (declaringClass == null) {
            return baseName;
        }

        return declaringClass + "." + baseName;
    }

}
