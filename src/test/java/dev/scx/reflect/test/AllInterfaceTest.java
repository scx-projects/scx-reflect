package dev.scx.reflect.test;

import dev.scx.reflect.ClassInfo;
import dev.scx.reflect.ScxReflect;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AllInterfaceTest {

    public static void main(String[] args) {
        test1();
    }

    @Test
    public static void test1() {
        var typeInfo = (ClassInfo) ScxReflect.typeOf(A.class);
        ClassInfo[] classInfos = typeInfo.allInterfaces();
        ClassInfo superType = typeInfo.findSuperType(C.class);

        Assert.assertEquals(classInfos.length, 1);
        Assert.assertNotNull(superType);
    }


    public static class A extends B {

    }

    public static class B implements C {

    }

    public interface C {

    }

}
