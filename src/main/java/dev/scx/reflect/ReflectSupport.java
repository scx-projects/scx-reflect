package dev.scx.reflect;

import java.lang.reflect.AccessFlag;
import java.lang.reflect.Executable;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;

import static dev.scx.reflect.AccessModifier.*;
import static dev.scx.reflect.ClassKind.*;
import static dev.scx.reflect.TypeFactory.typeOfAny;
import static java.util.Collections.addAll;

/// 内部构建辅助类
///
/// @author scx567888
/// @version 0.0.1
final class ReflectSupport {

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

    public static ClassInfo _findSuperClass(Class<?> rawClass, TypeBindings contextBindings) {
        var superClass = rawClass.getGenericSuperclass();
        // superClass 只可能是 Class (非数组,非基本类型) 或 ParameterizedType (rawClass 同样非数组,非基本类型)
        // 所以我们 使用 getType 返回的也必然是 ClassInfo, 此处强转安全
        return superClass != null ? (ClassInfo) typeOfAny(superClass, new TypeResolutionContext(contextBindings)) : null;
    }

    public static ClassInfo[] _findInterfaces(Class<?> rawClass, TypeBindings contextBindings) {
        var interfaces = rawClass.getGenericInterfaces();
        // interface 只可能是 Class (非数组,非基本类型) 或 ParameterizedType (rawClass 同样非数组,非基本类型)
        // 所以我们 使用 getType 返回的也必然是 ClassInfo, 此处强转安全
        var result = new ClassInfo[interfaces.length];
        for (int i = 0; i < interfaces.length; i = i + 1) {
            result[i] = (ClassInfo) typeOfAny(interfaces[i], new TypeResolutionContext(contextBindings));
        }
        return result;
    }

    public static ConstructorInfo[] _findConstructors(Class<?> rawClass, ClassInfo classInfo) {
        var constructors = rawClass.getDeclaredConstructors();
        var result = new ConstructorInfo[constructors.length];
        for (int i = 0; i < constructors.length; i = i + 1) {
            result[i] = new ConstructorInfoImpl(constructors[i], classInfo);
        }
        return result;
    }

    public static FieldInfo[] _findFields(Class<?> rawClass, ClassInfo classInfo) {
        var fields = rawClass.getDeclaredFields();
        var result = new FieldInfo[fields.length];
        for (int i = 0; i < fields.length; i = i + 1) {
            result[i] = new FieldInfoImpl(fields[i], classInfo);
        }
        return result;
    }

    /// 获取当前 ClassInfo 的所有方法 (不包括桥接方法)
    public static MethodInfo[] _findMethods(Class<?> rawClass, ClassInfo classInfo) {
        var methods = rawClass.getDeclaredMethods();
        var list = new ArrayList<MethodInfo>();
        for (var method : methods) {
            //过滤掉桥接方法, 因为我们几乎用不到
            if (!method.isBridge()) {
                list.add(new MethodInfoImpl(method, classInfo));
            }
        }
        return list.toArray(MethodInfo[]::new);
    }

    public static ClassInfo[] _findAllSuperClasses(ClassInfo classInfo) {
        var allSuperClasses = new ArrayList<ClassInfo>();
        var superClass = classInfo.superClass();
        while (superClass != null) {
            allSuperClasses.add(superClass);
            superClass = superClass.superClass();
        }
        return allSuperClasses.toArray(ClassInfo[]::new);
    }

    public static ClassInfo[] _findAllInterfaces(ClassInfo classInfo) {
        // 这里需要 进行广度遍历 (BFS), 但是我们不使用传统的队列方式,
        // 而是使用 类似递归深度遍历 + 行转列,
        // 主要原因是为了 通过调用 父接口的 allInterfaces 来激活整个继承链条的 allInterfaces 缓存
        // 此方法不处理 环形依赖, 因为 java 的编译期 已经保证了不可能存在 环形依赖

        // 1, 使用 LinkedHashSet 保证去重
        var result = new LinkedHashSet<ClassInfo>();
        // 2, 我们需要查找所有父级 包括 superClass 和 interface
        var superClass = classInfo.superClass();
        var interfaces = classInfo.interfaces();

        ClassInfo[] parents;
        if (superClass != null) {
            // 合并父类和接口
            parents = new ClassInfo[interfaces.length + 1];
            parents[0] = superClass;
            System.arraycopy(interfaces, 0, parents, 1, interfaces.length);
        } else {
            parents = classInfo.interfaces();
        }
        // 3, 先将当前层级接口添加进去
        addAll(result, interfaces);
        // 4, 获取所有父接口的 所有接口, 同时找出最大的层级深度
        var temp = new ClassInfo[parents.length][];
        int maxDepth = 0;
        for (int i = 0; i < parents.length; i = i + 1) {
            temp[i] = parents[i].allInterfaces();
            if (temp[i].length > maxDepth) {
                maxDepth = temp[i].length;
            }
        }

        // 4, 按 "行转列" 遍历, 逐层加入接口
        for (int level = 0; level < maxDepth; level = level + 1) {
            for (var classInfos : temp) {
                if (level < classInfos.length) {
                    result.add(classInfos[level]);
                }
            }
        }

        return result.toArray(ClassInfo[]::new);
    }

