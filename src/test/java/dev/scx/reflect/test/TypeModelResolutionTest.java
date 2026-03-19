package dev.scx.reflect.test;


import dev.scx.reflect.*;
import org.testng.annotations.Test;

import java.lang.reflect.Type;

import static org.testng.Assert.*;

public class TypeModelResolutionTest {

    public static void main(String[] args) throws Exception {
        declaringClass_should_keep_context_for_member_inner_type();
        allBindings_should_include_declaring_chain_bindings();
        semantically_equivalent_member_inner_types_should_be_same_instance_when_both_degrade_to_same_semantics();
        recursive_member_generic_should_not_stack_overflow_on_typeOf_toString_hashCode_equals();
        deeply_recursive_member_generic_should_not_stack_overflow();
        classInfo_equals_hashCode_and_identity_should_be_consistent();
        enumClass_should_return_real_enum_class_for_anonymous_enum_subclass();
        enumClass_should_return_self_for_normal_enum_type();
        recordConstructor_should_be_found_correctly();
        allBindings_should_merge_outer_and_inner_bindings_for_non_static_member_class();
        allBindings_should_not_merge_outer_bindings_for_static_nested_class();
        generic_array_should_not_break_identity_or_toString();
        raw_array_class_should_be_singleton_mapped();
        wildcard_owner_should_degrade_consistently();
        findSuperType_should_find_parameterized_interface();
        typeOf_entry_points_should_be_consistent_for_same_semantics();
    }

    // =========================
    //    成员内部类 ownerType 上下文丢失
    // =========================

    static class Outer<T> {
        class Inner<U> {
        }
    }

    static class Holder<S> {
        Outer<S>.Inner<Integer> value;
    }

    @Test
    public static void declaringClass_should_keep_context_for_member_inner_type() {
        Type holderType = new TypeReference<Holder<String>>() {}.type();
        ClassInfo holderInfo = (ClassInfo) ScxReflect.typeOf(holderType);

        FieldInfo field = holderInfo.fields()[0];
        ClassInfo fieldType = (ClassInfo) field.fieldType();

        assertNotNull(fieldType.declaringClass());
        assertEquals("TypeModelResolutionTest.Outer<String>", fieldType.declaringClass().toString());
        assertEquals("TypeModelResolutionTest.Outer<String>.Inner<Integer>", fieldType.toString());
    }

    @Test
    public static void allBindings_should_include_declaring_chain_bindings() {
        Type holderType = new TypeReference<Holder<String>>() {}.type();
        ClassInfo holderInfo = (ClassInfo) ScxReflect.typeOf(holderType);

        FieldInfo field = holderInfo.fields()[0];
        ClassInfo fieldType = (ClassInfo) field.fieldType();

        TypeBindings allBindings = fieldType.allBindings();

        assertEquals(2, allBindings.size());
        assertEquals("String", allBindings.get(0).toString());
        assertEquals("Integer", allBindings.get(1).toString());
    }

    // =========================
    // 2. 语义等价类型是否归一到同一实例
    // =========================

    static class A<X> {
        Outer<X>.Inner<Integer> f;
    }

    static class B<Y> {
        Outer<Y>.Inner<Integer> g;
    }

