package dev.scx.reflect.test;

import dev.scx.reflect.ClassInfo;
import dev.scx.reflect.ScxReflect;
import dev.scx.reflect.TypeReference;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ConstructorsTest {

    public static void main(String[] args) {
        test1_declaredConstructors();
        test2_defaultConstructor_should_be_found();
        test3_defaultConstructor_should_be_null_when_absent();
        test4_recordConstructor_should_be_found();
        test5_memberClass_defaultConstructor_should_be_null_under_current_definition();
        test6_staticNestedClass_defaultConstructor_should_be_found();
    }

    @Test
    public static void test1_declaredConstructors() {
        var type = typeOf(A.class);
        var constructors = type.constructors();
        Assert.assertEquals(constructors.length, 3);
    }

    @Test
    public static void test2_defaultConstructor_should_be_found() {
        var type = typeOf(B.class);
        var c = type.defaultConstructor();
        Assert.assertNotNull(c);
        Assert.assertEquals(c.parameters().length, 0);
    }

    @Test
    public static void test3_defaultConstructor_should_be_null_when_absent() {
        var type = typeOf(C.class);
        Assert.assertNull(type.defaultConstructor());
    }

    @Test
    public static void test4_recordConstructor_should_be_found() {
        var type = (ClassInfo) ScxReflect.typeOf(new TypeReference<R<String>>() {});
        var c = type.recordConstructor();

        Assert.assertNotNull(c);
        Assert.assertEquals(c.parameters().length, 2);
        Assert.assertEquals(c.parameters()[0].parameterType().toString(), "String");
        Assert.assertEquals(c.parameters()[1].parameterType().toString(), "Integer");
    }

    @Test
    public static void test5_memberClass_defaultConstructor_should_be_null_under_current_definition() {
        var type = typeOf(Outer.Inner.class);
        Assert.assertNull(type.defaultConstructor());
    }

    @Test
    public static void test6_staticNestedClass_defaultConstructor_should_be_found() {
        var type = typeOf(Outer.StaticInner.class);
        var c = type.defaultConstructor();

        Assert.assertNotNull(c);
        Assert.assertEquals(c.parameters().length, 0);
    }

    private static ClassInfo typeOf(Class<?> clazz) {
        return (ClassInfo) ScxReflect.typeOf(clazz);
    }

    static class A {
        A() {
        }

        A(String s) {
        }

        private A(Integer i) {
        }
    }

    static class B {
        B() {
        }
    }

    static class C {
        C(String s) {
        }
    }

    record R<T>(T left, Integer right) {
    }

    static class Outer {
        class Inner {
            Inner() {
            }
        }

        static class StaticInner {
            StaticInner() {
            }
        }
    }

}
