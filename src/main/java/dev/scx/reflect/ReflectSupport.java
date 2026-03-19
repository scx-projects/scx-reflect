package dev.scx.reflect;

import java.lang.reflect.*;
import java.util.*;

import static dev.scx.reflect.AccessModifier.*;
import static dev.scx.reflect.ClassKind.*;
import static dev.scx.reflect.TypeFactory.typeOfAny;
import static java.util.Collections.addAll;

/// ReflectSupport
///
/// @author scx567888
/// @version 0.0.1
final class ReflectSupport {

    // ************************ AccessModifier 相关 ***************************

    public static AccessModifier _findAccessModifier(Set<AccessFlag> accessFlags) {
        if (accessFlags.contains(AccessFlag.PUBLIC)) {
            return PUBLIC;
        }
        if (accessFlags.contains(AccessFlag.PROTECTED)) {
            return PROTECTED;
        }
        if (accessFlags.contains(AccessFlag.PRIVATE)) {
            return PRIVATE;
        }
        return PACKAGE_PRIVATE;
    }

    // ************************ ClassInfo 核心 相关 ***************************

    public static TypeBindings _findBindings(ParameterizedType type, TypeResolutionContext context) {
        // 我们假设 ParameterizedType 不是用户自定义的 那么 getRawType 的返回值实际上永远都是 Class
        var typeVariables = ((Class<?>) type.getRawType()).getTypeParameters();
        // 这里我们假设 typeParameters 和 actualTypeArguments 的长度和顺序是完全一一对应的
        var actualTypeArguments = type.getActualTypeArguments();
        var typeInfos = new TypeInfo[actualTypeArguments.length];
        for (int i = 0; i < actualTypeArguments.length; i = i + 1) {
            var actualTypeArgument = actualTypeArguments[i];
            var typeInfo = typeOfAny(actualTypeArgument, context);
            typeInfos[i] = typeInfo;
        }
        return new TypeBindingsImpl(typeVariables, typeInfos);
    }

    public static ClassInfo _findDeclaringClass(Type type, TypeResolutionContext context) {
        var declaringClass = switch (type) {
            case Class<?> c -> c.getDeclaringClass();
            case ParameterizedType p -> p.getOwnerType();
            default -> null;
        };
        // 这里必然是 ClassInfo
        return declaringClass != null ? (ClassInfo) typeOfAny(declaringClass, context) : null;
    }

    public static ClassKind _findClassKind(Class<?> rawClass, Set<AccessFlag> accessFlags) {
        if (accessFlags.contains(AccessFlag.ANNOTATION)) {
            return ANNOTATION;
        }
        if (accessFlags.contains(AccessFlag.INTERFACE)) {
            return INTERFACE;
        }
        if (accessFlags.contains(AccessFlag.ENUM)) {
            return ENUM;
        }
        if (rawClass.isRecord()) {
            return RECORD;
        }
        return CLASS;
    }

    public static ClassInfo _findSuperClass(Class<?> rawClass, TypeBindings bindings) {
        var superClass = rawClass.getGenericSuperclass();
        // superClass 只可能是 Class (非数组,非基本类型) 或 ParameterizedType (rawClass 同样非数组,非基本类型)
        // 所以我们 使用 typeOfAny 返回的也必然是 ClassInfo, 此处强转安全
        return superClass != null ? (ClassInfo) typeOfAny(superClass, new TypeResolutionContext(bindings)) : null;
    }

    public static ClassInfo[] _findInterfaces(Class<?> rawClass, TypeBindings bindings) {
        var interfaces = rawClass.getGenericInterfaces();
        // interface 只可能是 Class (非数组, 非基本类型) 或 ParameterizedType (rawClass 同样非数组,非基本类型)
        // 所以我们 使用 typeOfAny 返回的也必然是 ClassInfo, 此处强转安全
        var result = new ClassInfo[interfaces.length];
        for (int i = 0; i < interfaces.length; i = i + 1) {
            result[i] = (ClassInfo) typeOfAny(interfaces[i], new TypeResolutionContext(bindings));
        }
        return result;
    }

