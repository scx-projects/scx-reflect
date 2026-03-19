package dev.scx.reflect.test;

import dev.scx.reflect.*;
import org.testng.Assert;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ManyClassTest {

    /// 你自己的 jar 目录
    public static final Path CLASS_FOLDER = Path.of("XXX");

    /// 并发线程数
    public static final int THREAD_COUNT = Math.max(2, Runtime.getRuntime().availableProcessors());

    /// 并发重复轮数
    public static final int CONCURRENT_ROUNDS = 3;

    public static void main(String[] args) throws Exception {
        test1_singleThread_smoke();
        test2_concurrent_smoke();
    }

    /// 单线程大规模 smoke test
    public static void test1_singleThread_smoke() throws Exception {
        var classList = findClassFromFileFolder(CLASS_FOLDER);
        var result = runSingleThread(classList);

        printResult("single-thread", result);

        Assert.assertTrue(result.totalClasses > 0, "未扫描到任何类, 请检查 CLASS_FOLDER 配置");
        Assert.assertEquals(
            result.failures.size(),
            0,
            buildFailureMessage("单线程 smoke 存在失败", result.failures)
        );
    }

    /// 并发大规模 smoke test
    public static void test2_concurrent_smoke() throws Exception {
        var classList = findClassFromFileFolder(CLASS_FOLDER);
        var result = runConcurrent(classList, THREAD_COUNT, CONCURRENT_ROUNDS);

        printResult("concurrent", result);

        Assert.assertTrue(result.totalClasses > 0, "未扫描到任何类, 请检查 CLASS_FOLDER 配置");
        Assert.assertEquals(
            result.failures.size(),
            0,
            buildFailureMessage("并发 smoke 存在失败", result.failures)
        );
    }

    // =========================
    // run
    // =========================

    public static RunResult runSingleThread(List<Class<?>> classList) {
        var result = new RunResult();
        result.totalClasses = classList.size();

        for (var loadedClass : classList) {
            inspectOneClass(loadedClass, result);
        }

        return result;
    }

    public static RunResult runConcurrent(List<Class<?>> classList, int threadCount, int rounds) throws Exception {
        var result = new RunResult();
        result.totalClasses = classList.size();

        var pool = Executors.newFixedThreadPool(threadCount);
        try {
            List<Callable<Void>> tasks = new ArrayList<>();

            for (int round = 0; round < rounds; round = round + 1) {
                for (var loadedClass : classList) {
                    tasks.add(() -> {
                        inspectOneClass(loadedClass, result);
                        return null;
                    });
                }
            }

            List<Future<Void>> futures = pool.invokeAll(tasks);

            for (var future : futures) {
                future.get();
            }
        } finally {
            pool.shutdown();
            pool.awaitTermination(1, TimeUnit.MINUTES);
        }

        return result;
    }

    // =========================
    // core inspect
    // =========================

    public static void inspectOneClass(Class<?> loadedClass, RunResult result) {
        try {
            // 入口 1: typeOf(Class)
            TypeInfo type = ScxReflect.typeOf(loadedClass);

            // 基础稳定性
            type.toString();
            type.hashCode();
            type.equals(type);

            if (type instanceof ClassInfo c) {
                inspectClassInfo(c);
            }

            result.successes.incrementAndGet();
        } catch (NoClassDefFoundError | TypeNotPresentException e) {
            result.skipped.incrementAndGet();
        } catch (LinkageError e) {
            // 例如:
            // - IncompatibleClassChangeError
            // - ClassFormatError
            // - UnsupportedClassVersionError
            // - VerifyError
            // 这类通常说明外部 class/jar 本身不自洽或运行环境不匹配,
            // 不一定是 ScxReflect 的问题, 在 smoke test 中直接跳过.
            result.skipped.incrementAndGet();
        } catch (IllegalArgumentException e) {
            var msg = e.getMessage();
            if (msg != null && (
                msg.contains("Unmatched bit position")
                    || msg.contains("class file format")
                    || msg.contains("location FIELD")
                    || msg.contains("location METHOD")
                    || msg.contains("location CLASS")
            )) {
                result.skipped.incrementAndGet();
            } else {
                result.failures.add(new Failure(loadedClass.getName(), e));
            }
        } catch (Throwable e) {
            result.failures.add(new Failure(loadedClass.getName(), e));
        }
    }

    public static void inspectClassInfo(ClassInfo c) {
        // 自身基本属性
        c.rawClass();
        c.name();
        c.classKind();
        c.accessModifier();
        c.isStatic();
        c.isFinal();
        c.isAbstract();
        c.isAnonymousClass();
        c.isMemberClass();
        c.isLocalClass();
        c.isRaw();

        // 结构
        c.bindings();
        c.declaringClass();
        c.superClass();
        c.interfaces();
        c.allBindings();
        c.allSuperClasses();
        c.allInterfaces();
        c.findSuperType(Object.class);

        // 枚举/record 辅助
        c.enumClass();
        c.defaultConstructor();
        c.recordConstructor();

        // 成员
        inspectConstructors(c.constructors());
        inspectFields(c.fields());
        inspectMethods(c.methods());
        inspectRecordComponents(c.recordComponents());

        // allXXX
        inspectFields(c.allFields());
        inspectMethods(c.allMethods());

        // 值语义稳定性
        c.toString();
        c.hashCode();
        c.equals(c);
    }

    public static void inspectConstructors(ConstructorInfo[] constructors) {
        for (var constructor : constructors) {
            constructor.rawConstructor();
            constructor.accessModifier();
            constructor.declaringClass();
            constructor.parameters();
            constructor.toString();
            constructor.hashCode();
            constructor.equals(constructor);

            var parameters = constructor.parameters();
            for (var parameter : parameters) {
                parameter.name();
                parameter.parameterType();
                parameter.toString();
                parameter.hashCode();
                parameter.equals(parameter);

                var type = parameter.parameterType();
                type.toString();
                type.hashCode();
                type.equals(type);
            }
        }
    }

    public static void inspectFields(FieldInfo[] fields) {
        for (var field : fields) {
            field.rawField();
            field.name();
            field.accessModifier();
            field.declaringClass();
            field.fieldType();
            field.isStatic();
            field.isFinal();
            field.toString();
            field.hashCode();
            field.equals(field);

            var fieldType = field.fieldType();
            fieldType.toString();
            fieldType.hashCode();
            fieldType.equals(fieldType);
        }
    }

    public static void inspectMethods(MethodInfo[] methods) {
        for (var method : methods) {
            method.rawMethod();
            method.name();
            method.accessModifier();
            method.declaringClass();
            method.parameters();
            method.returnType();
            method.signature();

            method.isStatic();
            method.isFinal();
            method.isAbstract();
            method.isDefault();
            method.isNative();

            method.superMethods();
            method.allSuperMethods();

            method.toString();
            method.hashCode();
            method.equals(method);

            var returnType = method.returnType();
            returnType.toString();
            returnType.hashCode();
            returnType.equals(returnType);

            var parameters = method.parameters();
            for (var parameter : parameters) {
                parameter.name();
                parameter.parameterType();
                parameter.toString();
                parameter.hashCode();
                parameter.equals(parameter);

                var parameterType = parameter.parameterType();
                parameterType.toString();
                parameterType.hashCode();
                parameterType.equals(parameterType);
            }
        }
    }

    public static void inspectRecordComponents(RecordComponentInfo[] recordComponents) {
        for (var recordComponent : recordComponents) {
            recordComponent.name();
            recordComponent.declaringClass();
            recordComponent.recordComponentType();
            recordComponent.toString();
            recordComponent.hashCode();
            recordComponent.equals(recordComponent);

            var type = recordComponent.recordComponentType();
            type.toString();
            type.hashCode();
            type.equals(type);
        }
    }

    // =========================
    // scan
    // =========================

    public static List<Class<?>> findClassFromFileFolder(Path fileFolder) throws IOException {
        if (!Files.exists(fileFolder)) {
            return List.of();
        }

        var result = new ArrayList<Class<?>>();

        var jars = Files.walk(fileFolder)
            .filter(path -> path.toString().endsWith(".jar"))
            .sorted(Comparator.comparing(Path::toString))
            .toList();

        URL[] urls = jars.stream()
            .map(path -> {
                try {
                    return path.toUri().toURL();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            })
            .toArray(URL[]::new);

        try (var classLoader = new URLClassLoader(urls, ManyClassTest.class.getClassLoader())) {
            for (var jarPath : jars) {
                try (var jarFile = new JarFile(jarPath.toFile())) {
                    var entries = jarFile.stream()
                        .filter(entry -> !entry.isDirectory())
                        .filter(entry -> entry.getName().endsWith(".class"))
                        .toList();

                    for (JarEntry entry : entries) {
                        var className = entry.getName()
                            .replace('/', '.')
                            .replace(".class", "");

                        try {
                            var c = Class.forName(className, false, classLoader);
                            result.add(c);
                        } catch (Throwable ignored) {
                            // 跳过无法加载的类
                        }
                    }
                } catch (IOException ignored) {
                    // 跳过无法读取的 jar
                }
            }
        }

        // 去重 + 稳定顺序
        return result.stream()
            .distinct()
            .sorted(Comparator.comparing(Class::getName))
            .toList();
    }

    // =========================
    // util
    // =========================

    public static void printResult(String label, RunResult result) {
        System.out.println("===== " + label + " =====");
        System.out.println("总类数: " + result.totalClasses);
        System.out.println("成功数: " + result.successes.get());
        System.out.println("跳过数: " + result.skipped.get());
        System.out.println("失败数: " + result.failures.size());

        if (!result.failures.isEmpty()) {
            System.out.println("----- failures -----");
            for (var failure : result.failures) {
                System.out.println(failure.className);
                failure.throwable.printStackTrace(System.out);
            }
        }
    }

    public static String buildFailureMessage(String prefix, ConcurrentLinkedQueue<Failure> failures) {
        if (failures.isEmpty()) {
            return prefix;
        }

        var sb = new StringBuilder(prefix);
        sb.append("，失败数量=").append(failures.size());

        int count = 0;
        for (var failure : failures) {
            if (count >= 10) {
                sb.append(" ...");
                break;
            }
            sb.append("\n - ").append(failure.className)
                .append(" : ").append(failure.throwable.getClass().getName())
                .append(" -> ").append(failure.throwable.getMessage());
            count = count + 1;
        }

        return sb.toString();
    }

    // =========================
    // data
    // =========================

    public static final class RunResult {
        public int totalClasses;
        public final java.util.concurrent.atomic.AtomicInteger successes = new java.util.concurrent.atomic.AtomicInteger();
        public final java.util.concurrent.atomic.AtomicInteger skipped = new java.util.concurrent.atomic.AtomicInteger();
        public final ConcurrentLinkedQueue<Failure> failures = new ConcurrentLinkedQueue<>();
    }

    public record Failure(String className, Throwable throwable) {
    }

}
