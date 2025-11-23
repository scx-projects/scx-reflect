package dev.scx.reflect.test;

import dev.scx.reflect.*;
import dev.scx.reflect.test.c.CCC;
import dev.scx.reflect.test.c.EE;
import dev.scx.reflect.test.c.GGG;
import dev.scx.reflect.test.c.GGH;
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
        ClassInfo typeA = (ClassInfo) ScxReflect.typeOf(EE.A.getClass());
        ClassInfo typeB = (ClassInfo) ScxReflect.typeOf(EE.B.getClass());
        //这里 直接 isEnum 无法正确判断 枚举的 匿名内部类
        Assert.assertEquals(EE.B.getClass().isEnum(), false);
        // TypeInfo 可以正确判断
        Assert.assertEquals(typeA.classKind(), ClassKind.ENUM);
        Assert.assertEquals(typeB.classKind(), ClassKind.ENUM);
    }

    @Test
    public static void testInterFace() {
        ClassInfo typeG = (ClassInfo) ScxReflect.typeOf(GGG.class);
        ClassInfo typeH = (ClassInfo) ScxReflect.typeOf(GGH.class);

        MethodInfo[] gMethods = typeG.methods();

        Assert.assertEquals(gMethods.length, 2);

        MethodInfo[] hMethods = typeH.methods();

        Assert.assertEquals(hMethods.length, 1);

    }

    @Test
    public static void test3() {
        var type = (ClassInfo) ScxReflect.typeOf(CCC.R.class);
        var fieldInfos = type.allFields();
        Assert.assertEquals(fieldInfos.length, 1);
    }

    @Test
    public static void test4() {
        var typeA = (ClassInfo) ScxReflect.typeOf(CCC.A.class);
        var typeC = (ClassInfo) ScxReflect.typeOf(CCC.C.class);
        var typeD = (ClassInfo) ScxReflect.typeOf(CCC.D.class);

        var annotationsA = typeA.findAnnotations(CCC.AA.class);
        var annotationsC = typeC.findAnnotations(CCC.AA.class);
        var annotationsD = typeD.findAnnotations(CCC.AA.class);
        Assert.assertEquals(annotationsA.length, 2);
        Assert.assertEquals(annotationsC.length, 2);
        Assert.assertEquals(annotationsD.length, 2);
    }

    @Test
    public static void test5() {

        var type = (ClassInfo) ScxReflect.typeOf(CCC.GH.class);

        Assert.assertEquals(type.allFields().length, 0);

        var type4 = (ClassInfo) ScxReflect.typeOf(CCC.GAG.class);

        FieldInfo[] fields = type4.fields();
        FieldInfo[] allFields = type4.allFields();


        var typeInfo = (ClassInfo) ScxReflect.typeOf(new TypeReference<CCC.ComplexGenericClass<Map<String, ? extends Number[]>, List<? super Integer>, Set<Double>>>() {
        });

        FieldInfo[] fields1 = typeInfo.fields();
        System.out.println();
    }

    @Test
    public static void test6() {

        var l = System.nanoTime();
        TypeInfo classInfo;
        for (int i = 0; i < 9999; i = i + 1) {

            classInfo = ScxReflect.typeOf(CCC.C.class);
            classInfo = ScxReflect.typeOf(CCC.A.class);
            classInfo = ScxReflect.typeOf(CCC.D.class);
            classInfo = ScxReflect.typeOf(CCC.GH.class);
            classInfo = ScxReflect.typeOf(CCC.GG.class);
            classInfo = ScxReflect.typeOf(CCC.GAG.class);
            classInfo = ScxReflect.typeOf(CCC.BGH.class);
            classInfo = ScxReflect.typeOf(CCC.CGC.class);
            classInfo = ScxReflect.typeOf(CCC.ComplexGenericClass.class);
        }

        System.out.println((System.nanoTime() - l) / 1000_000);

    }

}
