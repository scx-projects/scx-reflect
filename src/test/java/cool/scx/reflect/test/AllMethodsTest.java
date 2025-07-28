package cool.scx.reflect.test;

import cool.scx.reflect.ClassInfo;
import cool.scx.reflect.MethodInfo;
import cool.scx.reflect.ScxReflect;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AllMethodsTest {

    public static void main(String[] args) {
        test1();
        test2();
    }

    @Test
    public static void test1() {
        var typeInfo = (ClassInfo) ScxReflect.typeOf(C.class);
        MethodInfo[] methodInfos = typeInfo.allMethods();
        Assert.assertEquals(methodInfos.length, 13);
    }

    @Test
    public static void test2() {
        var typeInfo = (ClassInfo) ScxReflect.typeOf(K.class);
        MethodInfo[] methodInfos = typeInfo.allMethods();
        System.out.println();
    }


    interface A {
        void bbb();
    }

    interface B extends A {
        default void bbb() {

        }
    }

    static class C implements A, B {

    }


    // 特例的重写方式
    interface H {
        void hasXXX();
    }

    abstract static class J {

        public void hasXXX() {

        }

    }

    static class K extends J implements H {

    }

}
