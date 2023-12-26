package org.bluo.ioc.content;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.bluo.ioc.annotation.Component;
import org.bluo.ioc.annotation.ComponentScan;
import org.bluo.ioc.annotation.Scope;
import org.bluo.ioc.bean.BeanPostProcessor;
import org.bluo.ioc.bean.InitializingBean;
import org.bluo.ioc.exception.BaseException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author boluo
 * @date 2023/12/25
 */
@Slf4j
public class AnnotationConfigApplicationContext {
    private Class<?> configClass;
    private String packageBaseName;
    private ConcurrentHashMap<String, BeanDefinition> beanDefinitions = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<>();
    private List<File> classFiles = new ArrayList<>();

    public AnnotationConfigApplicationContext(Class<?> configClass) {
        this.configClass = configClass;
        // 扫描
        doScan(configClass);
        // 注册
        doRegister();
        // 实例化
        initializeBean();
    }

    private void initializeBean() {
        // 实例化
        beanDefinitions.forEach((beanName, beanDefinition) -> {
            if (!beanDefinition.isLazy() &&
                    beanDefinition.getScope().equals(ScopeType.SINGLETON)) {
                try {
                    Object o = beanDefinition.getBeanClass().newInstance();
                    singletonObjects.put(beanName, o);
                } catch (InstantiationException | IllegalAccessException e) {
                    log.error("实例化失败", e);
                }
            }
        });
        // 初始化前
        singletonObjects.forEach((beanName, bean) -> {
            if (bean instanceof BeanPostProcessor) {
                try {
                    Object o = ((BeanPostProcessor) bean).postProcessBeforeInitialization(bean, beanName);
                    if (ObjectUtil.isNotNull(o)) {
                        bean = o;
                    }
                } catch (Exception e) {
                    log.error("初始化前异常", e);
                }
            }
        });
        // 初始化
        singletonObjects.forEach((beanName, bean) -> {
            if (bean instanceof InitializingBean) {
                try {
                    ((InitializingBean) bean).afterPropertiesSet();
                } catch (Exception e) {
                    log.error("初始化异常", e);
                }
            }
        });
        // 初始化后
        singletonObjects.forEach((beanName, bean) -> {
            if (bean instanceof BeanPostProcessor) {
                try {
                    Object o = ((BeanPostProcessor) bean).postProcessAfterInitialization(bean, beanName);
                    if (ObjectUtil.isNotNull(o)) {
                        bean = o;
                    }
                } catch (Exception e) {
                    log.error("初始化前异常", e);
                }
            }
        });
    }

    private void doRegister() {
        for (File file : classFiles) {
            String fileName = file.getName();
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (fileName.endsWith(".class")) {
                String className = packageBaseName + "." + fileName.substring(0, fileName.lastIndexOf("."));
                try {
                    Class<?> beanClass = classLoader.loadClass(className);
                    if (beanClass.isAnnotationPresent(Component.class)) {
                        BeanDefinition beanDefinition = new BeanDefinition();
                        if (ObjectUtil.isNotNull(beanClass.getAnnotation(Scope.class))) {
                            Scope scope = beanClass.getAnnotation(Scope.class);
                            beanDefinition.setScope(scope.value());
                        } else {
                            beanDefinition.setScope("singleton");
                        }
                        beanDefinition.setBeanClass(beanClass);
                        beanDefinition.setBeanName(beanClass.getName());
                        System.out.println(beanDefinition);
                        beanDefinitions.put(beanClass.getName(), beanDefinition);
                    }
                } catch (Exception e) {
                    log.error("加载类失败：" + className);
                }
            }
        }
    }

    private void doScan(Class<?> configClass) {
        boolean annotationPresent = configClass.isAnnotationPresent(ComponentScan.class);
        ComponentScan componentScan = configClass.getDeclaredAnnotation(ComponentScan.class);
        // 使用注解扫描
        if (annotationPresent && componentScan.value().length() > 0) {
            packageBaseName = componentScan.value();
        } else {
            int i = configClass.getName().lastIndexOf(".");
            packageBaseName = configClass.getName().substring(0, i);
        }
        loadClasses(packageBaseName);
    }

    public void loadClasses(String packageBaseName) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageBaseName.replace(".", "/");
        File file = new File(classLoader.getResource(path).getFile());
        if (file.exists()) {
            File[] files = file.listFiles();
            if (ObjectUtil.isNotNull(files)) {
                for (File f : files) {
                    if (f.isFile()) {
                        classFiles.add(f);
                    }
                }
            }
        }
    }

    public Object getBean(String beanName) {
        // 是否是单例
        BeanDefinition beanDefinition = beanDefinitions.get(beanName);
        if (ObjectUtil.isNull(beanDefinition)) {
            throw new BaseException("没有找到bean：" + beanName);
        }
        if (beanDefinition.getScope().equals(ScopeType.SINGLETON)) {
            return singletonObjects.get(beanName);
        } else if (beanDefinition.getScope().equals(ScopeType.PROTOTYPE)) {
            try {
                return beanDefinition.getBeanClass().newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                log.error("实例化失败：" + beanName);
            }
        }
        return null;
    }
}
