package dev.scx.reflect.test;

import dev.scx.reflect.ClassInfo;
import dev.scx.reflect.MethodInfo;
import dev.scx.reflect.ScxReflect;
import dev.scx.reflect.test.p1.CrossPkgParent;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class AllMethodsTest {

    public static void main(String[] args) {
        test1_interfaceAbstractThenDefaultThenClassImplement();
        test2_classConcreteMethodDominatesInterfaceAbstractMethod();
        test3_multipleInterfaceAbstractMethodsShouldCoexist();
        test4_classConcreteMethodDominatesInterfaceDefaultMethod();
        test5_abstractClassMethodDominatesInterfaceDefaultInView();
        test6_childAbstractOverrideShouldReplaceParentConcreteMethod();
        test7_interfaceChainAbstractDefaultAbstract();
        test8_privateMethodShouldNotBeOverridden();
        test9_staticMethodsShouldAlwaysCoexist();
        test10_packagePrivateMethodInSamePackageCanOverride();
        test11_privateClassMethodShouldNotDominateInterfaceAbstractMethod();
        test12_privateClassMethodShouldNotDominateInterfaceDefaultMethod();
        test16_privateClassMethodShouldNotDominateMultipleInterfaceMethods();
        test17_privateParentMethodShouldRemainAndChildConcreteMethodShouldDominateInterfaceMethod();
        test18_privateClassMethodShouldNotDominateInterfaceDefaultAndAbstractMethods();
        test19_privateInterfaceMethodShouldNotBeDroppedByClassMethod();
        test20_packagePrivateMethodInDifferentPackageShouldNotDominateInterfaceMethod();
        test21_packagePrivateMethodWithIntermediateDifferentPackageSubclass();
        test22();
        test_allMethods_totalCount_C();
        test_allMethods_totalCount_K();
        test_allMethods_totalCount_WW();
        test_allMethods_totalCount_OOO();

        testDiamondInterfaceOverrideConflict();
        testClassOverrideInterfaceDefaultMethod();
        testAbstractClassImplementsInterface();
        testEnumConstantAnonymousClass();
        testGenericBridgeMethodElimination();
        testInterfaceSameNameDifferentParams();
        testRecordCanonicalConstructor();

        testAllMethods();
    }

    // =========================
    // helpers
    // =========================

    private static ClassInfo typeOf(Class<?> clazz) {
        return (ClassInfo) ScxReflect.typeOf(clazz);
    }

    private static List<MethodInfo> methodsByName(Class<?> clazz, String methodName) {
        return Arrays.stream(typeOf(clazz).allMethods())
            .filter(m -> m.name().equals(methodName))
            .toList();
    }

    private static Set<Class<?>> declaringClasses(List<MethodInfo> methods) {
        return methods.stream()
            .map(m -> m.declaringClass().rawClass())
            .collect(Collectors.toSet());
    }

    private static void assertMethodCount(Class<?> clazz, String methodName, int expectedCount) {
        var methods = methodsByName(clazz, methodName);
        Assert.assertEquals(
            methods.size(),
            expectedCount,
            clazz.getSimpleName() + ".allMethods() 中名为 " + methodName + " 的方法数量不符合预期"
        );
    }

    private static MethodInfo assertSingleMethod(Class<?> clazz, String methodName) {
        var methods = methodsByName(clazz, methodName);
        Assert.assertEquals(
            methods.size(),
            1,
            clazz.getSimpleName() + ".allMethods() 中名为 " + methodName + " 的方法应当恰好只有一个"
        );
        return methods.get(0);
    }

    private static void assertDeclaringClasses(Class<?> clazz, String methodName, Class<?>... expectedDeclaringClasses) {
        var methods = methodsByName(clazz, methodName);
        var actual = declaringClasses(methods);
        var expected = Set.of(expectedDeclaringClasses);

        Assert.assertEquals(
            actual,
            expected,
            clazz.getSimpleName() + ".allMethods() 中名为 " + methodName + " 的方法声明来源不符合预期"
        );
    }

    private static void assertMethodFlags(MethodInfo method, boolean isAbstract, boolean isDefault) {
        Assert.assertEquals(method.isAbstract(), isAbstract, "isAbstract 不符合预期");
        Assert.assertEquals(method.isDefault(), isDefault, "isDefault 不符合预期");
    }

    // =========================
    // test cases
    // =========================

    /// 接口 abstract + 子接口 default + 类实现
    ///
    /// 预期:
    /// - C 最终只保留 B.bbb
    /// - A.bbb 应被排除
    @Test
    public static void test1_interfaceAbstractThenDefaultThenClassImplement() {
        var method = assertSingleMethod(C.class, "bbb");
        Assert.assertEquals(method.declaringClass().rawClass(), B.class);
        assertMethodFlags(method, false, true);
    }

    /// 类具体方法 + 接口抽象方法
    ///
    /// 预期:
    /// - K 最终只保留 J.hasXXX
    /// - H.hasXXX 应被排除
    @Test
    public static void test2_classConcreteMethodDominatesInterfaceAbstractMethod() {
        var method = assertSingleMethod(K.class, "hasXXX");
        Assert.assertEquals(method.declaringClass().rawClass(), J.class);
        assertMethodFlags(method, false, false);
    }

    /// 多接口同签名抽象方法并存
    ///
    /// 预期:
    /// - OOO 最终保留 O.ooo 和 O1.ooo
    /// - 二者都不应被排除
    @Test
    public static void test3_multipleInterfaceAbstractMethodsShouldCoexist() {
        assertMethodCount(OOO.class, "ooo", 2);
        assertDeclaringClasses(OOO.class, "ooo", O.class, O1.class);
    }

    /// 类具体方法 + 接口 default 方法
    ///
    /// 预期:
    /// - 最终只保留 A4.m
    /// - I4.m 应被排除
    @Test
    public static void test4_classConcreteMethodDominatesInterfaceDefaultMethod() {
        var method = assertSingleMethod(C4.class, "m");
        Assert.assertEquals(method.declaringClass().rawClass(), A4.class);
        assertMethodFlags(method, false, false);
    }

    /// 父类抽象方法 + 接口 default 方法
    ///
    /// 说明:
    /// - 这里测试的是“最终保留的方法声明视图”
    /// - 不是“最终可执行实现”
    ///
    /// 预期:
    /// - 最终只保留 A5.m
    /// - I5.m 应被排除
    /// - B5 仍然是抽象类
    @Test
    public static void test5_abstractClassMethodDominatesInterfaceDefaultInView() {
        var method = assertSingleMethod(B5.class, "m");
        Assert.assertEquals(method.declaringClass().rawClass(), A5.class);
        assertMethodFlags(method, true, false);
    }

    /// 父类具体方法 + 子类抽象重写
    ///
    /// 预期:
    /// - 最终只保留 B6.m
    /// - A6.m 应被排除
    @Test
    public static void test6_childAbstractOverrideShouldReplaceParentConcreteMethod() {
        var method = assertSingleMethod(B6.class, "m");
        Assert.assertEquals(method.declaringClass().rawClass(), B6.class);
        assertMethodFlags(method, true, false);
    }

    /// 接口链 abstract -> default -> abstract
    ///
    /// 预期:
    /// - 最终只保留 C7.m
    /// - B7.m 和 A7.m 都应被排除
    @Test
    public static void test7_interfaceChainAbstractDefaultAbstract() {
        var method = assertSingleMethod(C7.class, "m");
        Assert.assertEquals(method.declaringClass().rawClass(), C7.class);
        assertMethodFlags(method, true, false);
    }

    /// private 同签名方法不构成 override
    ///
    /// 预期:
    /// - WW.x 和 W.x 都应保留
    @Test
    public static void test8_privateMethodShouldNotBeOverridden() {
        assertMethodCount(WW.class, "x", 2);
        assertDeclaringClasses(WW.class, "x", WW.class, W.class);
    }

    /// static 同签名方法不参与 override
    ///
    /// 预期:
    /// - SS.y 和 S.y 都应保留
    @Test
    public static void test9_staticMethodsShouldAlwaysCoexist() {
        assertMethodCount(SS.class, "y", 2);
        assertDeclaringClasses(SS.class, "y", SS.class, S.class);
    }

    /// 同包 package-private 方法可以 override
    ///
    /// 预期:
    /// - 最终只保留 PPChild.z
    /// - PPParent.z 应被排除
    @Test
    public static void test10_packagePrivateMethodInSamePackageCanOverride() {
        var method = assertSingleMethod(PPChild.class, "z");
        Assert.assertEquals(method.declaringClass().rawClass(), PPChild.class);
        assertMethodFlags(method, false, false);
    }

    @Test
    public static void test11_privateClassMethodShouldNotDominateInterfaceAbstractMethod() {
        assertMethodCount(B11.class, "x", 2);
        assertDeclaringClasses(B11.class, "x", A11.class, I11X.class);
    }

    interface I11X {
        void x();
    }

    static class A11 {
        private void x() {
        }
    }

    abstract static class B11 extends A11 implements I11X {
    }

    @Test
    public static void test12_privateClassMethodShouldNotDominateInterfaceDefaultMethod() {
        assertMethodCount(B12.class, "x", 2);
        assertDeclaringClasses(B12.class, "x", A12.class, I12.class);
    }

    interface I12 {
        default void x() {
        }
    }

    static class A12 {
        private void x() {
        }
    }

    abstract static class B12 extends A12 implements I12 {
    }

    @Test
    public static void test16_privateClassMethodShouldNotDominateMultipleInterfaceMethods() {
        assertMethodCount(B16.class, "x", 3);
        assertDeclaringClasses(B16.class, "x", A16.class, I16.class, I16B.class);
    }

    interface I16 {
        void x();
    }

    interface I16B {
        void x();
    }

    static class A16 {
        private void x() {
        }
    }

    abstract static class B16 extends A16 implements I16, I16B {
    }

    @Test
    public static void test17_privateParentMethodShouldRemainAndChildConcreteMethodShouldDominateInterfaceMethod() {
        assertMethodCount(B17.class, "x", 2);
        assertDeclaringClasses(B17.class, "x", A17.class, B17.class);
    }

    interface I17 {
        void x();
    }

    static class A17 {
        private void x() {
        }
    }

    static class B17 extends A17 implements I17 {
        @Override
        public void x() {
        }
    }

    @Test
    public static void test18_privateClassMethodShouldNotDominateInterfaceDefaultAndAbstractMethods() {
        assertMethodCount(B18.class, "x", 2);
        assertDeclaringClasses(B18.class, "x", A18.class, I18B.class);
    }

    interface I18A {
        void x();
    }

    interface I18B extends I18A {
        default void x() {
        }
    }

    static class A18 {
        private void x() {
        }
    }

    abstract static class B18 extends A18 implements I18B {
    }


    @Test
    public static void test19_privateInterfaceMethodShouldNotBeDroppedByClassMethod() {
        assertMethodCount(B19.class, "x", 2);
        assertDeclaringClasses(B19.class, "x", A19.class, I19Private.class);
    }

    interface I19Private {
        private void x() {
        }
    }

    interface I19Public {
        default void x() {
        }
    }

    static class A19 {
        public void x() {
        }
    }

    static class B19 extends A19 implements I19Private, I19Public {
    }


    interface I {
        void x();
    }

    abstract static class B20 extends CrossPkgParent implements I {
    }

    @Test
    public static void test20_packagePrivateMethodInDifferentPackageShouldNotDominateInterfaceMethod() {
        var info = (ClassInfo) ScxReflect.typeOf(B20.class);
        var methods = java.util.Arrays.stream(info.allMethods())
            .filter(m -> m.name().equals("x"))
            .toList();

        org.testng.Assert.assertEquals(methods.size(), 2);
        var actual = methods.stream()
            .map(m -> m.declaringClass().rawClass())
            .collect(java.util.stream.Collectors.toSet());

        org.testng.Assert.assertEquals(actual, java.util.Set.of(CrossPkgParent.class, I.class));
    }

    @Test
    public static void test21_packagePrivateMethodWithIntermediateDifferentPackageSubclass() {
        var info = (ClassInfo) ScxReflect.typeOf(dev.scx.reflect.test.p1.C.class);

        var methods = Arrays.stream(info.allMethods())
            .filter(m -> m.name().equals("x"))
            .toList();

        var actual = methods.stream()
            .map(m -> m.declaringClass().rawClass())
            .collect(Collectors.toSet());

        Assert.assertEquals(methods.size(), 2);
    }

    interface II1 {
        private void x() {
        }
    }

    interface II2 {
        default void x() {
        }
    }

    static class A22 {
        private void x() {
        }
    }

    static class B22 extends A22 {
        public void x() {
        }
    }

    static class C22 extends B22 implements II1, II2 {
    }

    @Test
    public static void test22() {
        var typeInfo = (ClassInfo) ScxReflect.typeOf(C22.class);
        MethodInfo[] methodInfos = typeInfo.allMethods();
        Assert.assertEquals(methodInfos.length, 15);
    }

    @Test
    public static void test_allMethods_totalCount_C() {
        var typeInfo = typeOf(C.class);
        Assert.assertEquals(typeInfo.allMethods().length, 13);
    }

    @Test
    public static void test_allMethods_totalCount_K() {
        var typeInfo = typeOf(K.class);
        Assert.assertEquals(typeInfo.allMethods().length, 13);
    }

    @Test
    public static void test_allMethods_totalCount_OOO() {
        var typeInfo = typeOf(OOO.class);
        Assert.assertEquals(typeInfo.allMethods().length, 2);
    }

    @Test
    public static void test_allMethods_totalCount_WW() {
        var typeInfo = typeOf(WW.class);
        Assert.assertEquals(typeInfo.allMethods().length, 14);
    }

    private static Set<String> methodNames(MethodInfo[] methods) {
        return Arrays.stream(methods).map(MethodInfo::name).collect(Collectors.toSet());
    }

    private static void printMethods(MethodInfo[] methods) {
        for (MethodInfo m : methods) {
            System.out.println(m.declaringClass().rawClass().getSimpleName() + " -> " + m.signature());
        }
    }

    @Test
    public static void testDiamondInterfaceOverrideConflict() {
        ClassInfo info = (ClassInfo) ScxReflect.typeOf(DiamondImpl.class);
        var methods = info.allMethods();
        // 打印结果供人工检查
        printMethods(methods);
        var names = methodNames(methods);
        // foo 必须只出现一次 (冲突已由子类解决)
        assertEquals(names.stream().filter("foo"::equals).count(), 1);
    }

    @Test
    public static void testClassOverrideInterfaceDefaultMethod() {
        ClassInfo info = (ClassInfo) ScxReflect.typeOf(Child.class);
        var methods = info.allMethods();
        printMethods(methods);
        // 只保留一个 hello, 应该来自 ParentClass
        assertEquals(methods.length, 13);
        assertEquals(Arrays.stream(methods).filter(c -> c.name().equals("hello")).findFirst().orElse(null).declaringClass().rawClass(), ParentClass.class);
    }

    @Test
    public static void testAbstractClassImplementsInterface() {
        ClassInfo info = (ClassInfo) ScxReflect.typeOf(Impl.class);
        var methods = info.allMethods();
        printMethods(methods);
        assertEquals(methods.length, 13);
        assertEquals(Arrays.stream(methods).filter(c -> c.name().equals("work")).findFirst().orElse(null).declaringClass().rawClass(), AbstractFoo.class);
    }

    @Test
    public static void testEnumConstantAnonymousClass() {
        ClassInfo info = (ClassInfo) ScxReflect.typeOf(ComplexEnum.A.getClass());
        var methods = info.allMethods();
        printMethods(methods);
        var runMethod = Arrays.stream(methods).filter(m -> m.name().equals("run")).findFirst();
        assertTrue(runMethod.isPresent());
    }

    @Test
    public static void testGenericBridgeMethodElimination() {
        ClassInfo info = (ClassInfo) ScxReflect.typeOf(GenericImpl.class);
        var methods = info.allMethods();
        printMethods(methods);
        // 不应包含桥接方法, 只保留 get
        assertEquals(methods.length, 13);
    }

    @Test
    public static void testInterfaceSameNameDifferentParams() {
        ClassInfo info = (ClassInfo) ScxReflect.typeOf(IFinal.class);
        var methods = info.allMethods();
        printMethods(methods);
        // 应该包含两个 doSomething, 不冲突
        var names = methodNames(methods);
        assertTrue(names.contains("doSomething"));
        assertEquals(methods.length, 2);
    }

    @Test
    public static void testRecordCanonicalConstructor() {
        ClassInfo info = (ClassInfo) ScxReflect.typeOf(Person.class);
        var constructor = info.recordConstructor();
        assertEquals(constructor.parameters().length, 2);
        assertEquals(constructor.parameters()[0].name(), "name");
        assertEquals(constructor.parameters()[1].name(), "age");
    }

    @Test
    public static void testAllMethods() {
        ClassInfo childClassInfo = (ClassInfo) ScxReflect.typeOf(Child1.class); // 假设你有这个方法生成 ClassInfo
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

        // 接口的默认方法 bar 不被实现, 可能不在集合中, 具体看实现需求
        // 这里示例不作断言

        // 进一步校验覆盖关系
        MethodInfo fooMethod = findMethodByName(allMethods, "foo");
        MethodInfo[] superMethods = fooMethod.superMethods();
        // 父类和两个接口的foo都应该在superMethods中
        assertEquals(superMethods.length, 3, "foo 的 superMethods 应包含父类和两个接口的foo");

        // 检查superMethods分别属于哪个类/接口
        boolean hasParent = false, hasI1 = false, hasI2 = false, hasI3 = false;
        for (var sm : superMethods) {
            String declName = sm.declaringClass().name();
            if (declName.equals(Parent1.class.getName())) {
                hasParent = true;
            } else if (declName.equals(I11.class.getName())) {
                hasI1 = true;
            } else if (declName.equals(I22.class.getName())) {
                hasI2 = true;
            } else if (declName.equals(I33.class.getName())) {
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
    interface I11 {
        static void baz() {
        }

        void foo();

        default void bar() {
        }
    }

    interface I22 extends I11 {
        void foo(); // 继承自I1, 接口同名方法

        void qux();
    }

    interface I33 {
        void foo();
    }

    // 父类
    static class Parent1 {
        public static void staticMethod() {
        }

        public void foo() {
        }

        public final void finalMethod() {
        }

        public void parentOnly() {
        }
    }

    // 子类, 实现多个接口, 继承父类, 覆盖foo方法
    static class Child1 extends Parent1 implements I22, I33 {

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

    // region: 边界类定义

    interface I1 {
        default void foo() {
        }
    }

    interface I2 extends I1 {
        default void foo() {
        }
    }

    interface I3 extends I1 {
        default void foo() {
        }
    }

    interface Diamond extends I2, I3 {
        @Override
        default void foo() {
            I2.super.foo();
        }
    }

    static class DiamondImpl implements Diamond {
        // 如果不实现 foo, 会编译失败 (必须 disambiguate)
        public void foo() {
        }
    }

    interface ParentIface {
        default void hello() {
        }
    }

    static class ParentClass {
        public void hello() {
        }
    }

    static class Child extends ParentClass implements ParentIface {}

    interface IFoo {
        void work();
    }

    abstract static class AbstractFoo {
        public void work() {
        }
    }

    static class Impl extends AbstractFoo implements IFoo {}

    enum ComplexEnum {
        A {
            @Override
            public void run() {
            }
        }, B;

        public void run() {
        }
    }

    static class GenericBase<T> {
        public T get() {
            return null;
        }
    }

    static class GenericImpl extends GenericBase<String> {
        @Override
        public String get() {
            return "str";
        }
    }

    interface ILeft {
        void doSomething(String s);
    }

    interface IRight {
        void doSomething(int i);
    }

    interface IFinal extends ILeft, IRight {}

    record Person(String name, int age) {}

    // endregion


    // =========================
    // original fixtures
    // =========================

    interface A {
        void bbb();
    }

    interface B extends A {
        default void bbb() {
        }
    }

    static class C implements A, B {
    }

    interface H {
        void hasXXX();
    }

    abstract static class J {
        public void hasXXX() {
        }
    }

    static class K extends J implements H {
    }

    interface O {
        void ooo();
    }

    interface O1 {
        void ooo();
    }

    interface OOO extends O, O1 {
    }

    interface I4 {
        default void m() {
        }
    }

    static class A4 {
        public void m() {
        }
    }

    static class C4 extends A4 implements I4 {
    }

    abstract static class A5 {
        public abstract void m();
    }

    interface I5 {
        default void m() {
        }
    }

    abstract static class B5 extends A5 implements I5 {
    }

    static class A6 {
        public void m() {
        }
    }

    abstract static class B6 extends A6 {
        public abstract void m();
    }

    interface A7 {
        void m();
    }

    interface B7 extends A7 {
        default void m() {
        }
    }

    interface C7 extends B7 {
        void m();
    }

    static class W {
        private void x() {
        }
    }

    static class WW extends W {
        public void x() {
        }
    }

    // =========================
    // additional fixtures
    // =========================

    static class S {
        public static void y() {
        }
    }

    static class SS extends S {
        public static void y() {
        }
    }

    static class PPParent {
        void z() {
        }
    }

    static class PPChild extends PPParent {
        @Override
        void z() {
        }
    }

}
