package org.bluo.springmvc;

import org.bluo.content.AnnotationConfigApplicationContext;
import org.bluo.springmvc.controller.OrderController;

/**
 * @author boluo
 * @date 2023/12/26
 */
public class Test {
    public static void main(String[] args) throws Exception {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Config.class);
        Object bean = context.getBean(OrderController.class);
        System.out.println(bean);
    }
}
