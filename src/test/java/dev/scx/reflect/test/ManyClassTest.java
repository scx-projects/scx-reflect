package dev.scx.reflect.test;

import dev.scx.reflect.ClassInfo;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static dev.scx.reflect.ScxReflect.typeOf;

public class ManyClassTest {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        test1();
    }

    public static void test1() throws IOException, ClassNotFoundException {
        var classList = findClassFromFileFolder(Path.of("C:\\Users\\scx\\.m2\\repository\\commons-io"));

        var i = 0;
        for (var loadedClass : classList) {
            try {
                var type = typeOf(loadedClass);
                if (type instanceof ClassInfo c) {
                    c.allSuperClasses();
                    c.allMethods();
                    c.allInterfaces();
                    c.allFields();
                    c.recordComponents();
                    c.recordConstructor();
                    c.defaultConstructor();
                }
                i = i + 1;
            } catch (NoClassDefFoundError | TypeNotPresentException _) {

            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        System.out.println("共加载类数量: " + i);
    }

    public static List<Class<?>> findClassFromFileFolder(Path fileFolder) throws IOException {
        var result = new ArrayList<Class<?>>();

        var list = Files.walk(fileFolder).filter(c -> c.toString().endsWith(".jar")).toList();

        var urls = list.stream().map(f -> {
            try {
                return f.toUri().toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }).toArray(URL[]::new);

        try (var classLoader = new URLClassLoader(urls, ManyClassTest.class.getClassLoader())) {
            for (var path : list) {
                try (var t = new JarFile(path.toFile())) {
                    List<JarEntry> jarEntries = t.stream().toList();
                    for (JarEntry jarEntry : jarEntries) {
                        if (jarEntry.getName().endsWith(".class")) {
                            var className = jarEntry.getName().replace('/', '.').replace(".class", "");
                            try {
                                var c = Class.forName(className, false, classLoader);
                                result.add(c);
                            } catch (Throwable e) {
                                // 跳过无法加载的类
                            }
                        }
                    }
                } catch (IOException ex) {
                    // 跳过无法加载的 jar
                }
            }
        }

        return result;
    }

}
