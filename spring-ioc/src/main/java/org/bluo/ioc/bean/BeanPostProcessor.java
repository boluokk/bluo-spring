package org.bluo.ioc.bean;

/**
 * @author boluo
 * @date 2023/12/25
 */
public interface BeanPostProcessor {
    Object postProcessBeforeInitialization(Object bean, String beanName) throws Exception;

    Object postProcessAfterInitialization(Object bean, String beanName) throws Exception;
}
