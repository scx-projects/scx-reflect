package dev.scx.reflect.test;

import dev.scx.reflect.ClassInfo;
import dev.scx.reflect.MethodInfo;
import dev.scx.reflect.ScxReflect;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class EdgeCaseAllMethodsTest {

    public static void main(String[] args) {
        testDiamondInterfaceOverrideConflict();
        testClassOverrideInterfaceDefaultMethod();
        testAbstractClassImplementsInterface();
        testEnumConstantAnonymousClass();
        testGenericBridgeMethodElimination();
        testInterfaceSameNameDifferentParams();
        testRecordCanonicalConstructor();
    }

    private static Set<String> methodNames(MethodInfo[] methods) {
        return Arrays.stream(methods).map(MethodInfo::name).collect(Collectors.toSet());
    }

    private static void printMethods(MethodInfo[] methods) {
        for (MethodInfo m : methods) {
            System.out.println(m.declaringClass().rawClass().getSimpleName() + " -> " + m.signature());
        }
    }

    @Test
    public static void testDiamondInterfaceOverrideConflict() {
        ClassInfo info = (ClassInfo) ScxReflect.typeOf(DiamondImpl.class);
        var methods = info.allMethods();
        // 打印结果供人工检查
        printMethods(methods);
        var names = methodNames(methods);
        // foo 必须只出现一次（冲突已由子类解决）
        assertEquals(names.stream().filter("foo"::equals).count(), 1);
    }

    @Test
    public static void testClassOverrideInterfaceDefaultMethod() {
        ClassInfo info = (ClassInfo) ScxReflect.typeOf(Child.class);
        var methods = info.allMethods();
        printMethods(methods);
        // 只保留一个 hello，应该来自 ParentClass
        assertEquals(methods.length, 13);
        assertEquals(Arrays.stream(methods).filter(c -> c.name().equals("hello")).findFirst().orElse(null).declaringClass().rawClass(), ParentClass.class);
    }

    @Test
    public static void testAbstractClassImplementsInterface() {
        ClassInfo info = (ClassInfo) ScxReflect.typeOf(Impl.class);
        var methods = info.allMethods();
        printMethods(methods);
        assertEquals(methods.length, 13);
        assertEquals(Arrays.stream(methods).filter(c -> c.name().equals("work")).findFirst().orElse(null).declaringClass().rawClass(), AbstractFoo.class);
    }

    @Test
    public static void testEnumConstantAnonymousClass() {
        ClassInfo info = (ClassInfo) ScxReflect.typeOf(ComplexEnum.A.getClass());
        var methods = info.allMethods();
        printMethods(methods);
        var runMethod = Arrays.stream(methods).filter(m -> m.name().equals("run")).findFirst();
        assertTrue(runMethod.isPresent());
    }

    @Test
    public static void testGenericBridgeMethodElimination() {
        ClassInfo info = (ClassInfo) ScxReflect.typeOf(GenericImpl.class);
        var methods = info.allMethods();
        printMethods(methods);
        // 不应包含桥接方法，只保留 get
        assertEquals(methods.length, 13);
    }

    @Test
    public static void testInterfaceSameNameDifferentParams() {
        ClassInfo info = (ClassInfo) ScxReflect.typeOf(IFinal.class);
        var methods = info.allMethods();
        printMethods(methods);
        // 应该包含两个 doSomething，不冲突
        var names = methodNames(methods);
        assertTrue(names.contains("doSomething"));
        assertEquals(methods.length, 2);
    }

    @Test
    public static void testRecordCanonicalConstructor() {
        ClassInfo info = (ClassInfo) ScxReflect.typeOf(Person.class);
        var constructor = info.recordConstructor();
        assertEquals(constructor.parameters().length, 2);
        assertEquals(constructor.parameters()[0].name(), "name");
        assertEquals(constructor.parameters()[1].name(), "age");
    }

    // region: 边界类定义

    interface I1 {
        default void foo() {
        }
    }

    interface I2 extends I1 {
        default void foo() {
        }
    }

    interface I3 extends I1 {
        default void foo() {
        }
    }

    interface Diamond extends I2, I3 {
        @Override
        default void foo() {
            I2.super.foo();
        }
    }

    static class DiamondImpl implements Diamond {
        // 如果不实现 foo，会编译失败（必须 disambiguate）
        public void foo() {
        }
    }

    interface ParentIface {
        default void hello() {
        }
    }

    static class ParentClass {
        public void hello() {
        }
    }

    static class Child extends ParentClass implements ParentIface {}

    interface IFoo {
        void work();
    }

    abstract static class AbstractFoo {
        public void work() {
        }
    }

    static class Impl extends AbstractFoo implements IFoo {}

    enum ComplexEnum {
        A {
            @Override
            public void run() {
            }
        }, B;

        public void run() {
        }
    }

    static class GenericBase<T> {
        public T get() {
            return null;
        }
    }

    static class GenericImpl extends GenericBase<String> {
        @Override
        public String get() {
            return "str";
        }
    }

    interface ILeft {
        void doSomething(String s);
    }

    interface IRight {
        void doSomething(int i);
    }

    interface IFinal extends ILeft, IRight {}

    record Person(String name, int age) {}

    // endregion

}
