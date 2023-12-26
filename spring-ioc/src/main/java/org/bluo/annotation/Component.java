package org.bluo.annotation;

import java.lang.annotation.*;

/**
 * @author boluo
 * @date 2023/12/25
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Component {
    String value() default "";
}
