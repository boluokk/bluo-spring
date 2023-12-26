package org.bluo.test;

import org.bluo.content.AnnotationConfigApplicationContext;

/**
 * @author boluo
 * @date 2023/12/25
 */
public class Main {
    public static void main(String[] args) throws Exception {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        Object orderService = context.getBean(OrderService.class);
        System.out.println(orderService);
    }
}
