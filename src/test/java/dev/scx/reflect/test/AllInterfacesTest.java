package dev.scx.reflect.test;

import dev.scx.reflect.ClassInfo;
import dev.scx.reflect.ScxReflect;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/// allInterfaces / findSuperType 测试
///
/// 覆盖:
/// 1. 父类继承接口
/// 2. 直接接口 + 间接接口
/// 3. 接口继承接口
/// 4. 菱形接口去重
/// 5. findSuperType 对 自身 / 父类 / 接口 / 不存在目标 的行为
/// 6. 接口类型自身调用 allInterfaces
/// 7. 泛型接口绑定
/// 8. 父类传递的泛型接口绑定
/// 9. 多层泛型接口继承后的绑定
///
/// @author scx567888
public class AllInterfacesTest {

    public static void main(String[] args) {
        test1_basicInheritedInterface();
        test2_directAndIndirectInterfaces();
        test3_interfaceExtendsInterface();
        test4_diamondInterfacesShouldDeduplicate();
        test5_findSuperType_selfSuperclassAndInterface();
        test6_findSuperType_notFound();
        test7_interfaceType_allInterfaces();
        test8_classAndParentBothProvideInterfaces();
        test9_findSuperType_onInterfaceItself();
        test10_genericInterfaceBinding_direct();
        test11_genericInterfaceBinding_fromSuperclass();
        test12_genericInterfaceBinding_multiLevelInterfaceChain();
    }

    /// 基础场景:
    /// A -> B -> C(interface)
    ///
    /// 预期:
    /// - A.allInterfaces() 只有 C
    /// - A.findSuperType(C.class) 命中
    @Test
    public static void test1_basicInheritedInterface() {
        var typeInfo = typeOf(A.class);

        assertAllInterfaces(typeInfo, C.class);
        assertFindSuperType(typeInfo, C.class, C.class);
    }

    /// 当前类直接实现接口 + 父类间接实现接口
    ///
    /// 结构:
    /// D extends E implements F
    /// E implements G
    ///
    /// 预期:
    /// - D.allInterfaces() = {F, G}
    /// - 均可通过 findSuperType 找到
    @Test
    public static void test2_directAndIndirectInterfaces() {
        var typeInfo = typeOf(D.class);

        assertAllInterfaces(typeInfo, F.class, G.class);
        assertFindSuperType(typeInfo, F.class, F.class);
        assertFindSuperType(typeInfo, G.class, G.class);
    }

    /// 接口继承接口
    ///
    /// 结构:
    /// H implements I
    /// I extends J
    ///
    /// 预期:
    /// - H.allInterfaces() = {I, J}
    /// - findSuperType(I.class) / findSuperType(J.class) 均命中
    @Test
    public static void test3_interfaceExtendsInterface() {
        var typeInfo = typeOf(H.class);

        assertAllInterfaces(typeInfo, I.class, J.class);
        assertFindSuperType(typeInfo, I.class, I.class);
        assertFindSuperType(typeInfo, J.class, J.class);
    }

    /// 菱形接口继承去重
    ///
    /// 结构:
    /// K implements L, M
    /// L extends N
    /// M extends N
    ///
    /// 预期:
    /// - K.allInterfaces() = {L, M, N}
    /// - N 只能出现一次
    @Test
    public static void test4_diamondInterfacesShouldDeduplicate() {
        var typeInfo = typeOf(K.class);

        assertAllInterfaces(typeInfo, L.class, M.class, N.class);
        assertFindSuperType(typeInfo, N.class, N.class);

        var allInterfaces = typeInfo.allInterfaces();
        long nCount = Arrays.stream(allInterfaces)
            .filter(x -> x.rawClass() == N.class)
            .count();

        Assert.assertEquals(nCount, 1L);
    }

    /// findSuperType 同时覆盖:
    /// - 自身
    /// - 父类
    /// - 接口
    ///
    /// 结构:
    /// O extends P implements Q
    /// P implements R
    @Test
    public static void test5_findSuperType_selfSuperclassAndInterface() {
        var typeInfo = typeOf(O.class);

        assertFindSuperType(typeInfo, O.class, O.class);
        assertFindSuperType(typeInfo, P.class, P.class);
        assertFindSuperType(typeInfo, Q.class, Q.class);
        assertFindSuperType(typeInfo, R.class, R.class);
    }

    /// 不存在的父类型
    ///
    /// 预期:
    /// - 返回 null
    @Test
    public static void test6_findSuperType_notFound() {
        var typeInfo = typeOf(A.class);
        var superType = typeInfo.findSuperType(String.class);
        Assert.assertNull(superType);
    }

    /// 接口类型自身调用 allInterfaces
    ///
    /// 结构:
    /// T extends U, V
    /// U extends W
    /// V extends W
    ///
    /// 预期:
    /// - T.allInterfaces() = {U, V, W}
    /// - 不包含 T 自身
    @Test
    public static void test7_interfaceType_allInterfaces() {
        var typeInfo = typeOf(T.class);

        assertAllInterfaces(typeInfo, U.class, V.class, W.class);

        var containsSelf = Arrays.stream(typeInfo.allInterfaces())
            .anyMatch(x -> x.rawClass() == T.class);

        Assert.assertFalse(containsSelf);
    }

    /// 当前类和父类都提供接口来源
    ///
    /// 结构:
    /// X extends Y implements Z
    /// Y implements AA
    ///
    /// 预期:
    /// - X.allInterfaces() = {Z, AA}
    @Test
    public static void test8_classAndParentBothProvideInterfaces() {
        var typeInfo = typeOf(X.class);

        assertAllInterfaces(typeInfo, Z.class, AA.class);
        assertFindSuperType(typeInfo, Z.class, Z.class);
        assertFindSuperType(typeInfo, AA.class, AA.class);
    }

