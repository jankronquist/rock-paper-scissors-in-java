package com.jayway.es.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionUtil {
	public static final String HANDLE_METHOD = "handle";

	@SuppressWarnings("unchecked")
	public static <R> R invokeHandleMethod(Object target, Object param) {
		try {
			Method method = target.getClass().getMethod(ReflectionUtil.HANDLE_METHOD, param.getClass());
			return (R) method.invoke(target, param);
		} catch (InvocationTargetException e) {
			throw Sneak.sneakyThrow(e.getTargetException());
		} catch (Exception e) {
			throw Sneak.sneakyThrow(e);
		}
	}
	
	public static <P> P project(P target, Object param) {
		invokeHandleMethod(target, param);
		return target;
	}

}
