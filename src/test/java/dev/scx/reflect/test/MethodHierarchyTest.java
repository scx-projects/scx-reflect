package dev.scx.reflect.test;

import dev.scx.reflect.ClassInfo;
import dev.scx.reflect.MethodInfo;
import dev.scx.reflect.ScxReflect;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class MethodHierarchyTest {

    public static void main(String[] args) {
        test1_basicOverrideChain();
        test2_privateMethodShouldNotBeSuperMethod();
        test3_classMethodShouldDominateInterfaceMethodInAllMethods();
        test4_interfaceChain();
        test5_staticMethodShouldNotParticipateInOverride();
    }

    @Test
    public static void test1_basicOverrideChain() {
        var method = findMethod(typeOf(C.class).methods(), "m");

        assertDeclaringClasses(method.superMethods(), B.class);
        assertDeclaringClasses(method.allSuperMethods(), B.class, A.class);
    }

    @Test
    public static void test2_privateMethodShouldNotBeSuperMethod() {
        var method = findMethod(typeOf(WW.class).methods(), "x");

        Assert.assertEquals(method.superMethods().length, 0);
        Assert.assertEquals(method.allSuperMethods().length, 0);
    }

    @Test
    public static void test3_classMethodShouldDominateInterfaceMethodInAllMethods() {
        var methods = Arrays.stream(typeOf(K.class).allMethods())
            .filter(m -> m.name().equals("hasXXX"))
            .toList();

        Assert.assertEquals(methods.size(), 1);
        Assert.assertEquals(methods.get(0).declaringClass().rawClass(), J.class);
    }

    @Test
    public static void test4_interfaceChain() {
        var method = findMethod(typeOf(C7.class).methods(), "m");
        assertDeclaringClasses(method.allSuperMethods(), B7.class, A7.class);
    }

    @Test
    public static void test5_staticMethodShouldNotParticipateInOverride() {
        var method = findMethod(typeOf(SS.class).methods(), "s");
        Assert.assertEquals(method.superMethods().length, 0);
        Assert.assertEquals(method.allSuperMethods().length, 0);
    }

    private static ClassInfo typeOf(Class<?> clazz) {
        return (ClassInfo) ScxReflect.typeOf(clazz);
    }

    private static MethodInfo findMethod(MethodInfo[] methods, String name) {
        return Arrays.stream(methods).filter(m -> m.name().equals(name)).findFirst().orElseThrow();
    }

    private static void assertDeclaringClasses(MethodInfo[] methods, Class<?>... expected) {
        var actual = Arrays.stream(methods).map(m -> m.declaringClass().rawClass()).collect(Collectors.toSet());
        Assert.assertEquals(actual, Set.of(expected));
    }

    static class A {
        void m() {
        }
    }

    static class B extends A {
        @Override
        void m() {
        }
    }

    static class C extends B {
        @Override
        void m() {
        }
    }

    static class W {
        private void x() {
        }
    }

    static class WW extends W {
        public void x() {
        }
    }

    interface H {
        void hasXXX();
    }

    abstract static class J {
        public void hasXXX() {
        }
    }

    static class K extends J implements H {
    }

    interface A7 {
        void m();
    }

    interface B7 extends A7 {
        default void m() {
        }
    }

    interface C7 extends B7 {
        void m();
    }

    static class S {
        static void s() {
        }
    }

    static class SS extends S {
        static void s() {
        }
    }

}
