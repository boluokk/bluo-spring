package org.bluo.springmvc.content;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;

/**
 * @author boluo
 * @date 2023/12/26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HandleInvocation {
    private String url;
    private Method method;
    private Object controller;
}
