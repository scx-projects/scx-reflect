package dev.scx.reflect.test;

import dev.scx.reflect.ClassInfo;
import dev.scx.reflect.ClassKind;
import dev.scx.reflect.ScxReflect;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ClassInfoFlagsTest {

    public static void main(String[] args) {
        test1_normalClass();
        test2_interface();
        test3_annotation();
        test4_enum();
        test5_record();
        test6_memberAndStaticNestedClass();
        test7_anonymousAndLocalClass();
    }

    @Test
    public static void test1_normalClass() {
        var type = typeOf(A.class);
        Assert.assertEquals(type.classKind(), ClassKind.CLASS);
        Assert.assertFalse(type.isAbstract());
        Assert.assertFalse(type.isFinal());
        Assert.assertTrue(type.isMemberClass());
    }

    @Test
    public static void test2_interface() {
        var type = typeOf(I.class);
        Assert.assertEquals(type.classKind(), ClassKind.INTERFACE);
        Assert.assertTrue(type.isAbstract());
    }

    @Test
    public static void test3_annotation() {
        var type = typeOf(Anno.class);
        Assert.assertEquals(type.classKind(), ClassKind.ANNOTATION);
    }

    @Test
    public static void test4_enum() {
        var type = typeOf(E.class);
        Assert.assertEquals(type.classKind(), ClassKind.ENUM);
    }

    @Test
    public static void test5_record() {
        var type = typeOf(R.class);
        Assert.assertEquals(type.classKind(), ClassKind.RECORD);
    }

    @Test
    public static void test6_memberAndStaticNestedClass() {
        var member = typeOf(Outer.Inner.class);
        var nested = typeOf(Outer.StaticInner.class);

        Assert.assertTrue(member.isMemberClass());
        Assert.assertFalse(member.isStatic());

        Assert.assertTrue(nested.isMemberClass());
        Assert.assertTrue(nested.isStatic());
    }

    @Test
    public static void test7_anonymousAndLocalClass() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
            }
        };
        class Local {
        }

        var anonymous = typeOf(r.getClass());
        var local = typeOf(Local.class);

        Assert.assertTrue(anonymous.isAnonymousClass());
        Assert.assertTrue(local.isLocalClass());
    }

    private static ClassInfo typeOf(Class<?> clazz) {
        return (ClassInfo) ScxReflect.typeOf(clazz);
    }

    static class A {
    }

    interface I {
    }

    @interface Anno {
    }

    enum E {
        A
    }

    record R(String x) {
    }

    static class Outer {
        class Inner {
        }

        static class StaticInner {
        }
    }

}