    @Test
    public static void semantically_equivalent_member_inner_types_should_be_same_instance_when_both_degrade_to_same_semantics() throws Exception {
        Type tf = A.class.getDeclaredField("f").getGenericType();
        Type tg = B.class.getDeclaredField("g").getGenericType();

        TypeInfo a = ScxReflect.typeOf(tf);
        TypeInfo b = ScxReflect.typeOf(tg);

        assertSame(a, b);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    // =========================
    // 3. 递归泛型最危险结构
    //    用来防 hashCode/equals/toString 递归爆栈
    // =========================

    static class RecursiveOuter<T extends RecursiveOuter<T>.Inner> {
        class Inner {
        }
    }

    @Test
    public static void recursive_member_generic_should_not_stack_overflow_on_typeOf_toString_hashCode_equals() {
        Type type = RecursiveOuter.class.getTypeParameters()[0].getBounds()[0];

        TypeInfo typeInfo = ScxReflect.typeOf(type);
        assertNotNull(typeInfo);

        typeInfo.toString();
        typeInfo.hashCode();
        assertEquals(typeInfo, typeInfo);

        TypeInfo typeInfo2 = ScxReflect.typeOf(type);
        assertSame(typeInfo, typeInfo2);
        assertEquals(typeInfo, typeInfo2);
        assertEquals(typeInfo.hashCode(), typeInfo2.hashCode());
    }

    // 更深一层的递归嵌套
    static class DeepOuter<T extends DeepOuter<T>.Middle.Inner> {
        class Middle {
            class Inner {
            }
        }
    }

    @Test
    public static void deeply_recursive_member_generic_should_not_stack_overflow() {
        Type type = DeepOuter.class.getTypeParameters()[0].getBounds()[0];

        TypeInfo typeInfo = ScxReflect.typeOf(type);
        assertNotNull(typeInfo);

        typeInfo.toString();
        typeInfo.hashCode();
        assertEquals(typeInfo, typeInfo);
    }

    // =========================
    // 4. declaringClass / bindings / equals / hashCode 一致性
    // =========================

    @Test
    public static void classInfo_equals_hashCode_and_identity_should_be_consistent() throws Exception {
        Type type = Holder.class.getDeclaredField("value").getGenericType();

        TypeInfo x1 = ScxReflect.typeOf(type);
        TypeInfo x2 = ScxReflect.typeOf(type);

        assertSame(x1, x2);
        assertEquals(x1, x2);
        assertEquals(x1.hashCode(), x2.hashCode());

        ClassInfo c1 = (ClassInfo) x1;
        ClassInfo c2 = (ClassInfo) x2;

        assertSame(c1.declaringClass(), c2.declaringClass());
        assertEquals(c1.declaringClass(), c2.declaringClass());
        assertEquals(c1.declaringClass().hashCode(), c2.declaringClass().hashCode());
    }

    // =========================
    // 5. 匿名枚举子类 enumClass()
    // =========================

    enum E {
        A {
            @Override
            public String x() {
                return "A";
            }
        },
        B;

        public String x() {
            return "B";
        }
    }

    @Test
    public static void enumClass_should_return_real_enum_class_for_anonymous_enum_subclass() {
        ClassInfo enumConstAnonymousSubclass = (ClassInfo) ScxReflect.typeOf(E.A.getClass());
        ClassInfo enumType = (ClassInfo) ScxReflect.typeOf(E.class);

        assertNotNull(enumConstAnonymousSubclass.enumClass());
        assertSame(enumType, enumConstAnonymousSubclass.enumClass());
        assertEquals("TypeModelResolutionTest.E", enumConstAnonymousSubclass.enumClass().toString());
    }

    @Test
    public static void enumClass_should_return_self_for_normal_enum_type() {
        ClassInfo enumType = (ClassInfo) ScxReflect.typeOf(E.class);

        assertSame(enumType, enumType.enumClass());
    }

    // =========================
    // 6. Record 规范构造函数识别
    // =========================

    record PairRecord<T>(T left, Integer right) {
    }

    @Test
    public static void recordConstructor_should_be_found_correctly() {
        ClassInfo recordType = (ClassInfo) ScxReflect.typeOf(new TypeReference<PairRecord<String>>() {}.type());

        ConstructorInfo recordConstructor = recordType.recordConstructor();

        assertNotNull(recordConstructor);
        assertEquals(2, recordConstructor.parameters().length);
        assertEquals("String", recordConstructor.parameters()[0].parameterType().toString());
        assertEquals("Integer", recordConstructor.parameters()[1].parameterType().toString());
    }

    // =========================
    // 7. 非静态成员类 allBindings 合并
    // =========================

    static class Outer2<T> {
        class Inner2<U> {
        }
    }

    @Test
    public static void allBindings_should_merge_outer_and_inner_bindings_for_non_static_member_class() throws Exception {
        class Holder2 {
            Outer2<String>.Inner2<Long> field;
        }

        Type type = Holder2.class.getDeclaredField("field").getGenericType();
        ClassInfo classInfo = (ClassInfo) ScxReflect.typeOf(type);

        TypeBindings allBindings = classInfo.allBindings();

        assertEquals(2, allBindings.size());
        assertEquals("String", allBindings.get(0).toString());
        assertEquals("Long", allBindings.get(1).toString());
    }

    // =========================
    // 8. 静态内部类不应继承外部类 bindings
    // =========================

    static class Outer3<T> {
        static class StaticInner<U> {
        }
    }

    @Test
    public static void allBindings_should_not_merge_outer_bindings_for_static_nested_class() throws Exception {
        class Holder3 {
            Outer3.StaticInner<Long> field;
        }

        Type type = Holder3.class.getDeclaredField("field").getGenericType();
        ClassInfo classInfo = (ClassInfo) ScxReflect.typeOf(type);

        TypeBindings allBindings = classInfo.allBindings();

        assertEquals(1, allBindings.size());
        assertEquals("Long", allBindings.get(0).toString());
    }

    // =========================
    // 9. 泛型数组 / 普通数组 归一一致性
    // =========================

    static class ArrayHolder<T> {
        T[] a;
        String[] b;
    }

    @Test
    public static void generic_array_should_not_break_identity_or_toString() throws Exception {
        Type genericArrayType = ArrayHolder.class.getDeclaredField("a").getGenericType();

        TypeInfo t1 = ScxReflect.typeOf(genericArrayType);
        TypeInfo t2 = ScxReflect.typeOf(genericArrayType);

        assertSame(t1, t2);
        assertEquals(t1, t2);
        assertEquals(t1.hashCode(), t2.hashCode());
        t1.toString();
    }

    @Test
    public static void raw_array_class_should_be_singleton_mapped() {
        TypeInfo t1 = ScxReflect.typeOf(String[].class);
        TypeInfo t2 = ScxReflect.typeOf(String[].class);

        assertSame(t1, t2);
        assertEquals("String[]", t1.toString());
    }

    // =========================
    // 10. Wildcard / TypeVariable 退化一致性
    // =========================

    static class WildHolder {
        Outer<?>.Inner<Integer> w;
    }

    @Test
    public static void wildcard_owner_should_degrade_consistently() throws Exception {
        Type type1 = WildHolder.class.getDeclaredField("w").getGenericType();
        TypeInfo t1 = ScxReflect.typeOf(type1);
        TypeInfo t2 = ScxReflect.typeOf(type1);

        assertSame(t1, t2);
        assertEquals(t1, t2);
        assertEquals(t1.hashCode(), t2.hashCode());
        t1.toString();
    }

    // =========================
    // 11. findSuperType 对参数化父类型查找
    // =========================

    interface BaseI<T> {
    }

    static class Impl implements BaseI<String> {
    }

    @Test
    public static void findSuperType_should_find_parameterized_interface() {
        ClassInfo impl = (ClassInfo) ScxReflect.typeOf(Impl.class);
        ClassInfo baseI = impl.findSuperType(BaseI.class);

        assertNotNull(baseI);
        assertEquals("TypeModelResolutionTest.BaseI<String>", baseI.toString());
    }

    // =========================
    // 12. 重复 typeOf 路径一致性
    //     Class / Type / TypeReference 混用不应出现语义不一致
    // =========================

    @Test
    public static void typeOf_entry_points_should_be_consistent_for_same_semantics() {
        TypeInfo c1 = ScxReflect.typeOf(String.class);
        TypeInfo c2 = ScxReflect.typeOf(new TypeReference<String>() {}.type());

        assertSame(c1, c2);
        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

}
