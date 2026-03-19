package dev.scx.reflect.test;

import dev.scx.reflect.*;
import org.testng.annotations.Test;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.*;

public class RecursionTest {

    public static void main(String[] args) {
        test1_recursive_bound_should_resolve_and_not_crash();
        test2_recursive_field_should_resolve_to_same_semantic_type();
        test3_single_level_member_inner_should_merge_outer_bindings();
        test4_multi_level_member_inner_should_merge_all_outer_bindings();
        test5_recursive_bound_should_be_singleton_across_repeated_typeOf();
        test6_recursive_bound_toString_hashCode_equals_should_be_stable();
        test7_recursive_member_inner_should_not_stack_overflow();
        test8_nested_member_type_should_keep_identity_across_repeated_resolution();
    }

    /// 递归上界 T extends Node<T>
    ///
    /// 预期:
    /// - 能成功解析
    /// - 不应抛异常
    /// - allFields() 可正常展开
    /// - 字段名应完整
    @Test
    public static void test1_recursive_bound_should_resolve_and_not_crash() {
        Type type = Node.class.getTypeParameters()[0].getBounds()[0];

        var typeInfo = (ClassInfo) ScxReflect.typeOf(type);
        assertNotNull(typeInfo);

        FieldInfo[] fields = typeInfo.allFields();
        assertEquals(fields.length, 4);

        var fieldNames = Arrays.stream(fields).map(FieldInfo::name).toList();
        assertTrue(fieldNames.contains("node"));
        assertTrue(fieldNames.contains("next"));
        assertTrue(fieldNames.contains("children"));
        assertTrue(fieldNames.contains("parent"));
    }

    /// S<String>.ccc 的字段类型应当解析为 List<String>
    ///
    /// 预期:
    /// - S<String> 的字段 ccc 类型与直接 typeOf(List<String>) 为同一实例
    @Test
    public static void test2_recursive_field_should_resolve_to_same_semantic_type() {
        var sType = (ClassInfo) ScxReflect.typeOf(new TypeReference<S<String>>() {});
        var listType = (ClassInfo) ScxReflect.typeOf(new TypeReference<List<String>>() {});

        assertEquals(sType.fields().length, 1);
        assertEquals(sType.fields()[0].name(), "ccc");
        assertSame(sType.fields()[0].fieldType(), listType);
    }

    /// 单层嵌套:
    /// Outer<String>.Inner<Integer>
    ///
    /// 预期:
    /// - allBindings() 应为:
    ///   T -> String
    ///   U -> Integer
    /// - 顺序: 外层在前, 当前类在后
    /// - 字段 b / c 的类型应分别为 Integer / String
    @Test
    public static void test3_single_level_member_inner_should_merge_outer_bindings() {
        var typeInfo = ScxReflect.typeOf(new TypeReference<Outer<String>.Inner<Integer>>() {});
        var classInfo = (ClassInfo) typeInfo;
        var bindings = classInfo.allBindings();

        assertBindings(bindings, "T", String.class, 0);
        assertBindings(bindings, "U", Integer.class, 1);

        var fields = classInfo.allFields();
        assertEquals(fields.length, 2);

        assertFieldType(fields, "b", Integer.class);
        assertFieldType(fields, "c", String.class);
    }

    /// 多层嵌套:
    /// Outer<String>.Middle<Integer>.Deep<Long>
    ///
    /// 预期:
    /// - allBindings() 应为:
    ///   T -> String
    ///   U -> Integer
    ///   V -> Long
    /// - 顺序: 外层在前, 当前类在后
    /// - 字段 f / g 的类型应分别为 Long / Integer
    @Test
    public static void test4_multi_level_member_inner_should_merge_all_outer_bindings() {
        var typeInfo = ScxReflect.typeOf(new TypeReference<Outer<String>.Middle<Integer>.Deep<Long>>() {});
        var classInfo = (ClassInfo) typeInfo;
        var bindings = classInfo.allBindings();

        assertBindings(bindings, "T", String.class, 0);
        assertBindings(bindings, "U", Integer.class, 1);
        assertBindings(bindings, "V", Long.class, 2);

        var fields = classInfo.allFields();
        assertEquals(fields.length, 2);

        assertFieldType(fields, "f", Long.class);
        assertFieldType(fields, "g", Integer.class);
    }

