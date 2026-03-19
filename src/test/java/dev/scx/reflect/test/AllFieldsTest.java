package dev.scx.reflect.test;

import dev.scx.reflect.ClassInfo;
import dev.scx.reflect.FieldInfo;
import dev.scx.reflect.ScxReflect;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


public class AllFieldsTest {

    public static void main(String[] args) {
        interface_allFields_should_include_super_interface_fields();
        class_allFields_should_include_interface_fields();
        subclass_allFields_should_include_superclass_and_interface_fields();
        no_duplicate_fields_should_exist_when_interface_paths_overlap();
        overlap_interface_inheritance_should_still_keep_all_expected_fields();
        sanity_check_declaring_classes();
    }

    @Test
    public static void interface_allFields_should_include_super_interface_fields() {
        var bInfo = (ClassInfo) ScxReflect.typeOf(B.class);

        Set<String> names = Arrays.stream(bInfo.allFields())
            .map(FieldInfo::name)
            .collect(Collectors.toSet());

        assertEquals(Set.of("X", "Y"), names);
    }

    @Test
    public static void class_allFields_should_include_interface_fields() {
        var cInfo = (ClassInfo) ScxReflect.typeOf(C.class);

        Set<String> names = Arrays.stream(cInfo.allFields())
            .map(FieldInfo::name)
            .collect(Collectors.toSet());

        assertEquals(Set.of("X", "Y", "Z"), names);
    }

    @Test
    public static void subclass_allFields_should_include_superclass_and_interface_fields() {
        var dInfo = (ClassInfo) ScxReflect.typeOf(D.class);

        Set<String> names = Arrays.stream(dInfo.allFields())
            .map(FieldInfo::name)
            .collect(Collectors.toSet());

        assertEquals(Set.of("X", "Y", "Z", "W"), names);
    }

    @Test
    public static void no_duplicate_fields_should_exist_when_interface_paths_overlap() {
        var eInfo = (ClassInfo) ScxReflect.typeOf(E.class);

        var fields = eInfo.allFields();
        var names = Arrays.stream(fields)
            .map(FieldInfo::name)
            .collect(Collectors.toList());

        long xCount = names.stream().filter("X"::equals).count();
        long yCount = names.stream().filter("Y"::equals).count();
        long zCount = names.stream().filter("Z"::equals).count();

        assertEquals(1, xCount);
        assertEquals(1, yCount);
        assertEquals(1, zCount);
    }

    @Test
    public static void overlap_interface_inheritance_should_still_keep_all_expected_fields() {
        var i3Info = (ClassInfo) ScxReflect.typeOf(I3.class);

        Set<String> names = Arrays.stream(i3Info.allFields())
            .map(FieldInfo::name)
            .collect(Collectors.toSet());

        assertEquals(Set.of("X", "Z"), names);

        var eInfo = (ClassInfo) ScxReflect.typeOf(E.class);

        Set<String> eNames = Arrays.stream(eInfo.allFields())
            .map(FieldInfo::name)
            .collect(Collectors.toSet());

        assertEquals(Set.of("X", "Y", "Z"), eNames);
    }

    @Test
    public static void sanity_check_declaring_classes() {
        var cInfo = (ClassInfo) ScxReflect.typeOf(C.class);

        var byName = Arrays.stream(cInfo.allFields())
            .collect(Collectors.toMap(FieldInfo::name, f -> f));

        assertTrue(byName.containsKey("X"));
        assertTrue(byName.containsKey("Y"));
        assertTrue(byName.containsKey("Z"));

        assertEquals(A.class, byName.get("X").declaringClass().rawClass());
        assertEquals(B.class, byName.get("Y").declaringClass().rawClass());
        assertEquals(C.class, byName.get("Z").declaringClass().rawClass());
    }

    interface A {
        int X = 1;
    }

    interface B extends A {
        int Y = 2;
    }

    interface I1 extends A {
    }

    interface I2 extends A {
    }

    interface I3 extends I1, I2 {
        int Z = 3;
    }

    static class C implements B {
        int Z;
    }

    static class D extends C {
        int W;
    }

    static class E implements I3 {
        int Y;
    }

}
