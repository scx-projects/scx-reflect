package dev.scx.reflect.test;

import dev.scx.reflect.ClassInfo;
import dev.scx.reflect.MethodInfo;
import dev.scx.reflect.ScxReflect;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class MethodInfoTest {

    public static void main(String[] args) {
        testAllMethods();
    }

    @Test
    public static void testAllMethods() {
        ClassInfo childClassInfo = (ClassInfo) ScxReflect.typeOf(Child.class); // 假设你有这个方法生成 ClassInfo
        MethodInfo[] allMethods = childClassInfo.allMethods();

        // 验证子类声明的方法
        assertTrue(containsMethodNamed(allMethods, "foo"));
        assertTrue(containsMethodNamed(allMethods, "qux"));
        assertTrue(containsMethodNamed(allMethods, "childOnly"));

        // 验证父类未被覆盖的方法依然在
        assertTrue(containsMethodNamed(allMethods, "parentOnly"));

        // final 和 static 方法不应该被认为是覆盖关系
        assertTrue(containsMethodNamed(allMethods, "finalMethod"));
        assertTrue(containsMethodNamed(allMethods, "staticMethod"));

        // 接口的默认方法 bar 不被实现，可能不在集合中，具体看实现需求
        // 这里示例不作断言

        // 进一步校验覆盖关系
        MethodInfo fooMethod = findMethodByName(allMethods, "foo");
        MethodInfo[] superMethods = fooMethod.superMethods();
        // 父类和两个接口的foo都应该在superMethods中
        assertEquals(3, superMethods.length, "foo 的 superMethods 应包含父类和两个接口的foo");

        // 检查superMethods分别属于哪个类/接口
        boolean hasParent = false, hasI1 = false, hasI2 = false, hasI3 = false;
        for (var sm : superMethods) {
            String declName = sm.declaringClass().name();
            if (declName.equals(Parent.class.getName())) {
                hasParent = true;
            } else if (declName.equals(I1.class.getName())) {
                hasI1 = true;
            } else if (declName.equals(I2.class.getName())) {
                hasI2 = true;
            } else if (declName.equals(I3.class.getName())) {
                hasI3 = true;
            }
        }
        assertTrue(hasParent, "superMethods 包含父类Parent的foo");
        assertTrue(hasI1 || hasI2 || hasI3, "superMethods 包含至少一个接口的foo");
    }

    // 辅助函数
    private static boolean containsMethodNamed(MethodInfo[] methods, String name) {
        for (var m : methods) {
            if (m.name().equals(name)) {
                return true;
            }
        }
        return false;
    }

    private static MethodInfo findMethodByName(MethodInfo[] methods, String name) {
        for (var m : methods) {
            if (m.name().equals(name)) {
                return m;
            }
        }
        return null;
    }

    // 模拟接口
    interface I1 {
        static void baz() {
        }

        void foo();

        default void bar() {
        }
    }

    interface I2 extends I1 {
        void foo(); // 继承自I1，接口同名方法

        void qux();
    }

    interface I3 {
        void foo();
    }

    // 父类
    static class Parent {
        public static void staticMethod() {
        }

        public void foo() {
        }

        public final void finalMethod() {
        }

        public void parentOnly() {
        }
    }

    // 子类，实现多个接口，继承父类，覆盖foo方法
    static class Child extends Parent implements I2, I3 {

        // 覆盖父类和接口foo
        @Override
        public void foo() {

        }

        // 实现接口I2的方法
        @Override
        public void qux() {

        }

        // 子类自有方法
        public void childOnly() {

        }

    }

}
