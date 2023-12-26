package org.bluo.ioc.content;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author boluo
 * @date 2023/12/25
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BeanDefinition {
    private String beanName;
    private Class<?> beanClass;
    private boolean isLazy;
    private String Scope;
}
