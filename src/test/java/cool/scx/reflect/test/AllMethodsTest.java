package cool.scx.reflect.test;

import cool.scx.reflect.ClassInfo;
import cool.scx.reflect.MethodInfo;
import cool.scx.reflect.ScxReflect;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AllMethodsTest {

    public static void main(String[] args) {
        test1();
    }

    @Test
    public static void test1() {
        var typeInfo = (ClassInfo) ScxReflect.typeOf(C.class);
        MethodInfo[] methodInfos = typeInfo.allMethods();
        Assert.assertEquals(methodInfos.length, 13);
    }

    interface I1 {
        default void hello() {
            System.out.println("I1 hello");
        }
    }

    interface I2 {
        default void hello() {
            System.out.println("I2 hello");
        }
    }

    class C implements I1, I2 {
        @Override
        public void hello() {
            I1.super.hello();
        }
        // 没有重写hello()
    }


    interface A {
        void bbb();
    }

    interface B extends A {
        default void bbb() {

        }
    }

    static class Cc implements A, B {

    }
    
}
