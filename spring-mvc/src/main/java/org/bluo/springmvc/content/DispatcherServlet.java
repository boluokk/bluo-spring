package org.bluo.springmvc.content;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.bluo.content.AnnotationConfigApplicationContext;
import org.bluo.springmvc.annotation.RequestMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author boluo
 * @date 2023/12/26
 */
@Slf4j
public class DispatcherServlet extends HttpServlet {
    private AnnotationConfigApplicationContext configApplicationContext;
    private ConcurrentHashMap<String, HandleInvocation> handleInvocationMap = new ConcurrentHashMap<>();

    @Override
    public void init() throws ServletException {
        String packagePath = this.getServletConfig().getInitParameter("springPath");
        configApplicationContext = new AnnotationConfigApplicationContext(packagePath);
        initHandlerMapping();
    }

    private void initHandlerMapping() {
        List<Object> beans = configApplicationContext.getAllBean();
        for (Object bean : beans) {
            Class<?> beanClass = bean.getClass();
            if (beanClass.isAnnotationPresent(RequestMapping.class)) {
                String preUrl = beanClass.getAnnotation(RequestMapping.class).value();
                Method[] methods = beanClass.getMethods();
                for (Method method : methods) {
                    if (method.isAnnotationPresent(RequestMapping.class)) {
                        String methodUrl = method.getAnnotation(RequestMapping.class).value();
                        String realUrl = preUrl + methodUrl;
                        handleInvocationMap.put(realUrl, new HandleInvocation(realUrl, method, bean));
                    }
                }
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String requestURI = req.getRequestURI();
        HandleInvocation handleInvocation = handleInvocationMap.get(requestURI);
        if (ObjectUtil.isNotNull(handleInvocation)) {
            Method method = handleInvocation.getMethod();
            try {
                Class<?>[] parameterTypes = method.getParameterTypes();
                Object[] params = new Object[parameterTypes.length];
                int index = 0;
                for (int i = 0; i < parameterTypes.length; i++) {
                    if (parameterTypes[i].isAssignableFrom(HttpServletRequest.class)) {
                        params[i] = req;
                    } else if (parameterTypes[i].isAssignableFrom(HttpServletResponse.class)) {
                        params[i] = req;
                    } else {
                        String paramName = "param" + index++; // 假设参数名为 param0, param1, ...
                        String[] parameterValues = req.getParameterValues(paramName);
                        if (parameterValues != null && parameterValues.length > 0) {
                            params[i] = convertParameterValue(parameterTypes[i], parameterValues[0]);
                        } else {
                            params[i] = getDefaultParameterValue(parameterTypes[i]);
                        }
                    }
                }

                method.invoke(handleInvocation.getController(), params);
            } catch (Exception e) {
                log.error("invoke error", e);
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            PrintWriter writer = resp.getWriter();
            writer.write("404 not found!!");
            writer.flush();
            writer.close();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    public Object parseBaseType(Class<?> parameterTypes, Object value) {
        if (parameterTypes.isAssignableFrom(String.class)) {
            return value.toString();
        } else if (parameterTypes.isAssignableFrom(Integer.class)) {
            return Integer.parseInt(value.toString());
        } else if (parameterTypes.isAssignableFrom(Long.class)) {
            return Long.parseLong(value.toString());
        } else if (parameterTypes.isAssignableFrom(Double.class)) {
            return Double.parseDouble(value.toString());
        } else if (parameterTypes.isAssignableFrom(Float.class)) {
            return Float.parseFloat(value.toString());
        } else if (parameterTypes.isAssignableFrom(Boolean.class)) {
            return Boolean.parseBoolean(value.toString());
        } else {
            return value;
        }
    }

    private Object getDefaultParameterValue(Class<?> parameterType) {
        if (parameterType.equals(Integer.class) || parameterType.equals(int.class)) {
            return 0; // 默认为0
        } else if (parameterType.equals(Boolean.class) || parameterType.equals(boolean.class)) {
            return false; // 默认为false
        } else if (parameterType.equals(Double.class) || parameterType.equals(double.class)) {
            return 0.0; // 默认为0.0
        } else if (parameterType.equals(Float.class) || parameterType.equals(float.class)) {
            return 0.0f; // 默认为0.0f
        } else if (parameterType.equals(Long.class) || parameterType.equals(long.class)) {
            return 0L; // 默认为0L
        } else if (parameterType.equals(Short.class) || parameterType.equals(short.class)) {
            return (short) 0; // 默认为0
        } else if (parameterType.equals(Byte.class) || parameterType.equals(byte.class)) {
            return (byte) 0; // 默认为0
        } else if (parameterType.equals(Character.class) || parameterType.equals(char.class)) {
            return '\u0000'; // 默认为空字符
        }
        return null;
    }

    private Object convertParameterValue(Class<?> parameterType, String parameterValue) {
        if (parameterType.equals(Integer.class) || parameterType.equals(int.class)) {
            return Integer.parseInt(parameterValue);
        } else if (parameterType.equals(Boolean.class) || parameterType.equals(boolean.class)) {
            return Boolean.parseBoolean(parameterValue);
        } else if (parameterType.equals(Double.class) || parameterType.equals(double.class)) {
            return Double.parseDouble(parameterValue);
        } else if (parameterType.equals(Float.class) || parameterType.equals(float.class)) {
            return Float.parseFloat(parameterValue);
        } else if (parameterType.equals(Long.class) || parameterType.equals(long.class)) {
            return Long.parseLong(parameterValue);
        } else if (parameterType.equals(Short.class) || parameterType.equals(short.class)) {
            return Short.parseShort(parameterValue);
        } else if (parameterType.equals(Byte.class) || parameterType.equals(byte.class)) {
            return Byte.parseByte(parameterValue);
        } else if (parameterType.equals(Character.class) || parameterType.equals(char.class)) {
            return parameterValue.charAt(0);
        } else if (parameterType.equals(String.class)) {
            return parameterValue;
        }
        return null;
    }
}
