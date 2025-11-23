package dev.scx.reflect.test;

import dev.scx.reflect.ClassInfo;
import dev.scx.reflect.FieldInfo;
import dev.scx.reflect.ScxReflect;
import dev.scx.reflect.TypeReference;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.reflect.Type;
import java.util.List;

public class RecursionTest {

    public static void main(String[] args) {
        test1();
        test2();
    }

    @Test
    public static void test1() {
        Type type = Node.class.getTypeParameters()[0].getBounds()[0];
        var type1 = (ClassInfo) ScxReflect.typeOf(type);
        var type2 = ScxReflect.typeOf(type);
        Assert.assertEquals(type1, type2);
        FieldInfo[] fieldInfos = type1.allFields();
    }

    @Test
    public static void test2() {
        var type1 = (ClassInfo) ScxReflect.typeOf(new TypeReference<S<String>>() {});
        var type2 = (ClassInfo) ScxReflect.typeOf(new TypeReference<List<String>>() {});
        Assert.assertTrue(type1.fields()[0].fieldType() == type2);
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

}
