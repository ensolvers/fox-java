package com.ensolvers.fox.services.logging;

import ch.qos.logback.classic.Level;

/**
 * This service class logger avoids the need to structurally declare a Logger
 * instance in every client class definition.
 * <p>
 * Logs in the right category when logging within a polymorphic hierarchy.
 * <p>
 * <p>
 * <p>
 * Information to obtain the right category is obtained from the instance class.
 * <p>
 * Within a static context ClassName.class() should be used.
 * <p>
 * <p>
 * <p>
 * Accessing info and debug categories is efficient and logging attempt is
 * avoided when inactive
 * <p>
 * <p>
 * <p>
 * <code>debug(...)</code>, <code>info(...)</code>, <code>warn(...)</code> and
 * <code>error(...)</code> methods families provide dynamic access to according
 * level categories with or without exception as parameter
 * <p>
 * <p>
 * <p>
 * <code>is[debug|info]Enabled</code> allows testing on level activation
 * <p>
 * <p>
 * <p>
 * <code>set[debug|info|warn|error]Level( category )</code> allows to set log
 * level for a <code>category</code>
 *
 * <p>
 */
public class Logger {

	private static String customizeMsg(Object msg) {
		return msg.toString();
	}

	public static <L> void info(Object category, CodeBlock fn) {
		info(category.getClass(), fn);
	}

	public static <L> void info(Class category, CodeBlock fn) {
		org.slf4j.Logger c = getCategory(category);
		if (c.isInfoEnabled()) {
			c.info(fn.value());
		}
	}

	public static void info(Object category, String msg) {
		info(category.getClass(), msg);
	}

	public static void info(Class category, String msg) {
		org.slf4j.Logger c = getCategory(category);
		if (c.isInfoEnabled()) {
			c.info(customizeMsg(msg));
		}
	}

	public static void info(Object category, Object msg, Throwable e) {
		info(category.getClass(), msg, e);
	}

	public static void info(Class category, Object msg, Throwable e) {
		org.slf4j.Logger c = getCategory(category);
		if (c.isInfoEnabled()) {
			c.info(customizeMsg(msg), e);
		}
	}

	public static void info(Object category, Object msg) {
		info(category.getClass(), msg);
	}

	public static void info(Class category, Object msg) {
		org.slf4j.Logger c = getCategory(category);
		if (c.isInfoEnabled()) {
			c.info(customizeMsg(msg));
		}
	}

	public static <L> void debug(Object category, CodeBlock fn) {
		debug(category.getClass(), fn);
	}

	public static <L> void debug(Class category, CodeBlock fn) {
		org.slf4j.Logger c = getCategory(category);
		if (c.isDebugEnabled()) {
			c.debug(customizeMsg(fn.value()));
		}
	}

	public static void debug(Object category, Object msg) {
		debug(category.getClass(), msg);
	}

	public static void debug(Class category, Object msg) {
		org.slf4j.Logger c = getCategory(category);
		if (c.isDebugEnabled()) {
			c.debug(customizeMsg(msg));
		}
	}

	public static void debug(Object category, Object msg, Throwable e) {
		debug(category.getClass(), msg, e);
	}

	public static void debug(Class category, Object msg, Throwable e) {
		org.slf4j.Logger c = getCategory(category);
		if (c.isDebugEnabled()) {
			c.debug(customizeMsg(msg), e);
		}
	}

	public static void debug(Object category, String msg) {
		debug(category.getClass(), msg);
	}

	public static void debug(Class category, String msg) {
		org.slf4j.Logger c = getCategory(category);
		if (c.isDebugEnabled()) {
			c.debug(customizeMsg(msg));
		}
	}

	public static void error(Object category, Object msg, Throwable e) {
		error(category.getClass(), msg, e);
	}

	public static void error(Class category, Object msg, Throwable e) {
		getCategory(category).error(customizeMsg(msg), e);
	}

	public static void error(Object category, Object msg) {
		error(category.getClass(), msg);
	}

	public static void error(Class category, Object msg) {
		getCategory(category).error(customizeMsg(msg));
	}

	public static void error(Object category, String msg) {
		error(category.getClass(), msg);
	}

	public static void error(Class category, String msg) {
		getCategory(category).error(msg);
	}

	public static void warn(Object category, Object msg) {
		warn(category.getClass(), msg);
	}

	public static void warn(Class category, Object msg) {
		getCategory(category).warn(customizeMsg(msg));
	}

	public static void warn(Object category, Object msg, Throwable e) {
		warn(category.getClass(), msg, e);
	}

	public static void warn(Class category, Object msg, Throwable e) {
		getCategory(category).warn(customizeMsg(msg), e);
	}

	public static boolean isInfoEnabled(Object category) {
		return getCategory(category.getClass()).isInfoEnabled();
	}

	public static boolean isInfoEnabled(Class category) {
		return getCategory(category).isInfoEnabled();
	}

	public static boolean isDebugEnabled(Object category) {
		return getCategory(category.getClass()).isDebugEnabled();
	}

	public static boolean isDebugEnabled(Class category) {
		return getCategory(category).isDebugEnabled();
	}

	public static void initInfo(Object o, String methodName) {
		initInfo(o.getClass(), methodName);
	}

	public static void initInfo(Class o, String methodName) {
		getCategory(o).info(methodName + " [Init]");
	}

	public static void endInfo(Object o, String methodName) {
		endInfo(o.getClass(), methodName);
	}

	public static void endInfo(Class o, String methodName) {
		getCategory(o).info(methodName + " [End]");
	}

	public static org.slf4j.Logger getCategory(Class clazz) {
		return org.slf4j.LoggerFactory.getLogger(clazz);
	}

	public static org.slf4j.Logger getCategory(String clazz) {
		return org.slf4j.LoggerFactory.getLogger(clazz);
	}

	public static void setDebugLevel(Class clazz) {
		((ch.qos.logback.classic.Logger) getCategory(clazz)).setLevel(Level.DEBUG);
	}

	public static void setDebugLevel(String clazz) {
		((ch.qos.logback.classic.Logger) getCategory(clazz)).setLevel(Level.DEBUG);
	}

	public static void setInfoLevel(Class clazz) {
		((ch.qos.logback.classic.Logger) getCategory(clazz)).setLevel(Level.INFO);
	}

	public static void setInfoLevel(String clazz) {
		((ch.qos.logback.classic.Logger) getCategory(clazz)).setLevel(Level.INFO);
	}

	public static void setWarnLevel(Class clazz) {
		((ch.qos.logback.classic.Logger) getCategory(clazz)).setLevel(Level.WARN);
	}

	public static void setWarnLevel(String clazz) {
		((ch.qos.logback.classic.Logger) getCategory(clazz)).setLevel(Level.WARN);
	}
}

interface CodeBlock {
	String value();
}
