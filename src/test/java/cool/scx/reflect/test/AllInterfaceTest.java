package cool.scx.reflect.test;

import cool.scx.reflect.ClassInfo;
import cool.scx.reflect.ScxReflect;
import cool.scx.reflect.TypeInfo;
import org.testng.annotations.Test;

public class AllInterfaceTest {

    public static void main(String[] args) {
        test1();
    }

    @Test
    public static void test1() {
        var typeInfo =(ClassInfo) ScxReflect.typeOf(A.class);
        ClassInfo[] classInfos = typeInfo.allInterfaces();
        ClassInfo superType = typeInfo.findSuperType(C.class);

        System.out.println();
    }

    public static class A extends B{

    }

    public static class B implements C {

    }

    public interface C {

    }
    
}
