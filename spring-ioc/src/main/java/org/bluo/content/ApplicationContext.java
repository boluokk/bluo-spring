package org.bluo.content;

/**
 * @author boluo
 * @date 2023/12/26
 */
public interface ApplicationContext {
    Object getBean(Class clazz) throws Exception;

    Object getBean(String beanName) throws Exception;
}