    public static ParameterInfo[] _findParameters(Executable rawExecutable, ExecutableInfo executableInfo) {
        var parameters = rawExecutable.getParameters();
        var result = new ParameterInfo[parameters.length];
        for (int i = 0; i < parameters.length; i = i + 1) {
            result[i] = new ParameterInfoImpl(parameters[i], executableInfo);
        }
        return result;
    }

    /// 寻找 无参构造函数 (不支持成员类)
    public static ConstructorInfo _findDefaultConstructor(ClassInfo classInfo) {
        for (var constructor : classInfo.constructors()) {
            if (constructor.parameters().length == 0) {
                return constructor;
            }
        }
        return null;
    }

    /// 寻找 Record 规范构造参数
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

    private static TypeInfo[] _getRecordComponentsTypes(ClassInfo classInfo) {
        var recordComponents = classInfo.recordComponents();
        var result = new TypeInfo[recordComponents.length];
        for (int i = 0; i < recordComponents.length; i = i + 1) {
            // 这里因为是 后于 constructors 调用的
            // 所以 理论上 所有的 recordComponent 都能够再 TYPE_CACHE 中直接找到对应的 TypeInfo
            result[i] = recordComponents[i].recordComponentType();
        }
        return result;
    }

    private static boolean _hasSameParameterTypes(ExecutableInfo constructorInfo, TypeInfo[] types) {
        if (constructorInfo.parameters().length != types.length) {
            return false;
        }
        var p1 = constructorInfo.parameters();
        for (int i = 0; i < p1.length; i = i + 1) {
            var p1Type = p1[i].parameterType();
            var p2Type = types[i];
            if (p1Type != p2Type) {
                return false;
            }
        }
        return true;
    }

    public static FieldInfo[] _findAllFields(ClassInfo classInfo) {
        //此处不直接使用 allSuperClasses 循环添加, 也是为了 尝试激活整个链路的 allFields 缓存
        var allFieldInfos = new ArrayList<FieldInfo>();
        //添加自己的字段
        addAll(allFieldInfos, classInfo.fields());
        //添加父级的字段 (如果有父级的话)
        var superClass = classInfo.superClass();
        if (superClass != null) {
            addAll(allFieldInfos, superClass.allFields());
        }
        return allFieldInfos.toArray(FieldInfo[]::new);
    }

    public static MethodInfo[] _findSuperMethods(MethodInfo methodInfo) {
        //静态方法不存在被重写的可能, 所以不可能拥有 superMethod
        if (methodInfo.isStatic()) {
            return new MethodInfo[]{};
        }

        var result = new ArrayList<MethodInfo>();

        // 先查找父类
        root:
        for (var i : methodInfo.declaringClass().allSuperClasses()) {
            for (var superMethod : i.methods()) {
                //静态方法 和 final 不可能是 superMethod 直接跳过
                if (superMethod.isStatic() || superMethod.isFinal()) {
                    continue;
                }
                // 只查找第一次匹配的方法
                if (isOverride(methodInfo, superMethod)) {
                    result.add(superMethod);
                    //父类是单继承的, 所以一旦匹配成功一次就不需要再向上找了
                    break root;
                }
            }
        }

        // 记录访问过的接口
        var needSkip = new HashSet<ClassInfo>();

        // 再查找接口
        for (var i : methodInfo.declaringClass().allInterfaces()) {
            if (needSkip.contains(i)) {
                continue;
            }
            for (var superMethod : i.methods()) {
                //静态方法 和 final 不可能是 superMethod 直接跳过
                if (superMethod.isStatic() || superMethod.isFinal()) {
                    continue;
                }
                // 只查找第一次匹配的方法
                if (isOverride(methodInfo, superMethod)) {
                    result.add(superMethod);
                    // 一旦找到, 这表明 当前接口的 整个继承链条中 的所有的接口都不需要继续查找了
                    addAll(needSkip, i.allInterfaces());
                    break;
                }
            }
        }

        return result.toArray(MethodInfo[]::new);
    }

