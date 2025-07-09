package cool.scx.reflect.test;

import cool.scx.reflect.*;
import cool.scx.reflect.test.c.CCC;
import cool.scx.reflect.test.c.EE;
import cool.scx.reflect.test.c.GGG;
import cool.scx.reflect.test.c.GGH;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ScxReflectTest {

    public static void main(String[] args) {
        testEnum();
        testInterFace();
        test3();
        test4();
        test5();
        test6();
    }

    @Test
    public static void testEnum() {
        ClassInfo typeA = (ClassInfo) ScxReflect.getType(EE.A.getClass());
        ClassInfo typeB = (ClassInfo) ScxReflect.getType(EE.B.getClass());
        //这里 直接 isEnum 无法正确判断 枚举的 匿名内部类 
        Assert.assertEquals(EE.B.getClass().isEnum(), false);
        // TypeInfo 可以正确判断
        Assert.assertEquals(typeA.classKind(), ClassKind.ENUM);
        Assert.assertEquals(typeB.classKind(), ClassKind.ENUM);
    }

    @Test
    public static void testInterFace() {
        ClassInfo typeG = (ClassInfo) ScxReflect.getType(GGG.class);
        ClassInfo typeH = (ClassInfo) ScxReflect.getType(GGH.class);

        MethodInfo[] gMethods = typeG.methods();

        Assert.assertEquals(gMethods.length, 2);

        MethodInfo[] hMethods = typeH.methods();

        Assert.assertEquals(hMethods.length, 1);

    }

    @Test
    public static void test3() {
        var type = (ClassInfo) ScxReflect.getType(CCC.R.class);
        var fieldInfos = type.allFields();
        Assert.assertEquals(fieldInfos.length, 1);
    }

    @Test
    public static void test4() {
        var typeA = (ClassInfo) ScxReflect.getType(CCC.A.class);
        var typeC = (ClassInfo) ScxReflect.getType(CCC.C.class);
        var typeD = (ClassInfo) ScxReflect.getType(CCC.D.class);

        var annotationsA = typeA.findAnnotations(CCC.AA.class);
        var annotationsC = typeC.findAnnotations(CCC.AA.class);
        var annotationsD = typeD.findAnnotations(CCC.AA.class);
        Assert.assertEquals(annotationsA.length, 2);
        Assert.assertEquals(annotationsC.length, 2);
        Assert.assertEquals(annotationsD.length, 2);
    }

    @Test
    public static void test5() {

        var type = (ClassInfo) ScxReflect.getType(CCC.GH.class);

        Assert.assertEquals(type.allFields().length, 0);

        var type4 = (ClassInfo) ScxReflect.getType(CCC.GAG.class);

        FieldInfo[] fields = type4.fields();
        FieldInfo[] allFields = type4.allFields();


        var typeInfo = (ClassInfo) ScxReflect.getType(new TypeReference<CCC.ComplexGenericClass<Map<String, ? extends Number[]>, List<? super Integer>, Set<Double>>>() {
        });

        FieldInfo[] fields1 = typeInfo.fields();
        System.out.println();
    }

    @Test
    public static void test6() {

        var l = System.nanoTime();
        TypeInfo classInfo;
        for (int i = 0; i < 9999; i = i + 1) {

            classInfo = ScxReflect.getType(CCC.C.class);
            classInfo = ScxReflect.getType(CCC.A.class);
            classInfo = ScxReflect.getType(CCC.D.class);
            classInfo = ScxReflect.getType(CCC.GH.class);
            classInfo = ScxReflect.getType(CCC.GG.class);
            classInfo = ScxReflect.getType(CCC.GAG.class);
            classInfo = ScxReflect.getType(CCC.BGH.class);
            classInfo = ScxReflect.getType(CCC.CGC.class);
            classInfo = ScxReflect.getType(CCC.ComplexGenericClass.class);
        }

        System.out.println((System.nanoTime() - l) / 1000_000);

    }

}