    /// 接口类型自身 findSuperType
    ///
    /// 预期:
    /// - 对自身查找返回自身
    /// - 对父接口查找返回父接口
    @Test
    public static void test9_findSuperType_onInterfaceItself() {
        var typeInfo = typeOf(T.class);

        assertFindSuperType(typeInfo, T.class, T.class);
        assertFindSuperType(typeInfo, U.class, U.class);
        assertFindSuperType(typeInfo, V.class, V.class);
        assertFindSuperType(typeInfo, W.class, W.class);
    }

    /// 直接实现泛型接口
    ///
    /// 结构:
    /// GG implements Comparable<String>
    ///
    /// 预期:
    /// - 可找到 Comparable
    /// - 其 bindings[0] 为 String
    @Test
    public static void test10_genericInterfaceBinding_direct() {
        var typeInfo = typeOf(GG.class);
        var superType = assertFindSuperType(typeInfo, Comparable.class, Comparable.class);

        assertBinding(superType, 0, String.class);
    }

    /// 父类传递泛型接口绑定
    ///
    /// 结构:
    /// HH<T> implements Comparable<T>
    /// II extends HH<Integer>
    ///
    /// 预期:
    /// - II.findSuperType(Comparable.class) 命中
    /// - 其 bindings[0] 为 Integer
    @Test
    public static void test11_genericInterfaceBinding_fromSuperclass() {
        var typeInfo = typeOf(II.class);
        var superType = assertFindSuperType(typeInfo, Comparable.class, Comparable.class);

        assertBinding(superType, 0, Integer.class);
    }

    /// 多层泛型接口继承
    ///
    /// 结构:
    /// JJ<T> extends KK<T>
    /// KK<T> extends LL<T>
    /// MM implements JJ<Long>
    ///
    /// 预期:
    /// - MM 可找到 JJ<Long>, KK<Long>, LL<Long>
    /// - 三者 bindings[0] 均为 Long
    @Test
    public static void test12_genericInterfaceBinding_multiLevelInterfaceChain() {
        var typeInfo = typeOf(MM.class);

        var jj = assertFindSuperType(typeInfo, JJ.class, JJ.class);
        var kk = assertFindSuperType(typeInfo, KK.class, KK.class);
        var ll = assertFindSuperType(typeInfo, LL.class, LL.class);

        assertBinding(jj, 0, Long.class);
        assertBinding(kk, 0, Long.class);
        assertBinding(ll, 0, Long.class);

        assertAllInterfaces(typeInfo, JJ.class, KK.class, LL.class);
    }

    // =========================
    // helper
    // =========================

    private static ClassInfo typeOf(Class<?> clazz) {
        return (ClassInfo) ScxReflect.typeOf(clazz);
    }

    private static void assertAllInterfaces(ClassInfo typeInfo, Class<?>... expectedInterfaces) {
        var actual = Arrays.stream(typeInfo.allInterfaces())
            .map(ClassInfo::rawClass)
            .collect(Collectors.toSet());

        var expected = Set.of(expectedInterfaces);

        Assert.assertEquals(
            actual,
            expected,
            typeInfo.rawClass().getName() + " 的 allInterfaces() 结果不符合预期"
        );
    }

    private static ClassInfo assertFindSuperType(ClassInfo typeInfo, Class<?> rawTarget, Class<?> expectedRawClass) {
        var result = typeInfo.findSuperType(rawTarget);
        Assert.assertNotNull(
            result,
            typeInfo.rawClass().getName() + " 查找 " + rawTarget.getName() + " 时不应为 null"
        );
        Assert.assertEquals(
            result.rawClass(),
            expectedRawClass,
            typeInfo.rawClass().getName() + " 查找 " + rawTarget.getName() + " 的结果 rawClass 不符合预期"
        );
        return result;
    }

    private static void assertBinding(ClassInfo classInfo, int index, Class<?> expectedRawClass) {
        var typeInfo = classInfo.bindings().get(index);
        Assert.assertNotNull(typeInfo, classInfo + " 的 bindings[" + index + "] 不应为 null");
        Assert.assertEquals(
            typeInfo.rawClass(),
            expectedRawClass,
            classInfo + " 的 bindings[" + index + "] 不符合预期"
        );
    }

    // =========================
    // fixtures
    // =========================

    public static class A extends B {
    }

    public static class B implements C {
    }

    public interface C {
    }

    public static class D extends E implements F {
    }

    public static class E implements G {
    }

    public interface F {
    }

    public interface G {
    }

    public static class H implements I {
    }

    public interface I extends J {
    }

    public interface J {
    }

    public static class K implements L, M {
    }

    public interface L extends N {
    }

    public interface M extends N {
    }

    public interface N {
    }

    public static class O extends P implements Q {
    }

    public static class P implements R {
    }

    public interface Q {
    }

    public interface R {
    }

    public interface T extends U, V {
    }

    public interface U extends W {
    }

    public interface V extends W {
    }

    public interface W {
    }

    public static class X extends Y implements Z {
    }

    public static class Y implements AA {
    }

    public interface Z {
    }

    public interface AA {
    }

    public static class GG implements Comparable<String> {
        @Override
        public int compareTo(String o) {
            return 0;
        }
    }

    public static class HH<T> implements Comparable<T> {
        @Override
        public int compareTo(T o) {
            return 0;
        }
    }

    public static class II extends HH<Integer> {
    }

    public interface JJ<T> extends KK<T> {
    }

    public interface KK<T> extends LL<T> {
    }

    public interface LL<T> {
    }

    public static class MM implements JJ<Long> {
    }

}
