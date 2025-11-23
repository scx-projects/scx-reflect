package dev.scx.reflect.test;

import dev.scx.reflect.ClassInfo;
import dev.scx.reflect.MethodInfo;
import dev.scx.reflect.ScxReflect;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AllMethodsTest {

    public static void main(String[] args) {
        test1();
        test2();
        test3();
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
        Assert.assertEquals(methodInfos.length, 13);
    }


    @Test
    public static void test3() {
        var typeInfo = (ClassInfo) ScxReflect.typeOf(OOO.class);
        MethodInfo[] methodInfos = typeInfo.allMethods();
        Assert.assertEquals(methodInfos.length, 2);
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


    interface O {
        void ooo();
    }

    interface O1  {
        void ooo();
    }

    interface OOO extends  O, O1 {

    }


}
