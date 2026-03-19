package dev.scx.reflect.test;

import dev.scx.reflect.ClassInfo;
import dev.scx.reflect.FieldInfo;
import dev.scx.reflect.ScxReflect;
import dev.scx.reflect.TypeReference;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class DeclaredFieldsTest {

    public static void main(String[] args) {
        test1_fields_should_only_return_declared_fields();
        test2_fields_should_preserve_shadowed_field();
        test3_generic_field_should_resolve();
        test4_array_and_primitive_fields_should_resolve();
    }

    @Test
    public static void test1_fields_should_only_return_declared_fields() {
        var type = typeOf(B.class);
        var fields = type.fields();

        Assert.assertEquals(fields.length, 2);
        assertFieldNames(fields, "b", "c");
    }

    @Test
    public static void test2_fields_should_preserve_shadowed_field() {
        var type = typeOf(C.class);
        var fields = type.fields();

        Assert.assertEquals(fields.length, 1);
        Assert.assertEquals(fields[0].name(), "x");
        Assert.assertSame(fields[0].fieldType(), ScxReflect.typeOf(String.class));
    }

    @Test
    public static void test3_generic_field_should_resolve() {
        var type = (ClassInfo) ScxReflect.typeOf(new TypeReference<G<String>>() {});
        var fields = type.fields();

        Assert.assertEquals(fields.length, 2);
        assertFieldType(fields, "t", "String");
        assertFieldType(fields, "list", "List<String>");
    }

    @Test
    public static void test4_array_and_primitive_fields_should_resolve() {
        var type = typeOf(H.class);
        var fields = type.fields();

        assertFieldType(fields, "a", "String[]");
        assertFieldType(fields, "b", "int");
    }

    private static ClassInfo typeOf(Class<?> clazz) {
        return (ClassInfo) ScxReflect.typeOf(clazz);
    }

    private static void assertFieldNames(FieldInfo[] fields, String... expected) {
        var actual = Arrays.stream(fields).map(FieldInfo::name).collect(Collectors.toSet());
        Assert.assertEquals(actual, Set.of(expected));
    }

    private static void assertFieldType(FieldInfo[] fields, String fieldName, String expectedType) {
        var field = Arrays.stream(fields).filter(f -> f.name().equals(fieldName)).findFirst().orElseThrow();
        Assert.assertEquals(field.fieldType().toString(), expectedType);
    }

    static class A {
        Integer a;
        String x;
    }

    static class B extends A {
        Long b;
        Double c;
    }

    static class C extends A {
        String x;
    }

    static class G<T> {
        T t;
        java.util.List<T> list;
    }

    static class H {
        String[] a;
        int b;
    }

}