    /// 和 _findFields 和 _findMethods 不同 我们完整保留 所有构造函数.
    public static ConstructorInfo[] _findConstructors(Class<?> rawClass, ClassInfo classInfo) {
        var constructors = rawClass.getDeclaredConstructors();
        var result = new ConstructorInfo[constructors.length];
        for (int i = 0; i < constructors.length; i = i + 1) {
            result[i] = new ConstructorInfoImpl(constructors[i], classInfo);
        }
        return result;
    }

    /// 此处我们过滤掉 合成字段.
    public static FieldInfo[] _findFields(Class<?> rawClass, ClassInfo classInfo) {
        var fields = rawClass.getDeclaredFields();
        var list = new ArrayList<FieldInfo>();
        for (var field : fields) {
            if (field.isSynthetic()) {
                continue;
            }
            list.add(new FieldInfoImpl(field, classInfo));
        }
        return list.toArray(FieldInfo[]::new);
    }

    /// 此处我们过滤掉 桥接方法 和 合成方法.
    public static MethodInfo[] _findMethods(Class<?> rawClass, ClassInfo classInfo) {
        var methods = rawClass.getDeclaredMethods();
        var list = new ArrayList<MethodInfo>();
        for (var method : methods) {
            if (method.isBridge() || method.isSynthetic()) {
                continue;
            }
            list.add(new MethodInfoImpl(method, classInfo));
        }
        return list.toArray(MethodInfo[]::new);
    }

    public static RecordComponentInfo[] _findRecordComponents(ClassInfo classInfo) {
        if (classInfo.classKind() != RECORD) {
            return new RecordComponentInfo[0];
        }
        var recordComponents = classInfo.rawClass().getRecordComponents();
        var result = new RecordComponentInfo[recordComponents.length];
        for (int i = 0; i < recordComponents.length; i = i + 1) {
            result[i] = new RecordComponentInfoImpl(recordComponents[i], classInfo);
        }
        return result;
    }

    // ************************ ExecutableInfo 相关 ***************************

    /// 和 _findFields 和 _findMethods 不同 我们完整保留 参数.
    public static ParameterInfo[] _findParameters(Executable rawExecutable, ExecutableInfo executableInfo) {
        var parameters = rawExecutable.getParameters();
        var result = new ParameterInfo[parameters.length];
        for (int i = 0; i < parameters.length; i = i + 1) {
            result[i] = new ParameterInfoImpl(parameters[i], executableInfo);
        }
        return result;
    }

    // ************************ MethodInfo 核心 相关 ***************************

    public static Class<?>[] _findParameterTypes(MethodInfo methodInfo) {
        // 此处不能直接使用 Method.getParameterTypes(), 因为存在泛型擦除的问题
        var parameterInfos = methodInfo.parameters();
        var parameterTypes = new Class<?>[parameterInfos.length];
        for (int i = 0; i < parameterInfos.length; i = i + 1) {
            parameterTypes[i] = parameterInfos[i].parameterType().rawClass();
        }
        return parameterTypes;
    }

    // ************************ MethodInfo 高级 相关 ***************************

    public static MethodInfo[] _findSuperMethods(MethodInfo methodInfo) {
        var result = new LinkedHashSet<MethodInfo>();
        var visited = new HashSet<ClassInfo>();
        var queue = new ArrayDeque<ClassInfo>();

        var declaringClass = methodInfo.declaringClass();

        // 起点: 直接父类 + 直接接口
        var superClass = declaringClass.superClass();
        if (superClass != null) {
            queue.addLast(superClass);
        }
        addAll(queue, declaringClass.interfaces());

        while (!queue.isEmpty()) {
            var current = queue.removeFirst();

            if (!visited.add(current)) {
                continue;
            }

            // 先检查当前类型自己声明的方法
            var matched = false;
            for (var superMethod : current.methods()) {
                if (isOverride(methodInfo, superMethod)) {
                    result.add(superMethod);
                    matched = true;
                    break;
                }
            }

            // 当前路径已经找到第一个父方法, 不再继续向上
            if (matched) {
                continue;
            }

            // 当前节点未找到, 继续向上扩展
            var currentSuperClass = current.superClass();
            if (currentSuperClass != null) {
                queue.addLast(currentSuperClass);
            }
            addAll(queue, current.interfaces());
        }

        return result.toArray(MethodInfo[]::new);
    }

