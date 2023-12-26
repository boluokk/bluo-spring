package org.bluo.test;

import lombok.Data;
import org.bluo.annotation.Autowired;
import org.bluo.annotation.Component;
import org.bluo.annotation.Scope;
import org.bluo.bean.BeanPostProcessor;

/**
 * @author boluo
 * @date 2023/12/25
 */
@Component
@Scope
@Data
public class OrderService implements BeanPostProcessor {

    @Autowired("menuService")
    MenuService menuService;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws Exception {
        return null;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws Exception {
        return null;
    }
}