    /// 写法参考 _findAllInterfaces
    public static MethodInfo[] _findAllSuperMethods(MethodInfo methodInfo) {
        // 这里需要 进行广度遍历 (BFS), 但是我们不使用传统的队列方式,
        // 而是使用 类似递归深度遍历 + 行转列,
        // 主要原因是为了 通过调用 父接口的 allSuperMethods 来激活整个继承链条的 allSuperMethods 缓存
        // 此方法不处理 环形依赖, 因为 java 的编译期 已经保证了不可能存在 环形依赖

        // 1, 使用 LinkedHashSet 保证去重
        var result = new LinkedHashSet<MethodInfo>();
        // 2, 先将当前层级接口添加进去
        var superMethods = methodInfo.superMethods();
        addAll(result, superMethods);
        // 3, 获取所有父方法的 所有方法, 同时找出最大的层级深度
        var temp = new MethodInfo[superMethods.length][];
        int maxDepth = 0;
        for (int i = 0; i < superMethods.length; i = i + 1) {
            temp[i] = superMethods[i].allSuperMethods();
            if (temp[i].length > maxDepth) {
                maxDepth = temp[i].length;
            }
        }

        // 4, 按 "行转列" 遍历, 逐层加入接口
        for (int level = 0; level < maxDepth; level = level + 1) {
            for (var methodInfos : temp) {
                if (level < methodInfos.length) {
                    result.add(methodInfos[level]);
                }
            }
        }

        return result.toArray(MethodInfo[]::new);
    }

    /// 这里需要我们正确模拟 java 的重写逻辑
    public static MethodInfo[] _findAllMethods(ClassInfo classInfo) {
        var staticMethods = new LinkedHashSet<MethodInfo>();
        var instanceMethods = new LinkedHashSet<MethodInfo>();

        // 1. 添加当前类声明的方法，并记录它们覆盖的父方法
        for (var method : classInfo.methods()) {
            if (method.isStatic()) {
                staticMethods.add(method);
            } else {
                instanceMethods.add(method);
            }
        }

        // 2. 添加父类的方法（排除被覆盖的）
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

        // 3. 添加接口的方法（排除被覆盖的）
        var interfaces = classInfo.interfaces();
        for (var i : interfaces) {
            for (MethodInfo method : i.allMethods()) {
                if (method.isStatic()) {
                    staticMethods.add(method);
                } else {
                    instanceMethods.add(method);
                }
            }
        }

        List<MethodInfo> finalInstanceMethods = new ArrayList<>();
        // 这里我们需要对 instanceMethod 进行覆写检查
        // 先分组
        var map = instanceMethods.stream().collect(Collectors.groupingBy(MethodInfo::signature));
        for (var e : map.entrySet()) {
            var value = e.getValue();
            // 只有以一个 无需检查
            if (value.size() == 1) {
                finalInstanceMethods.addAll(value);
                continue;
            }
            var methodInfos = _selectMethods(value);
            finalInstanceMethods.addAll(methodInfos);
        }
        // 合并
        var result = new ArrayList<MethodInfo>();
        result.addAll(staticMethods);
        result.addAll(finalInstanceMethods);
        return result.toArray(MethodInfo[]::new);
    }

    /// 在一众方法中寻找 最符合的方法
    public static List<MethodInfo> _selectMethods(List<MethodInfo> methodInfos) {
        // 先把所有被重写的方法全部移除
        var override = new ArrayList<MethodInfo>();
        for (var methodInfo : methodInfos) {
            addAll(override, methodInfo.allSuperMethods());
        }
        methodInfos.removeAll(override);

        // 1. 先找出所有具体（非抽象）方法
        List<MethodInfo> concreteMethods = methodInfos.stream().filter(m -> !m.isAbstract()).toList();

        if (concreteMethods.size() == 1) {
            // 如果有具体实现，抽象方法和接口默认方法被覆盖，直接返回具体实现（可能多个，表示冲突）
            return concreteMethods;
        } else if (concreteMethods.size() > 1) {
            // 这里只有可能是一个 实例方法和 一个 default 方法
            for (var concreteMethod : concreteMethods) {
                // 寻找不是 default 的方法 也就是实例方法
                if (!concreteMethod.isDefault()) {
                    return List.of(concreteMethod);
                }
            }
            // 不可达异常, 未找到任何的 实例方法 这在正常情况下是不可能的
            throw new IllegalStateException("存在多个 default 方法, 但未找到任何实例方法.");
        }

        // 2. 否则全部是抽象方法，全部返回（接口多继承多抽象方法）
        return methodInfos;
    }

