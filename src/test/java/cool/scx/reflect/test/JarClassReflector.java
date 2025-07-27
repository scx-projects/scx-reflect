package cool.scx.reflect.test;



import cool.scx.reflect.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.jar.*;

import static cool.scx.reflect.ScxReflect.typeOf;

public class JarClassReflector {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        File jarFolder = new File("C:\\Users\\scx\\.m2\\repository\\org\\apache\\commons\\commons-collections4\\4.4"); // 替换为你的 jar 文件夹路径
//        File jarFolder = new File("C:\\Users\\scx\\.m2\\repository\\org\\graalvm\\"); // 替换为你的 jar 文件夹路径
        List<File> jarFiles = findJarFiles(jarFolder);

        List<Class<?>> loadedClasses = new ArrayList<>();
        
        URL[] urls = jarFiles.stream().map(f -> {
            try {
                return f.toURI().toURL();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).toArray(URL[]::new);

        try (URLClassLoader classLoader = new URLClassLoader(urls, JarClassReflector.class.getClassLoader())) {
            for (File jarFile : jarFiles) {
                loadedClasses.addAll(loadClassesFromJar(jarFile, classLoader));
            }
        }

        var i=0;
        var b=0;
        for (Class<?> loadedClass : loadedClasses) {
            try {
                TypeInfo type = typeOf(loadedClass);
                if (type instanceof ClassInfo c) {
                    c.allSuperClasses();
                    c.allMethods();
                    c.allInterfaces();
                    c.allFields();
                    c.recordComponents();
                    c.recordConstructor();
                    c.defaultConstructor();
                }
                i=i+1;
            }catch (NoClassDefFoundError e){
//                e.printStackTrace();
            }catch (TypeNotPresentException e){

            }catch (Throwable e){
                e.printStackTrace();
            }
        }
//        URLClassLoader  classLoader = new URLClassLoader(new URL[]{
//                new File("C:\\Users\\scx\\.m2\\repository\\com\\beust\\jcommander\\1.82\\jcommander-1.82.jar").toURI().toURL()
//        }, JarClassReflector.class.getClassLoader());
//
//        Class<?> aClass = Class.forName("com.beust.jcommander.converters.EnumConverter", false, classLoader);
//        var type =(ClassInfo) typeOf(aClass);
//        FieldInfo[] fields = type.fields();
//        var typeInfo =(ClassInfo) fields[1].fieldType();
//        boolean equals = typeInfo.equals(typeInfo);
        
        Callable<String> a= new Callable<String>() {

            @Override
            public String call() throws Exception {
                return "";
            }
        };

        TypeInfo type1 = typeOf(new TypeReference<List<String>>() {});
        TypeInfo type2 = typeOf(a.getClass());
//
//        System.out.println(type);


//        var iCountMapEntries = ScxCollections.groupingBy(TypeFactory.TYPE_CACHE.keySet(), (c) -> {
//            return c.getClass();
//        });


        List<TypeInfo> list = TypeFactory.TYPE_CACHE.values().stream().filter(c -> {
            if (c instanceof ClassInfo cc) {
                try {
                    List<MethodInfo> list1 = Arrays.stream(cc.allMethods()).filter(ds -> {
                        if (ds.isStatic()){
                            return false;
                        }else{
                            if (ds.accessModifier()==AccessModifier.PRIVATE){
                                return false;
                            }
                        }
                        return true;
                    }).toList();
                    var ccc = list1.stream().map(e -> e.toString()).distinct().count();
                    if (ccc != list1.size()) {
                        return true;
                    } else {
                        return false;
                    }
                } catch (Throwable e) {
                    if (e instanceof NoClassDefFoundError) {
                        return false;
                    }else if (e instanceof TypeNotPresentException){
                        return false;
                    }else {
                        throw new RuntimeException(e);
                    }
                }
            }
            return false;
        }).filter(c->((ClassInfo)c).classKind()!=ClassKind.INTERFACE).toList();


        System.out.println("共加载类数量: " + loadedClasses.size());
    }

    public static List<File> findJarFiles(File dir) {
        List<File> jars = new ArrayList<>();
        File[] files = dir.listFiles();
        if (files == null) return jars;

        for (File file : files) {
            if (file.isDirectory()) {
                jars.addAll(findJarFiles(file));
            } else if (file.getName().endsWith(".jar")) {
                jars.add(file);
            }
        }
        return jars;
    }

    public static List<Class<?>> loadClassesFromJar(File jarFile, ClassLoader classLoader) {
        List<Class<?>> classes = new ArrayList<>();
        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class") && !entry.getName().contains("$")) {
                    String className = entry.getName()
                            .replace('/', '.')
                            .replace(".class", "");
                    try {
                        Class<?> clazz = Class.forName(className, false, classLoader);
                        classes.add(clazz);
                    } catch (Throwable ignored) {
                        // 跳过无法加载的类（依赖不全、初始化失败等）
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("无法读取 jar: " + jarFile.getAbsolutePath());
        }
        return classes;
    }
}
