package com.trading.miniioc.container;

import com.trading.miniioc.annotation.Component;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 【職責】掃描指定套件（含子套件），找出所有標註 {@link Component} 的類別。
 * 【技巧】ClassLoader {@code getResources} → file／jar 兩路徑；{@code Class.forName} + {@code isAnnotationPresent}。
 * 【概念】這正是 Spring {@code @ComponentScan} 的最小核心：把「套件名」變成「路徑」再載入 class。手刻一次後，就懂為什麼掃描有成本、為什麼要限定 basePackage。
 * 【邊界】不建立實例、不做注入；只回傳候選 Class 列表。無法載入的類別略過。
 */
public final class ClasspathScanner {

    private ClasspathScanner() {
    }

    /**
     * 【職責】回傳 {@code basePackage} 下所有 {@code @Component} 類別。
     * 【技巧】套件點號轉斜線路徑；依 URL protocol 分流目錄掃描與 jar 掃描。
     * 【概念】「自動發現」取代手動 {@code register}——呼叫端只給套件名，容器自己找元件，這是 IoC 易用性的關鍵一步。
     * @param basePackage 掃描起點套件（如 {@code com.trading.miniioc.demo}）
     * @return 標註 {@link Component} 的類別列表（可能為空）
     */
    public static List<Class<?>> findComponents(String basePackage) {
        List<Class<?>> result = new ArrayList<>();
        String path = basePackage.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Enumeration<URL> resources = classLoader.getResources(path);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                if ("file".equals(resource.getProtocol())) {
                    File directory = new File(URLDecoder.decode(resource.getFile(), StandardCharsets.UTF_8));
                    scanDirectory(directory, basePackage, result);
                } else if ("jar".equals(resource.getProtocol())) {
                    scanJar(resource, path, result);
                }
            }
        } catch (IOException e) {
            throw new BeanException("掃描套件失敗：" + basePackage, e);
        }
        return result;
    }

    private static void scanDirectory(File directory, String packageName, List<Class<?>> result) {
        if (!directory.exists()) {
            return;
        }
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(file, packageName + "." + file.getName(), result);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                addIfComponent(className, result);
            }
        }
    }

    private static void scanJar(URL resource, String path, List<Class<?>> result) throws IOException {
        String jarPath = resource.getPath().substring("file:".length(), resource.getPath().indexOf("!"));
        try (JarFile jarFile = new JarFile(URLDecoder.decode(jarPath, StandardCharsets.UTF_8))) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.startsWith(path) && name.endsWith(".class")) {
                    String className = name.substring(0, name.length() - 6).replace('/', '.');
                    addIfComponent(className, result);
                }
            }
        }
    }

    private static void addIfComponent(String className, List<Class<?>> result) {
        try {
            Class<?> clazz = Class.forName(className);
            if (clazz.isAnnotationPresent(Component.class)) {
                result.add(clazz);
            }
        } catch (Throwable ignored) {
            // 無法載入（例如缺少相依）的類別直接略過，不影響其他元件掃描。
        }
    }
}
