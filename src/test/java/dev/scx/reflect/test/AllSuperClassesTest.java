package dev.scx.reflect.test;

import dev.scx.reflect.ClassInfo;
import dev.scx.reflect.ScxReflect;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class AllSuperClassesTest {

    public static void main(String[] args) {
        test1_basicClassChain();
        test2_parameterizedSuperclassChain();
        test3_interfaceShouldHaveNoSuperClasses();
        test4_findSuperType_should_find_parameterized_superclass();
        test5_selfShouldNotAppearInAllSuperClasses();
    }

    @Test
    public static void test1_basicClassChain() {
        var type = typeOf(C.class);
        assertAllSuperClasses(type, B.class, A.class, Object.class);
    }

    @Test
    public static void test2_parameterizedSuperclassChain() {
        var type = (ClassInfo) ScxReflect.typeOf(new dev.scx.reflect.TypeReference<Child>() {});
        var superClasses = type.allSuperClasses();

        Assert.assertEquals(superClasses.length, 3);
        Assert.assertEquals(superClasses[0].toString(), "AllSuperClassesTest.Parent<String>");
        Assert.assertEquals(superClasses[1].rawClass(), Base.class);
        Assert.assertEquals(superClasses[2].rawClass(), Object.class);
    }

    @Test
    public static void test3_interfaceShouldHaveNoSuperClasses() {
        var type = typeOf(I.class);
        Assert.assertEquals(type.allSuperClasses().length, 0);
        Assert.assertNull(type.superClass());
    }

    @Test
    public static void test4_findSuperType_should_find_parameterized_superclass() {
        var type = typeOf(Child.class);
        var superType = type.findSuperType(Parent.class);

        Assert.assertNotNull(superType);
        Assert.assertEquals(superType.toString(), "AllSuperClassesTest.Parent<String>");
        Assert.assertSame(superType.bindings().get(0), ScxReflect.typeOf(String.class));
    }

    @Test
    public static void test5_selfShouldNotAppearInAllSuperClasses() {
        var type = typeOf(C.class);
        var set = Arrays.stream(type.allSuperClasses()).map(ClassInfo::rawClass).collect(Collectors.toSet());
        Assert.assertFalse(set.contains(C.class));
    }

    private static ClassInfo typeOf(Class<?> clazz) {
        return (ClassInfo) ScxReflect.typeOf(clazz);
    }

    private static void assertAllSuperClasses(ClassInfo type, Class<?>... expected) {
        var actual = Arrays.stream(type.allSuperClasses()).map(ClassInfo::rawClass).collect(Collectors.toSet());
        Assert.assertEquals(actual, Set.of(expected));
    }

    static class A {
    }

    static class B extends A {
    }

    static class C extends B {
    }

    interface I {
    }

    static class Base {
    }

    static class Parent<T> extends Base {
    }

    static class Child extends Parent<String> {
    }

}
