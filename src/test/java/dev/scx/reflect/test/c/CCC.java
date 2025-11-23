package dev.scx.reflect.test.c;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.Map;

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
    @AC()
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

        // 泛型字段，带泛型数组
        public K[] arrayOfK;

        // 泛型字段，嵌套泛型
        public Map<V, List<W>> nestedMap;

        // 泛型字段，使用通配符
        public List<? super V> listOfSuperV;

        // 泛型方法，带复杂参数和返回值
        public Map<W, List<? extends K>> complexMethod(V param, List<? extends W> list) {
            return null;
        }

    }

}
