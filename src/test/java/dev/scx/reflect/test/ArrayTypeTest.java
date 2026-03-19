package dev.scx.reflect.test;

import dev.scx.reflect.ArrayTypeInfo;
import dev.scx.reflect.ScxReflect;
import dev.scx.reflect.TypeInfo;
import dev.scx.reflect.TypeReference;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ArrayTypeTest {

    public static void main(String[] args) {
        test1_raw_reference_array();
        test2_primitive_array();
        test3_multi_dimensional_array();
        test4_generic_array();
        test5_newArray_should_work();
        test6_raw_and_semantically_equivalent_array_should_be_singleton_when_possible();
    }

    @Test
    public static void test1_raw_reference_array() {
        var type = (ArrayTypeInfo) ScxReflect.typeOf(String[].class);

        Assert.assertEquals(type.toString(), "String[]");
        Assert.assertSame(type.componentType(), ScxReflect.typeOf(String.class));
        Assert.assertTrue(type.isRaw());
    }

    @Test
    public static void test2_primitive_array() {
        var type = (ArrayTypeInfo) ScxReflect.typeOf(int[].class);

        Assert.assertEquals(type.toString(), "int[]");
        Assert.assertEquals(type.componentType().toString(), "int");
        Assert.assertTrue(type.isRaw());
    }

    @Test
    public static void test3_multi_dimensional_array() {
        var type = (ArrayTypeInfo) ScxReflect.typeOf(String[][].class);

        Assert.assertEquals(type.toString(), "String[][]");
        Assert.assertEquals(type.componentType().toString(), "String[]");
    }

    @Test
    public static void test4_generic_array() {
        TypeInfo type = ScxReflect.typeOf(new TypeReference<java.util.List<String>[]>() {});
        Assert.assertEquals(type.toString(), "List<String>[]");
    }

    @Test
    public static void test5_newArray_should_work() {
        var type = (ArrayTypeInfo) ScxReflect.typeOf(String[].class);
        var arr = type.newArray(3);

        Assert.assertEquals(arr.getClass(), String[].class);
        Assert.assertEquals(java.lang.reflect.Array.getLength(arr), 3);
    }

    @Test
    public static void test6_raw_and_semantically_equivalent_array_should_be_singleton_when_possible() {
        TypeInfo t1 = ScxReflect.typeOf(String[].class);
        TypeInfo t2 = ScxReflect.typeOf(String[].class);

        Assert.assertSame(t1, t2);
        Assert.assertEquals(t1, t2);
        Assert.assertEquals(t1.hashCode(), t2.hashCode());
    }

}
