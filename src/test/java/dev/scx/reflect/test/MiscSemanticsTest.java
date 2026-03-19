package dev.scx.reflect.test;

import dev.scx.reflect.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.testng.Assert.*;

public class MiscSemanticsTest {

    public static void main(String[] args) {
        enum_anonymous_subclass_should_still_be_enum_kind();
        interface_methods_should_filter_bridge_methods_correctly();
        record_should_expose_only_its_own_declared_field_in_allFields();
        repeatable_annotations_should_be_found_correctly();
        recursive_generic_subclass_should_not_introduce_extra_fields();
        generic_field_resolution_should_work_for_inherited_generic_chain();
        wildcard_degradation_should_produce_same_semantic_type();
        repeated_typeOf_should_be_stable_for_common_types();
    }

    /// 匿名枚举子类仍应识别为 ENUM
    @Test
    public static void enum_anonymous_subclass_should_still_be_enum_kind() {
        ClassInfo typeA = (ClassInfo) ScxReflect.typeOf(EE.A.getClass());
        ClassInfo typeB = (ClassInfo) ScxReflect.typeOf(EE.B.getClass());

        // JDK 的 isEnum 对匿名枚举常量子类并不可靠
        assertFalse(EE.B.getClass().isEnum());

        assertEquals(typeA.classKind(), ClassKind.ENUM);
        assertEquals(typeB.classKind(), ClassKind.ENUM);
        assertNotNull(typeA.enumClass());
        assertNotNull(typeB.enumClass());
        assertEquals(typeA.enumClass().rawClass(), EE.class);
        assertEquals(typeB.enumClass().rawClass(), EE.class);
    }

    /// 接口 default 方法 + 实现类协变返回值
    ///
    /// 预期:
    /// - GGG.methods() 应有 2 个: aa / bb
    /// - GGH.methods() 应只有 1 个: aa
    /// - bridge method 不应暴露出来
    @Test
    public static void interface_methods_should_filter_bridge_methods_correctly() {
        ClassInfo typeG = (ClassInfo) ScxReflect.typeOf(GGG.class);
        ClassInfo typeH = (ClassInfo) ScxReflect.typeOf(GGH.class);

        MethodInfo[] gMethods = typeG.methods();
        MethodInfo[] hMethods = typeH.methods();

        assertEquals(gMethods.length, 2);
        assertEquals(hMethods.length, 1);

        var gNames = Arrays.stream(gMethods).map(MethodInfo::name).toList();
        var hNames = Arrays.stream(hMethods).map(MethodInfo::name).toList();

        assertTrue(gNames.contains("aa"));
        assertTrue(gNames.contains("bb"));

        assertEquals(hNames.size(), 1);
        assertEquals(hNames.get(0), "aa");
    }

    /// record 的 allFields 行为
    ///
    /// 预期:
    /// - R 只有一个声明字段 name
    @Test
    public static void record_should_expose_only_its_own_declared_field_in_allFields() {
        var type = (ClassInfo) ScxReflect.typeOf(CCC.R.class);
        var fieldInfos = type.allFields();

        assertEquals(fieldInfos.length, 1);
        assertEquals(fieldInfos[0].name(), "name");
        assertSame(fieldInfos[0].fieldType(), ScxReflect.typeOf(String.class));
    }

    /// Repeatable 注解查找
    ///
    /// 预期:
    /// - A / C / D 都能正确拿到 AA 注解数组
    @Test
    public static void repeatable_annotations_should_be_found_correctly() {
        var typeA = (ClassInfo) ScxReflect.typeOf(CCC.A.class);
        var typeC = (ClassInfo) ScxReflect.typeOf(CCC.C.class);
        var typeD = (ClassInfo) ScxReflect.typeOf(CCC.D.class);

        var annotationsA = typeA.findAnnotations(CCC.AA.class);
        var annotationsC = typeC.findAnnotations(CCC.AA.class);
        var annotationsD = typeD.findAnnotations(CCC.AA.class);

        assertEquals(annotationsA.length, 2);
        assertEquals(annotationsC.length, 2);
        assertEquals(annotationsD.length, 2);

        assertEquals(annotationsA[0].value(), "a1");
        assertEquals(annotationsA[1].value(), "a2");

        assertEquals(annotationsC[0].value(), "c1");
        assertEquals(annotationsC[1].value(), "c2");

        assertEquals(annotationsD[0].value(), "d1");
        assertEquals(annotationsD[1].value(), "d2");
    }

    /// 递归泛型子类不应平白引入字段
    ///
    /// 预期:
    /// - GH 自己没有声明字段
    /// - 因此 GH.allFields() 也应为 0
    @Test
    public static void recursive_generic_subclass_should_not_introduce_extra_fields() {
        var type = (ClassInfo) ScxReflect.typeOf(CCC.GH.class);

        assertEquals(type.fields().length, 0);
        assertEquals(type.allFields().length, 0);
    }