    public static MethodInfo[] _findAllSuperMethods(MethodInfo methodInfo) {
        var result = new LinkedHashSet<MethodInfo>();
        var queue = new ArrayDeque<MethodInfo>();

        addAll(queue, methodInfo.superMethods());

        while (!queue.isEmpty()) {
            var current = queue.removeFirst();

            if (!result.add(current)) {
                continue;
            }

            addAll(queue, current.superMethods());
        }

        return result.toArray(MethodInfo[]::new);
    }

    /// 判断是否为重写方法
    /// 此方法 的判断依据实际上是建立在 methodInfo 和 superMethod 一定是 同一条继承链上的.
    public static boolean isOverride(MethodInfo methodInfo, MethodInfo superMethod) {
        // 只讨论实例方法
        if (methodInfo.isStatic() || superMethod.isStatic()) {
            return false;
        }
        if (superMethod.isFinal()) {
            return false;
        }
        if (superMethod.accessModifier() == PRIVATE) {
            return false;
        }
        if (superMethod.accessModifier() == PACKAGE_PRIVATE) {
            var p1 = superMethod.declaringClass().rawClass().getPackageName();
            var p2 = methodInfo.declaringClass().rawClass().getPackageName();
            // 不同包, 无法重写.
            if (!p1.equals(p2)) {
                return false;
            }
        }

        // 3, 判断方法签名. 返回值无需判断 编译器已保证.
        return methodInfo.signature().equals(superMethod.signature());

    }

    // ************************ ClassInfo 高级 相关 ***************************

    public static TypeBindings _findAllBindings(ClassInfo classInfo) {
        var bindings = classInfo.bindings();

        var declaringClass = classInfo.declaringClass();
        // 静态类不继承 declaringClass 的泛型.
        if (declaringClass == null || classInfo.isStatic()) {
            return bindings;
        }

        var outerBindings = declaringClass.allBindings();

        if (outerBindings.isEmpty()) {
            return bindings;
        }

        if (bindings.isEmpty()) {
            return outerBindings;
        }

        // 合并
        var typeVariables = bindings.typeVariables();
        var typeInfos = bindings.typeInfos();

        var outerTypeVariables = outerBindings.typeVariables();
        var outerTypeInfos = outerBindings.typeInfos();

        var mergedTypeVariables = new TypeVariable[typeVariables.length + outerTypeVariables.length];
        var mergedTypeInfos = new TypeInfo[typeInfos.length + outerTypeInfos.length];

        // 顺序: 外部类在前, 当前类在后.
        System.arraycopy(outerTypeVariables, 0, mergedTypeVariables, 0, outerTypeVariables.length);
        System.arraycopy(typeVariables, 0, mergedTypeVariables, outerTypeVariables.length, typeVariables.length);

        System.arraycopy(outerTypeInfos, 0, mergedTypeInfos, 0, outerTypeInfos.length);
        System.arraycopy(typeInfos, 0, mergedTypeInfos, outerTypeInfos.length, typeInfos.length);

        return new TypeBindingsImpl(mergedTypeVariables, mergedTypeInfos);
    }

    public static ClassInfo[] _findAllSuperClasses(ClassInfo classInfo) {
        var result = new ArrayList<ClassInfo>();
        var superClass = classInfo.superClass();
        while (superClass != null) {
            result.add(superClass);
            superClass = superClass.superClass();
        }
        return result.toArray(ClassInfo[]::new);
    }

    public static ClassInfo[] _findAllInterfaces(ClassInfo classInfo) {
        var result = new LinkedHashSet<ClassInfo>();
        var visited = new HashSet<ClassInfo>();
        var queue = new ArrayDeque<ClassInfo>();

        // 起点顺序:
        // 1. 当前类直接接口
        // 2. 当前类直接父类
        addAll(queue, classInfo.interfaces());

        var superClass = classInfo.superClass();
        if (superClass != null) {
            queue.addLast(superClass);
        }

        while (!queue.isEmpty()) {
            var current = queue.removeFirst();

            if (!visited.add(current)) {
                continue;
            }

            if (current.classKind() == INTERFACE) {
                result.add(current);
            }

            // 扩展顺序:
            // 1. 当前节点直接接口
            // 2. 当前节点直接父类
            addAll(queue, current.interfaces());

            var currentSuperClass = current.superClass();
            if (currentSuperClass != null) {
                queue.addLast(currentSuperClass);
            }
        }

        return result.toArray(ClassInfo[]::new);
    }

