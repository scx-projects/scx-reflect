package dev.scx.reflect.test;

import dev.scx.reflect.ClassInfo;
import dev.scx.reflect.MethodInfo;
import dev.scx.reflect.ScxReflect;
import dev.scx.reflect.TypeReference;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class DeclaredMethodsTest {

    public static void main(String[] args) {
        test1_methods_should_only_return_declared_methods();
        test2_bridge_method_should_be_filtered();
        test3_interface_default_method_should_be_present();
        test4_method_flags_should_be_correct();
        test5_generic_return_type_should_resolve();
    }

    @Test
    public static void test1_methods_should_only_return_declared_methods() {
        var type = typeOf(B.class);
        var methods = type.methods();

        assertMethodNames(methods, "b", "c");
    }

    @Test
    public static void test2_bridge_method_should_be_filtered() {
        var type = typeOf(StringBox.class);
        var methods = type.methods();

        Assert.assertEquals(methods.length, 1);
        Assert.assertEquals(methods[0].name(), "get");
        Assert.assertEquals(methods[0].returnType().toString(), "String");
    }

    @Test
    public static void test3_interface_default_method_should_be_present() {
        var type = typeOf(I.class);
        var methods = type.methods();

        Assert.assertEquals(methods.length, 2);
        var defaultMethod = Arrays.stream(methods).filter(m -> m.name().equals("b")).findFirst().orElseThrow();
        Assert.assertTrue(defaultMethod.isDefault());
        Assert.assertFalse(defaultMethod.isAbstract());
    }

    @Test
    public static void test4_method_flags_should_be_correct() {
        var type = typeOf(C.class);

        var staticMethod = findMethod(type.methods(), "s");
        var finalMethod = findMethod(type.methods(), "f");
        var abstractMethod = findMethod(type.methods(), "a");

        Assert.assertTrue(staticMethod.isStatic());
        Assert.assertTrue(finalMethod.isFinal());
        Assert.assertTrue(abstractMethod.isAbstract());
    }

    @Test
    public static void test5_generic_return_type_should_resolve() {
        var type = (ClassInfo) ScxReflect.typeOf(new TypeReference<G<String>>() {});
        var method = findMethod(type.methods(), "get");

        Assert.assertEquals(method.returnType().toString(), "String");
    }

    private static ClassInfo typeOf(Class<?> clazz) {
        return (ClassInfo) ScxReflect.typeOf(clazz);
    }

    private static void assertMethodNames(MethodInfo[] methods, String... expected) {
        var actual = Arrays.stream(methods).map(MethodInfo::name).collect(Collectors.toSet());
        Assert.assertEquals(actual, Set.of(expected));
    }

    private static MethodInfo findMethod(MethodInfo[] methods, String name) {
        return Arrays.stream(methods).filter(m -> m.name().equals(name)).findFirst().orElseThrow();
    }

    static class A {
        void a() {
        }
    }

    static class B extends A {
        void b() {
        }

        void c() {
        }
    }

    interface Box<T> {
        T get();
    }

    static class StringBox implements Box<String> {
        @Override
        public String get() {
            return "";
        }
    }

    interface I {
        void a();

        default void b() {
        }
    }

    abstract static class C {
        static void s() {
        }

        final void f() {
        }

        abstract void a();
    }

    static class G<T> {
        T get() {
            return null;
        }
    }

}
