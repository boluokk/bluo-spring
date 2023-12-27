package org.bluo.content;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.bluo.annotation.Autowired;
import org.bluo.annotation.Component;
import org.bluo.annotation.ComponentScan;
import org.bluo.annotation.Scope;
import org.bluo.bean.BeanPostProcessor;
import org.bluo.bean.InitializingBean;
import org.bluo.exception.BaseException;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author boluo
 * @date 2023/12/25
 */
@Slf4j
public class AnnotationConfigApplicationContext implements ApplicationContext {
    private Class<?> configClass;
    private String packageBaseName;
    private ConcurrentHashMap<String, BeanDefinition> beanDefinitions = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<>();
    private List<File> classFiles = new ArrayList<>();

    public AnnotationConfigApplicationContext(Class<?> configClass) {
        this.configClass = configClass;
        this.refresh();
    }

    public AnnotationConfigApplicationContext(String packageBaseName) {
        this();
        try {
            this.configClass = Thread.currentThread().getContextClassLoader().loadClass(packageBaseName);
            this.refresh();
        } catch (ClassNotFoundException e) {
            log.error("包不存在");
        }
    }

    public AnnotationConfigApplicationContext() {
    }

    public void refresh() {
        // 扫描
        doScan(configClass);
        // 注册
        doRegister();
        // 实例化
        initializeBean();
    }


    private void initializeBean() {
        // 实例化
        createBean();
    }

    private Object createBean() {
        for (BeanDefinition value : beanDefinitions.values()) {
            String className = value.getBeanClass().getName();
            Object bean = getBean(className);
            if (ObjectUtil.isNull(bean)) {
                doCreateBean(className, value);
            }
        }
        return null;
    }

    private Object doCreateBean(String className, BeanDefinition beanDefinition) {
        if (!beanDefinition.isLazy() && beanDefinition.getScope().equals(ScopeType.SINGLETON)) {
            try {
                // 实例化
                Object o = beanDefinition.getBeanClass().newInstance();
                // 填充属性
                populateProperties(o, beanDefinition);
                // 初始化
                initializing(o, beanDefinition);
                singletonObjects.put(className, o);
                return o;
            } catch (InstantiationException | IllegalAccessException e) {
                log.error("实例化失败", e);
            }
        }
        return null;
    }

    private void initializing(Object bean, BeanDefinition beanDefinition) {
        // 初始化前
        if (bean instanceof BeanPostProcessor) {
            try {
                ((BeanPostProcessor) bean).postProcessBeforeInitialization(bean, beanDefinition.getBeanName());
            } catch (Exception e) {
                log.error("初始化前置处理失败", e);
            }
        }
        // 初始化
        if (bean instanceof InitializingBean) {
            try {
                ((InitializingBean) bean).afterPropertiesSet();
            } catch (Exception e) {
                log.error("初始化失败", e);
            }
        }
        // 初始化后
        if (bean instanceof BeanPostProcessor) {
            try {
                ((BeanPostProcessor) bean).postProcessAfterInitialization(bean, beanDefinition.getBeanName());
            } catch (Exception e) {
                log.error("初始化后置处理失败", e);
            }
        }
    }

    private void populateProperties(Object bean, BeanDefinition beandifinition) {
        Class<?> beanClass = beandifinition.getBeanClass();
        Field[] fields = beanClass.getDeclaredFields();
        for (Field field : fields) {
            try {
                // 先根据名称 + 然后根据类型
                Object value = null;
                if (field.isAnnotationPresent(Autowired.class)) {
                    Autowired annotation = field.getAnnotation(Autowired.class);
                    for (BeanDefinition beanDefinition : beanDefinitions.values()) {
                        if (beanDefinition.getBeanName().equals(annotation.value())) {
                            value = getBean(beanDefinition.getBeanClass());
                            break;
                        }
                    }
                }
                if (ObjectUtil.isNull(value)) {
                    value = getBean(field.getType());
                }

                field.setAccessible(true);
                field.set(bean, value);
            } catch (Exception e) {
                log.error("注入属性失败", e);
            }
        }
    }

    private void doRegister() {
        for (File file : classFiles) {
            String filePath = file.getAbsolutePath().replace("\\", ".");
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (filePath.endsWith(".class")) {
                String className = filePath.substring(filePath.indexOf(packageBaseName), filePath.lastIndexOf("."));
                try {
                    Class<?> beanClass = classLoader.loadClass(className);
                    if (!beanClass.isAnnotation() && beanClass.isAnnotationPresent(Component.class)) {
                        BeanDefinition beanDefinition = new BeanDefinition();
                        Component component = beanClass.getAnnotation(Component.class);
                        if (ObjectUtil.isNotNull(beanClass.getAnnotation(Scope.class))) {
                            Scope scope = beanClass.getAnnotation(Scope.class);
                            beanDefinition.setScope(scope.value());
                        } else {
                            beanDefinition.setScope("singleton");
                        }
                        beanDefinition.setBeanClass(beanClass);
                        if (component.value().length() > 0) {
                            beanDefinition.setBeanName(component.value());
                        } else {
                            int length = beanClass.getName().length();
                            int start = beanClass.getName().lastIndexOf(".");
                            String name = beanClass.getName().substring(start + 1, length);
                            beanDefinition.setBeanName(name);
                        }
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
        processDirectory(file);
    }

    private void processDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (ObjectUtil.isNotNull(files)) {
                for (File f : files) {
                    if (f.isFile()) {
                        classFiles.add(f);
                    } else if (f.isDirectory()) {
                        processDirectory(f); // 递归处理子目录
                    }
                }
            }
        }
    }

    @Override
    public Object getBean(Class clazz) throws Exception {
        return getBean(clazz.getName());
    }

    public Object getBean(String className) {
        // 是否是单例
        BeanDefinition beanDefinition = beanDefinitions.get(className);
        if (ObjectUtil.isNull(beanDefinition)) {
            throw new BaseException("没有找到bean：" + className);
        }
        if (beanDefinition.getScope().equals(ScopeType.SINGLETON)) {
            return singletonObjects.get(className);
        } else if (beanDefinition.getScope().equals(ScopeType.PROTOTYPE)) {
            try {
                return beanDefinition.getBeanClass().newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                log.error("实例化失败：" + className);
            }
        }
        return null;
    }

    public List<Object> getAllBean() {
        return new ArrayList<>(singletonObjects.values());
    }
}