    /// 继承链上的泛型字段解析
    ///
    /// GAG extends BGH<Integer> extends CGC<Integer>
    ///
    /// 预期:
    /// - GAG.fields() 为 0
    /// - GAG.allFields() 应包含:
    ///   s  -> Integer
    ///   x  -> Integer
    ///   x2 -> List<Object>
    ///   xs -> Integer[]
    @Test
    public static void generic_field_resolution_should_work_for_inherited_generic_chain() {
        var type = (ClassInfo) ScxReflect.typeOf(CCC.GAG.class);

        assertEquals(type.fields().length, 0);

        FieldInfo[] allFields = type.allFields();
        assertEquals(allFields.length, 4);

        assertFieldType(allFields, "s", "Integer");
        assertFieldType(allFields, "x", "Integer");
        assertFieldType(allFields, "x2", "List<Object>");
        assertFieldType(allFields, "xs", "Integer[]");
    }

    /// wildcard 退化后应得到同一语义实例
    ///
    /// Map<? extends List<String>, ? extends List<String>>
    ///
    /// 预期:
    /// - 两个绑定最终退化为相同的 TypeInfo 实例
    @Test
    public static void wildcard_degradation_should_produce_same_semantic_type() {
        var typeInfo = (ClassInfo) ScxReflect.typeOf(
            new TypeReference<Map<? extends List<String>, ? extends List<String>>>() {}
        );

        assertEquals(typeInfo.bindings().size(), 2);
        assertSame(typeInfo.bindings().get(0), typeInfo.bindings().get(1));
        assertEquals(typeInfo.bindings().get(0).toString(), "List<String>");
    }

    /// 常见类型重复 typeOf 应保持稳定
    ///
    /// 预期:
    /// - 同一个 Class / TypeReference 重复解析得到同一实例
    @Test
    public static void repeated_typeOf_should_be_stable_for_common_types() {
        TypeInfo t1 = ScxReflect.typeOf(CCC.C.class);
        TypeInfo t2 = ScxReflect.typeOf(CCC.C.class);
        assertSame(t1, t2);

        TypeInfo p1 = ScxReflect.typeOf(new TypeReference<CCC.ComplexGenericClass<
            Map<String, ? extends Number[]>,
            List<? super Integer>,
            Set<Double>>>() {});
        TypeInfo p2 = ScxReflect.typeOf(new TypeReference<CCC.ComplexGenericClass<
            Map<String, ? extends Number[]>,
            List<? super Integer>,
            Set<Double>>>() {});

        Assert.assertEquals(p1, p2);
        Assert.assertEquals(p1.hashCode(), p2.hashCode());
        Assert.assertEquals(p1.toString(), p2.toString());
    }

    // =========================
    // helper
    // =========================

    private static void assertFieldType(FieldInfo[] fields, String fieldName, String expectedTypeName) {
        var field = Arrays.stream(fields)
            .filter(f -> f.name().equals(fieldName))
            .findFirst()
            .orElseThrow(() -> new AssertionError("未找到字段: " + fieldName));

        assertEquals(field.fieldType().toString(), expectedTypeName);
    }

    // =========================
    // fixtures
    // =========================

    public interface CCC {

        @Repeatable(value = AB.class)
        @Retention(RetentionPolicy.RUNTIME)
        @interface AA {
            String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @interface AB {
            AA[] value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @interface AC {
        }

        record R(String name) {
        }

        @AA("c1")
        @AA("c2")
        class C extends A {
        }

        @AA("a1")
        @AA("a2")
        @AC
        class A {
            A gg;
        }

        @AA("d1")
        @AA("d2")
        class D extends A {
        }

        class GH extends GG<GH> {
        }

        class GG<T extends GG<T>> {
        }

        class GAG extends BGH<Integer> {
        }

        class BGH<S extends Number> extends CGC<S> {
            public S s;
        }

        class CGC<X> {

            public X x;

            public List<? super X> x2;

            public X[] xs;

            public CGC() {
            }

            public CGC(X[] xs, List<X[]> x) {
            }
        }

        class ComplexGenericClass<K extends Map<String, ? extends Number[]>, V, W> {

            public K[] arrayOfK;

            public Map<V, List<W>> nestedMap;

            public List<? super V> listOfSuperV;

            public Map<W, List<? extends K>> complexMethod(V param, List<? extends W> list) {
                return null;
            }
        }
    }

    public enum EE {
        A,
        B {
        }
    }

    public interface GGG {

        GGG aa(GGG p);

        default GGG[] bb(GGG[] p1) {
            return p1;
        }
    }

    public static class GGH implements GGG {

        @Override
        public GGH aa(GGG p) {
            return null;
        }
    }

}
