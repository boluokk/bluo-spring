package org.bluo.ioc.bean;

/**
 * @author boluo
 * @date 2023/12/25
 */
public interface InitializingBean {
    void afterPropertiesSet() throws Exception;
}