    /// 所有字段都保留 无论是否 static.
    public static FieldInfo[] _findAllFields(ClassInfo classInfo) {
        var result = new LinkedHashSet<FieldInfo>();

        // 1. 当前类型自己声明的字段
        addAll(result, classInfo.fields());

        // 2. 父类字段
        var superClass = classInfo.superClass();
        if (superClass != null) {
            addAll(result, superClass.allFields());
        }

        // 3. 接口字段
        for (var i : classInfo.interfaces()) {
            addAll(result, i.allFields());
        }

        return result.toArray(FieldInfo[]::new);
    }

    /// 所有静态方法完整保留 + 移除所有被重写的方法.
    public static MethodInfo[] _findAllMethods(ClassInfo classInfo) {
        var staticMethods = new LinkedHashSet<MethodInfo>();
        var instanceMethods = new LinkedHashSet<MethodInfo>();

        // 1. 添加当前类声明的方法.
        for (var method : classInfo.methods()) {
            if (method.isStatic()) {
                staticMethods.add(method);
            } else {
                instanceMethods.add(method);
            }
        }

        // 2. 添加父类的所有方法.
        var superClass = classInfo.superClass();
        if (superClass != null) {
            for (var method : superClass.allMethods()) {
                if (method.isStatic()) {
                    staticMethods.add(method);
                } else {
                    instanceMethods.add(method);
                }
            }
        }

        // 3. 添加接口的所有方法.
        for (var i : classInfo.interfaces()) {
            for (var method : i.allMethods()) {
                if (method.isStatic()) {
                    staticMethods.add(method);
                } else {
                    instanceMethods.add(method);
                }
            }
        }

        List<MethodInfo> finalInstanceMethods = new ArrayList<>();
        // 这里我们需要对 instanceMethod 进行覆写检查.
        // 先按照方法签名分组.
        var grouped = new LinkedHashMap<MethodSignature, List<MethodInfo>>();
        for (var instanceMethod : instanceMethods) {
            grouped.computeIfAbsent(instanceMethod.signature(), _ -> new ArrayList<>()).add(instanceMethod);
        }
        for (var group : grouped.entrySet()) {
            var m = group.getValue();
            // 只有一个 无需检查
            if (m.size() == 1) {
                finalInstanceMethods.addAll(m);
                continue;
            }
            // 存在多个我们需要寻找真正需要保留的方法.
            var methodInfos = _selectMethods(classInfo, m);
            finalInstanceMethods.addAll(methodInfos);
        }
        // 合并
        var result = new ArrayList<MethodInfo>();
        // 静态方法永远保留.
        result.addAll(staticMethods);
        // 只保留未被重写的实例方法.
        result.addAll(finalInstanceMethods);
        return result.toArray(MethodInfo[]::new);
    }

