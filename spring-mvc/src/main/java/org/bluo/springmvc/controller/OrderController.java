package org.bluo.springmvc.controller;

import org.bluo.annotation.Component;
import org.bluo.springmvc.annotation.RequestMapping;

/**
 * @author boluo
 * @date 2023/12/26
 */
@Component
@RequestMapping("/order")
public class OrderController {

    @RequestMapping("/test")
    public String orderService(String param1, String param2) {
        System.out.println("param1:" + param1);
        System.out.println("param2:" + param2);
        return "order";
    }
}
