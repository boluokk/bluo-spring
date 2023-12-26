package org.bluo.ioc.test;

import org.bluo.ioc.content.AnnotationConfigApplicationContext;

/**
 * @author boluo
 * @date 2023/12/25
 */
public class Main {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
    }
}
