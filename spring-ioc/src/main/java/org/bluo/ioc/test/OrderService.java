package org.bluo.ioc.test;

import org.bluo.ioc.annotation.Component;
import org.bluo.ioc.annotation.Scope;
import org.bluo.ioc.bean.BeanPostProcessor;

/**
 * @author boluo
 * @date 2023/12/25
 */
@Component
@Scope
public class OrderService implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws Exception {
        System.out.println("pre");
        return null;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws Exception {
        return null;
    }
}
