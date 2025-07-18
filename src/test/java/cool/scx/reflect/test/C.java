package cool.scx.reflect.test;

import cool.scx.reflect.ClassInfoImpl;
import cool.scx.reflect.ScxReflect;
import cool.scx.reflect.TypeInfo;
import cool.scx.reflect.TypeResolutionContext;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static cool.scx.reflect.TypeBindingsImpl.EMPTY_BINDINGS;

public class C {
    public static class Node<T extends Node<T>> {
    }

    public static void main(String[] args) {
        Type type = Node.class.getTypeParameters()[0].getBounds()[0];
        var s=new ClassInfoImpl((ParameterizedType) type,new TypeResolutionContext(EMPTY_BINDINGS));
        var s1=new ClassInfoImpl((ParameterizedType) type,new TypeResolutionContext(EMPTY_BINDINGS));
//        s.equals(s1);
        TypeInfo type1 = ScxReflect.getType(type);
        TypeInfo type2 = ScxReflect.getType(type);
    }
}
