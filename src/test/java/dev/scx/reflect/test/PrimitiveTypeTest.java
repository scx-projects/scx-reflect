package dev.scx.reflect.test;

import dev.scx.reflect.PrimitiveTypeInfo;
import dev.scx.reflect.ScxReflect;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PrimitiveTypeTest {

    public static void main(String[] args) {
        test1_int();
        test2_boolean();
        test3_void();
        test4_singleton_identity();
        test5_primitive_should_not_equal_wrapper();
    }

    @Test
    public static void test1_int() {
        var type = (PrimitiveTypeInfo) ScxReflect.typeOf(int.class);
        Assert.assertEquals(type.toString(), "int");
        Assert.assertTrue(type.isRaw());
        Assert.assertEquals(type.rawClass(), int.class);
    }

    @Test
    public static void test2_boolean() {
        var type = (PrimitiveTypeInfo) ScxReflect.typeOf(boolean.class);
        Assert.assertEquals(type.toString(), "boolean");
        Assert.assertEquals(type.rawClass(), boolean.class);
    }

    @Test
    public static void test3_void() {
        var type = (PrimitiveTypeInfo) ScxReflect.typeOf(void.class);
        Assert.assertEquals(type.toString(), "void");
        Assert.assertEquals(type.rawClass(), void.class);
    }

    @Test
    public static void test4_singleton_identity() {
        var t1 = ScxReflect.typeOf(int.class);
        var t2 = ScxReflect.typeOf(int.class);

        Assert.assertSame(t1, t2);
    }

    @Test
    public static void test5_primitive_should_not_equal_wrapper() {
        var p = ScxReflect.typeOf(int.class);
        var w = ScxReflect.typeOf(Integer.class);

        Assert.assertNotEquals(p, w);
    }

}