    /// 返回当前 ClassInfo 所表示的枚举类的 "真实" 枚举类型。
    /// 在 Java 中, 枚举常量有时会被编译成枚举的匿名子类 (匿名枚举类),
    /// 这时直接拿匿名类的 ClassInfo 并不能代表真正的枚举类型.
    /// 该方法的作用是:
    /// - 如果 classInfo 表示的是匿名枚举类, 则返回它的父类 (即真正的枚举类).
    /// - 如果 classInfo 是普通的枚举类, 则直接返回它自身.
    /// - 如果不是枚举类，则返回 null.
    /// 这样在处理枚举类型时，可以统一拿到 "真实" 的枚举声明类, 方便后续处理.
    ///
    /// @param classInfo 需要判断的 ClassInfo 对象
    /// @return 真实的枚举类型 ClassInfo，或者非枚举时返回 null
    public static ClassInfo _findEnumClass(ClassInfo classInfo) {
        if (classInfo.classKind() == ENUM) {
            return classInfo.isAnonymousClass() ? classInfo.superClass() : classInfo;
        } else {
            return null;
        }
    }

    public static RecordComponentInfo[] _findRecordComponents(ClassInfo classInfo) {
        if (classInfo.classKind() == RECORD) {
            var recordComponents = classInfo.rawClass().getRecordComponents();
            var result = new RecordComponentInfo[recordComponents.length];
            for (int i = 0; i < recordComponents.length; i = i + 1) {
                result[i] = new RecordComponentInfoImpl(recordComponents[i], classInfo);
            }
            return result;
        }
        return new RecordComponentInfo[0];
    }

    /// 判断是否为重写方法
    private static boolean isOverride(MethodInfo methodInfo, MethodInfo superMethod) {
        // 此方法 的判断逻辑实际上是建立在 methodInfo 和 superMethod 一定是 同一条继承链上的,
        // 这样我们能够减少一些判断, 因为 java 在编译期间已经帮我们处理了 比如返回值 此处是无需判断的
        // 注意 在调用此方法之前 我们已经 进行了一部分 检查 比如 static 和 final 检查 此处不再进行

        // 访问权限判断方面, 只需要排除 superMethod 是 private 的情况, 原因如下:
        // 1. private 方法不参与继承, 子类定义同名 private 方法不会构成重写, 而是隐藏 (new) 方法.
        // 2. 其他访问权限 (public、protected、package-private) 重写时, Java 编译器会强制要求子类方法的访问权限
        //    不能比父类方法更严格, 因此不会出现访问权限不合法的重写.
        // 3. 因此, 除了排除 private, 访问权限的合法性已经由编译器保障, 不需要在运行时额外判断。
        if (superMethod.accessModifier() == PRIVATE) {
            return false;
        }
        // 特例 : 需要注意的是, package-private 方法只能被同包内子类重写, 跨包时无法访问和重写.
        // 所以这里需要额外判断一下 包名
        if (superMethod.accessModifier() == PACKAGE_PRIVATE) {
            var p1 = superMethod.declaringClass().rawClass().getPackageName();
            var p2 = methodInfo.declaringClass().rawClass().getPackageName();
            // 不同包, 不能算重写
            if (!p1.equals(p2)) {
                return false;
            }
        }
        // 方法签名也必须相同
        return methodInfo.signature().equals(superMethod.signature());
    }

    public static Class<?>[] _findParameterTypes(MethodInfo methodInfo) {
        // 此处不能直接使用 Method.getParameterTypes(), 因为存在泛型擦除的问题
        var parameterInfos = methodInfo.parameters();
        var parameterTypes = new Class<?>[parameterInfos.length];
        for (int i = 0; i < parameterInfos.length; i = i + 1) {
            parameterTypes[i] = parameterInfos[i].parameterType().rawClass();
        }
        return parameterTypes;
    }

}