    /// 同一个递归上界重复解析, 应得到同一实例
    @Test
    public static void test5_recursive_bound_should_be_singleton_across_repeated_typeOf() {
        Type type = Node.class.getTypeParameters()[0].getBounds()[0];

        TypeInfo t1 = ScxReflect.typeOf(type);
        TypeInfo t2 = ScxReflect.typeOf(type);

        assertSame(t1, t2);
        assertEquals(t1, t2);
        assertEquals(t1.hashCode(), t2.hashCode());
    }

    /// 递归上界的 toString / hashCode / equals 不应爆栈, 且应稳定
    @Test
    public static void test6_recursive_bound_toString_hashCode_equals_should_be_stable() {
        Type type = Node.class.getTypeParameters()[0].getBounds()[0];

        TypeInfo t1 = ScxReflect.typeOf(type);
        TypeInfo t2 = ScxReflect.typeOf(type);

        assertNotNull(t1.toString());
        assertEquals(t1.hashCode(), t2.hashCode());
        assertEquals(t1, t2);
        assertEquals(t1, t1);
    }

    /// 递归成员内部类:
    /// RecursiveOuter<T extends RecursiveOuter<T>.Inner>
    ///
    /// 预期:
    /// - 能成功解析
    /// - toString / hashCode / equals 不应爆栈
    @Test
    public static void test7_recursive_member_inner_should_not_stack_overflow() {
        Type type = RecursiveOuter.class.getTypeParameters()[0].getBounds()[0];

        TypeInfo typeInfo = ScxReflect.typeOf(type);
        assertNotNull(typeInfo);

        assertNotNull(typeInfo.toString());
        typeInfo.hashCode();
        assertEquals(typeInfo, typeInfo);
    }

    /// 多层成员嵌套类型重复解析, 应保持 identity 一致
    @Test
    public static void test8_nested_member_type_should_keep_identity_across_repeated_resolution() {
        TypeInfo t1 = ScxReflect.typeOf(new TypeReference<Outer<String>.Middle<Integer>.Deep<Long>>() {});
        TypeInfo t2 = ScxReflect.typeOf(new TypeReference<Outer<String>.Middle<Integer>.Deep<Long>>() {});

        assertSame(t1, t2);
        assertEquals(t1, t2);
        assertEquals(t1.hashCode(), t2.hashCode());
        assertEquals(t1.toString(), t2.toString());
    }

    // =========================
    // helper
    // =========================

    private static void assertBindings(TypeBindings bindings, String variableName, Class<?> expectedType, int expectedIndex) {
        assertNotNull(bindings);
        assertTrue(bindings.size() > expectedIndex);

        assertEquals(bindings.typeVariables()[expectedIndex].getName(), variableName);
        assertSame(bindings.get(variableName), ScxReflect.typeOf(expectedType));
        assertSame(bindings.get(expectedIndex), ScxReflect.typeOf(expectedType));
    }

    private static void assertFieldType(FieldInfo[] fields, String fieldName, Class<?> expectedRawClass) {
        var field = Arrays.stream(fields)
            .filter(f -> f.name().equals(fieldName))
            .findFirst()
            .orElseThrow(() -> new AssertionError("未找到字段: " + fieldName));

        assertSame(field.fieldType(), ScxReflect.typeOf(expectedRawClass));
    }

    // =========================
    // fixtures
    // =========================

    public static class Outer<T> {

        T a;

        public class Inner<U> {
            U b;
            T c;
        }

        public class Middle<U> {

            U d;
            T e;

            public class Deep<V> {
                V f;
                U g;
            }
        }
    }

    public static class Node<T extends Node<T>> {
        public T node;
        public Node<T> next;
        public List<Node<T>> children;
        public List<T> parent;
    }

    public static class S<T> {
        public List<T> ccc;
    }

    public static class RecursiveOuter<T extends RecursiveOuter<T>.Inner> {
        public class Inner {
        }
    }

}
