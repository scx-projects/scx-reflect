package dev.scx.reflect.test;

import dev.scx.reflect.ClassInfo;
import dev.scx.reflect.ScxReflect;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class AnnotationsTest {

    public static void main(String[] args) {
        test1_singleAnnotation();
        test2_repeatableAnnotations();
        test3_missingAnnotation();
        test4_baseClass_should_have_declared_inherited_annotation();
        test5_subClass_inherited_annotation_behavior_should_match_current_definition();
    }

    @Test
    public static void test1_singleAnnotation() {
        var type = typeOf(A.class);
        var anns = type.findAnnotations(X.class);

        Assert.assertEquals(anns.length, 1);
        Assert.assertEquals(anns[0].value(), "a");
    }

    @Test
    public static void test2_repeatableAnnotations() {
        var type = typeOf(B.class);
        var anns = type.findAnnotations(Y.class);

        Assert.assertEquals(anns.length, 2);
        Assert.assertEquals(anns[0].value(), "y1");
        Assert.assertEquals(anns[1].value(), "y2");
    }

    @Test
    public static void test3_missingAnnotation() {
        var type = typeOf(C.class);
        var anns = type.findAnnotations(X.class);

        Assert.assertEquals(anns.length, 0);
    }

    @Test
    public static void test4_baseClass_should_have_declared_inherited_annotation() {
        var type = typeOf(Base.class);
        var anns = type.findAnnotations(Z.class);

        Assert.assertEquals(anns.length, 1);
        Assert.assertEquals(anns[0].value(), "base");
    }

    @Test
    public static void test5_subClass_inherited_annotation_behavior_should_match_current_definition() {
        var type = typeOf(Sub.class);
        var anns = type.findAnnotations(Z.class);

        Assert.assertEquals(anns.length, 0); // 或 1，取决于你的定义
    }

    private static ClassInfo typeOf(Class<?> clazz) {
        return (ClassInfo) ScxReflect.typeOf(clazz);
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface X {
        String value();
    }

    @Repeatable(Ys.class)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Y {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface Ys {
        Y[] value();
    }

    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @interface Z {
        String value();
    }

    @X("a")
    static class A {
    }

    @Y("y1")
    @Y("y2")
    static class B {
    }

    static class C {
    }

    @Z("base")
    static class Base {
    }

    static class Sub extends Base {
    }

}
