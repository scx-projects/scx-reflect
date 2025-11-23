package dev.scx.reflect;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static dev.scx.reflect.ReflectSupport.*;
import static dev.scx.reflect.TypeFactory.typeOfAny;
import static java.lang.reflect.AccessFlag.*;

/// MethodInfoImpl (非线程安全)
///
/// @author scx567888
/// @version 0.0.1
final class MethodInfoImpl implements MethodInfo {

    private final Method rawMethod;
    private final ClassInfo declaringClass;

    private final String name;
    private final AccessModifier accessModifier;
    private final boolean isAbstract;
    private final boolean isFinal;
    private final boolean isStatic;
    private final boolean isNative;
    private final boolean isDefault;
    private final ParameterInfo[] parameters;
    private final TypeInfo returnType;

    private final MethodSignature signature;

    // 锁
    private final Lock LOCK;

    private final int hashCode;

    private volatile MethodInfo[] superMethods;
    private volatile MethodInfo[] allSuperMethods;

    MethodInfoImpl(Method method, ClassInfo declaringClass) {
        this.rawMethod = method;
        this.declaringClass = declaringClass;
        this.name = this.rawMethod.getName();
        var accessFlags = this.rawMethod.accessFlags();
        this.accessModifier = _findAccessModifier(accessFlags);
        this.isAbstract = accessFlags.contains(ABSTRACT);
        this.isFinal = accessFlags.contains(FINAL);
        this.isStatic = accessFlags.contains(STATIC);
        this.isNative = accessFlags.contains(NATIVE);
        this.isDefault = this.rawMethod.isDefault();
        this.parameters = _findParameters(this.rawMethod, this);
        this.returnType = typeOfAny(this.rawMethod.getGenericReturnType(), new TypeResolutionContext(this.declaringClass.bindings()));
        this.signature = new MethodSignature(this);
        this.LOCK = new ReentrantLock();
        this.hashCode = this._hashCode();
    }

    @Override
    public Method rawMethod() {
        return rawMethod;
    }

    @Override
    public ClassInfo declaringClass() {
        return declaringClass;
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
    public boolean isNative() {
        return isNative;
    }

    @Override
    public boolean isDefault() {
        return isDefault;
    }

    @Override
    public ParameterInfo[] parameters() {
        return parameters.clone();
    }

    @Override
    public TypeInfo returnType() {
        return returnType;
    }

    @Override
    public MethodSignature signature() {
        return signature;
    }

    @Override
    public MethodInfo[] superMethods() {
        if (superMethods == null) {
            LOCK.lock();
            try {
                if (superMethods == null) {
                    superMethods = _findSuperMethods(this);
                }
            } finally {
                LOCK.unlock();
            }
        }
        return superMethods.clone();
    }

    @Override
    public MethodInfo[] allSuperMethods() {
        if (allSuperMethods == null) {
            LOCK.lock();
            try {
                if (allSuperMethods == null) {
                    allSuperMethods = _findAllSuperMethods(this);
                }
            } finally {
                LOCK.unlock();
            }
        }
        return allSuperMethods.clone();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof MethodInfoImpl o) {
            return rawMethod.equals(o.rawMethod);
        }
        return false;
    }

    private int _hashCode() {
        int result = MethodInfoImpl.class.hashCode();
        result = 31 * result + rawMethod.hashCode();
        return result;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();

        // 修饰符
        sb.append(accessModifier.name().toLowerCase());

        if (isDefault) {
            sb.append(" default");
        }
        if (isStatic) {
            sb.append(" static");
        }
        if (isAbstract) {
            sb.append(" abstract");
        }
        if (isFinal) {
            sb.append(" final");
        }

        // 返回类型
        sb.append(" ").append(returnType.toString());

        // 类名 + 方法名
        sb.append(" ").append(name);

        // 参数列表
        sb.append("(");
        var paramsStr = Arrays.stream(parameters).map(Object::toString).toList();
        sb.append(String.join(", ", paramsStr));
        sb.append(")");

        return sb.toString();
    }

}
