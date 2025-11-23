package dev.scx.reflect.test.c;

public interface GGG {

    GGG aa(GGG p);

    default GGG[] bb(GGG[] p1) {
        return p1;
    }

}