    /// 在同签名实例方法组中选择当前类型视图应保留的方法
    public static List<MethodInfo> _selectMethods(ClassInfo classInfo, List<MethodInfo> methodInfos) {
        // 1. 收集组内每个方法的全部父方法.
        //    注意: methodInfos 中的方法已经满足:
        //    - 全部是实例方法
        //    - 方法签名一致
        //    因此, 此处收集出来的 allSuperMethods() 正好表示:
        //    "这个组中哪些方法已经被更具体的方法显式覆盖了".
        var override = new ArrayList<MethodInfo>();
        for (var methodInfo : methodInfos) {
            addAll(override, methodInfo.allSuperMethods());
        }

        // 2. 移除所有已经被显式重写的方法.
        //
        //    经过这一步之后, methodInfos 中剩下的方法满足:
        //    - 它们彼此之间不再存在 "显式的继承覆盖关系"
        //    - 也就是说, 剩余方法中, 不会再出现 "A 明确 override B" 这种情况
        //
        //    因而, Java 中最复杂的那部分重写规则
        //    (private/package-private 可见性, 接口重声明, 多层继承链, 菱形继承等)
        //    已经在 allSuperMethods() 的计算过程中被提前消解掉了.
        //
        //    所以, 从这里开始我们不需要再次判断 "谁 override 谁",
        //    只需要处理"多个未被显式覆盖的方法同时出现时, 当前类型视图应该保留哪一类方法".
        methodInfos.removeAll(override);

        // 3. 对剩余方法按 "声明来源(类/接口) + 是否可继承" 分为三组:
        //    - 不可继承的方法 (可以直接保留)
        //    - 可继承的类方法
        //    - 可继承的接口方法
        var result = new ArrayList<MethodInfo>();
        var inheritableClassMethods = new ArrayList<MethodInfo>();
        var inheritableInterfaceMethods = new ArrayList<MethodInfo>();

        for (var methodInfo : methodInfos) {
            var inheritable = _isInheritableFrom(methodInfo, classInfo);

            if (inheritable) {
                if (methodInfo.declaringClass().classKind() == INTERFACE) {
                    inheritableInterfaceMethods.add(methodInfo);
                } else {
                    inheritableClassMethods.add(methodInfo);
                }
            } else {
                // 所有不可继承方法永远保留
                result.add(methodInfo);
            }
        }

        // 4. 若存在可继承的类方法, 它们压掉所有可继承的接口方法. 否则保留可继承的接口方法.
        if (!inheritableClassMethods.isEmpty()) {
            result.addAll(inheritableClassMethods);
        } else {
            result.addAll(inheritableInterfaceMethods);
        }

        return result;
    }

    /// 判断 methodInfo 对于当前 classInfo 视图来说 是否可继承.
    ///
    /// 注意:
    /// - 此处讨论的是 "当前类型视图中, 这个方法是否以可继承方法身份存在"
    /// - private 方法永远不可继承
    /// - 类的 package-private 仅同包可继承
    /// - 接口的 private 方法不可继承, 其余实例方法可继承
    /// - 这里不处理 static, 因为调用方只会传入实例方法
    public static boolean _isInheritableFrom(MethodInfo methodInfo, ClassInfo classInfo) {

        var accessModifier = methodInfo.accessModifier();
        var declaringClass = methodInfo.declaringClass();

        // private 永远不可继承
        if (accessModifier == PRIVATE) {
            return false;
        }

        // 接口方法: 除 private 外, 其余实例方法都视为可继承
        if (declaringClass.classKind() == INTERFACE) {
            return true;
        }

        // 类的 package-private 仅同包可继承
        if (accessModifier == PACKAGE_PRIVATE) {
            var p1 = declaringClass.rawClass().getPackageName();
            var p2 = classInfo.rawClass().getPackageName();
            return p1.equals(p2);
        }

        // public / protected
        return true;
    }

    /// 寻找 无参构造函数 (不支持非静态成员类)
    public static ConstructorInfo _findDefaultConstructor(ClassInfo classInfo) {
        for (var constructor : classInfo.constructors()) {
            if (constructor.parameters().length == 0) {
                return constructor;
            }
        }
        return null;
    }

    /// 寻找 Record 规范构造函数
    public static ConstructorInfo _findRecordConstructor(ClassInfo classInfo) {
        if (classInfo.classKind() != RECORD) {
            return null;
        }
        var recordComponentTypes = _getRecordComponentsTypes(classInfo);
        for (var constructor : classInfo.constructors()) {
            // 判断参数类型是否匹配
            var matched = _hasSameParameterTypes(constructor, recordComponentTypes);
            if (matched) {
                return constructor;
            }
        }
        return null;
    }

    public static TypeInfo[] _getRecordComponentsTypes(ClassInfo classInfo) {
        var recordComponents = classInfo.recordComponents();
        var result = new TypeInfo[recordComponents.length];
        for (int i = 0; i < recordComponents.length; i = i + 1) {
            result[i] = recordComponents[i].recordComponentType();
        }
        return result;
    }

    public static boolean _hasSameParameterTypes(ExecutableInfo constructorInfo, TypeInfo[] types) {
        var p1 = constructorInfo.parameters();
        if (p1.length != types.length) {
            return false;
        }
        for (int i = 0; i < p1.length; i = i + 1) {
            var p1Type = p1[i].parameterType();
            var p2Type = types[i];
            if (p1Type.rawClass() != p2Type.rawClass()) {
                return false;
            }
        }
        return true;
    }

}
