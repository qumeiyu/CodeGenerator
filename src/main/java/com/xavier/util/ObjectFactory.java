package com.xavier.util;

import com.xavier.exception.AppRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ObjectFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(ObjectFactory.class);

	private static List<ClassLoader> externalClassLoaders;

	static {
		externalClassLoaders = new ArrayList<>();
	}

	private ObjectFactory() {
		super();
	}

	public static synchronized void addExternalClassLoader(ClassLoader classLoader) {
		ObjectFactory.externalClassLoaders.add(classLoader);
	}

	@SuppressWarnings("rawtypes")
	public static Class externalClassForName(String type) throws ClassNotFoundException {

		Class clazz;

		for (ClassLoader classLoader : externalClassLoaders) {
			try {
				classLoader.loadClass(type);
				clazz = Class.forName(type, true, classLoader);
				return clazz;
			} catch (ClassNotFoundException e) {
				LOGGER.info(e.getMessage(), e);
			}
		}

		return internalClassForName(type);
	}

	public static Class<?> internalClassForName(String type) throws ClassNotFoundException {
		Class<?> clazz = null;

		try {
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			clazz = Class.forName(type, true, cl);
		} catch (ClassNotFoundException e) {
			LOGGER.info(e.getMessage(), e);
		}

		if (clazz == null) {
			clazz = Class.forName(type, true, ObjectFactory.class.getClassLoader());
		}

		return clazz;
	}

	public static Object createExternalObject(String type) {
		Object answer;

		try {
			Class<?> clazz = externalClassForName(type);
			answer = clazz.newInstance();
		} catch (Exception e) {
			throw new AppRuntimeException("类型转换出错:" + type, e);
		}

		return answer;
	}

}
