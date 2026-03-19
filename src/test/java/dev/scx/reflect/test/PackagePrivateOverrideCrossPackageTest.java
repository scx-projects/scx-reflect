package dev.scx.reflect.test;

import dev.scx.reflect.ClassInfo;
import dev.scx.reflect.ScxReflect;
import dev.scx.reflect.test.p2.PackagePrivateSub;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class PackagePrivateOverrideCrossPackageTest {

    public static void main(String[] args) {
        test1_crossPackagePackagePrivateShouldNotOverride();
    }

    @Test
    public static void test1_crossPackagePackagePrivateShouldNotOverride() {
        var type = (ClassInfo) ScxReflect.typeOf(PackagePrivateSub.class);
        var methods = Arrays.stream(type.allMethods()).filter(m -> m.name().equals("x")).toList();

        Assert.assertEquals(methods.size(), 2);

        var actual = methods.stream().map(m -> m.declaringClass().rawClass().getSimpleName()).collect(Collectors.toSet());
        Assert.assertEquals(actual, Set.of("PackagePrivateBase", "PackagePrivateSub"));
    }

}
