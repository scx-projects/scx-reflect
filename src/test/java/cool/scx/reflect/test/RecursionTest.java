package cool.scx.reflect.test;

import cool.scx.reflect.ClassInfo;
import cool.scx.reflect.FieldInfo;
import cool.scx.reflect.ScxReflect;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.reflect.Type;
import java.util.List;

//import static cool.scx.reflect.TypeBindingsImpl.EMPTY_BINDINGS;

public class RecursionTest {

    public static class Node<T extends Node<T>> {
        public T node;
        public Node<T> next;
        public List<Node<T>> children;
        public List<T> parent;
    }

    public static void main(String[] args) {
        test1();
    }

    @Test
    public static void test1() {
        Type type = Node.class.getTypeParameters()[0].getBounds()[0];
        var type1 = (ClassInfo) ScxReflect.getType(type);
        var type2 = ScxReflect.getType(type);
        Assert.assertEquals(type1, type2);
        FieldInfo[] fieldInfos = type1.allFields();
    }

}
