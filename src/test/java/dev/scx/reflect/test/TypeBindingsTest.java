package dev.scx.reflect.test;

import dev.scx.reflect.ClassInfo;
import dev.scx.reflect.ScxReflect;
import dev.scx.reflect.TypeReference;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;

public class TypeBindingsTest {

    public static void main(String[] args) {
        test1_getByIndexAndName();
        test2_outerAndInnerBindingsOrder();
        test3_iterator_should_work();
        test4_equals_hashCode_toString();
        test5_same_name_type_variable_should_prefer_inner_scope();
    }

    @Test
    public static void test1_getByIndexAndName() {
        var type = (ClassInfo) ScxReflect.typeOf(new TypeReference<Pair<String, Integer>>() {});
        var bindings = type.bindings();

        Assert.assertEquals(bindings.size(), 2);
        Assert.assertSame(bindings.get(0), ScxReflect.typeOf(String.class));
        Assert.assertSame(bindings.get(1), ScxReflect.typeOf(Integer.class));
        Assert.assertSame(bindings.get("A"), ScxReflect.typeOf(String.class));
        Assert.assertSame(bindings.get("B"), ScxReflect.typeOf(Integer.class));
    }

    @Test
    public static void test2_outerAndInnerBindingsOrder() {
        var type = (ClassInfo) ScxReflect.typeOf(new TypeReference<Outer<String>.Inner<Integer>>() {});
        var bindings = type.allBindings();

        Assert.assertEquals(bindings.size(), 2);
        Assert.assertEquals(bindings.typeVariables()[0].getName(), "T");
        Assert.assertEquals(bindings.typeVariables()[1].getName(), "U");
        Assert.assertSame(bindings.get(0), ScxReflect.typeOf(String.class));
        Assert.assertSame(bindings.get(1), ScxReflect.typeOf(Integer.class));
    }

    @Test
    public static void test3_iterator_should_work() {
        var type = (ClassInfo) ScxReflect.typeOf(new TypeReference<Pair<String, Integer>>() {});
        var bindings = type.bindings();

        var values = new ArrayList<String>();
        for (var e : bindings) {
            values.add(e.getKey().getName() + "=" + e.getValue());
        }

        Assert.assertEquals(values.size(), 2);
        Assert.assertTrue(values.contains("A=String"));
        Assert.assertTrue(values.contains("B=Integer"));
    }

    @Test
    public static void test4_equals_hashCode_toString() {
        var b1 = ((ClassInfo) ScxReflect.typeOf(new TypeReference<Pair<String, Integer>>() {})).bindings();
        var b2 = ((ClassInfo) ScxReflect.typeOf(new TypeReference<Pair<String, Integer>>() {})).bindings();

        Assert.assertEquals(b1, b2);
        Assert.assertEquals(b1.hashCode(), b2.hashCode());
        Assert.assertEquals(b1.toString(), "{A=String, B=Integer}");
    }

    @Test
    public static void test5_same_name_type_variable_should_prefer_inner_scope() {
        var type = (ClassInfo) ScxReflect.typeOf(new TypeReference<SameNameOuter<String>.SameNameInner<Integer>>() {});
        var bindings = type.allBindings();

        Assert.assertEquals(bindings.size(), 2);
        Assert.assertSame(bindings.get(0), ScxReflect.typeOf(String.class));
        Assert.assertSame(bindings.get(1), ScxReflect.typeOf(Integer.class));
        Assert.assertSame(bindings.get("T"), ScxReflect.typeOf(Integer.class));
    }

    static class Pair<A, B> {
    }

    static class Outer<T> {
        class Inner<U> {
        }
    }

    static class SameNameOuter<T> {
        class SameNameInner<T> {
        }
    }

}
