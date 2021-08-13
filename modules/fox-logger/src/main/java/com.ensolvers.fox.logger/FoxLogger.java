/* Copyright (c) 2021 Ensolvers
 * All Rights Reserved
 *
 * The contents of this file is dual-licensed under 2 alternative Open Source/Free licenses: LGPL 2.1 or later and
 * Apache License 2.0. (starting with JNA version 4.0.0).
 *
 * You can freely decide which license you want to apply to the project.
 *
 * You may obtain a copy of the LGPL License at: http://www.gnu.org/licenses/licenses.html
 *
 * A copy is also included in the downloadable source code package
 * containing JNA, in file "LGPL2.1".
 *
 * You may obtain a copy of the Apache License at: http://www.apache.org/licenses/
 *
 * A copy is also included in the downloadable source code package
 * containing JNA, in file "AL2.0".
 */

package com.ensolvers.fox.logger;

import com.newrelic.api.agent.NewRelic;
import java.util.Collections;
import org.apache.log4j.Category;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.slf4j.helpers.MessageFormatter;

/** Provides utilities for adding logging with New Relic support. */
public class FoxLogger extends Category {

    private static final String INFO =
            "[\u001b[1;34m %-5p \u001b[m] \u001b[0;37m%d{yyyy-MM-dd HH:mm:ss}\u001b[m \u001b[0;36m%c{3}\u001b[m - %m%n";
    private static final String ERROR =
            "[\u001b[1;31m %-5p \u001b[m] \u001b[0;37m%d{yyyy-MM-dd HH:mm:ss}\u001b[m \u001b[0;36m%c{3}\u001b[m - %m%n";
    private static final String DEBUG =
            "[\u001b[1;32m %-5p \u001b[m] \u001b[0;37m%d{yyyy-MM-dd HH:mm:ss}\u001b[m \u001b[0;36m%c{3}\u001b[m - %m%n";
    private static final String WARN =
            "[\u001b[1;35m %-5p \u001b[m] \u001b[0;37m%d{yyyy-MM-dd HH:mm:ss}\u001b[m \u001b[0;36m%c{3}\u001b[m - %m%n";
    private static final String FATAL =
            "[\u001b[1;31m %-5p \u001b[m] \u001b[0;37m%d{yyyy-MM-dd HH:mm:ss}\u001b[m \u001b[0;36m%c{3}\u001b[m - %m%n";

    private Logger logger;

    public static FoxLogger getLogger(String name) {
        return new FoxLogger(LogManager.getLogger(name));
    }

    public FoxLogger(Logger logger) {
        super(logger.getName());
        this.logger = logger;
    }

    private void changeConversionPattern(String pattern) {
        ((PatternLayout) logger.getParent().getAppender("stdout").getLayout())
                .setConversionPattern(pattern);
    }

    @Override
    public void info(Object message) {
        this.changeConversionPattern(INFO);
        this.logger.info(message);
    }

    @Override
    public void error(Object message) {
        NewRelic.noticeError(String.valueOf(message));
        this.changeConversionPattern(ERROR);
        this.logger.error(message);
    }

    public void error(String message) {
        NewRelic.noticeError(message);
        this.changeConversionPattern(ERROR);
        this.logger.error(message);
    }

    // @Override
    public void error(String message, Throwable t) {
        NewRelic.noticeError(t, Collections.singletonMap("message", message));
        this.changeConversionPattern(ERROR);
        this.logger.error(message, t);
    }

    public void error(String message, boolean doNewRelicLog) {
        if (doNewRelicLog) {
            NewRelic.noticeError(message);
        }
        this.changeConversionPattern(ERROR);
        this.logger.error(message);
    }

    public void error(Throwable t, boolean doNewRelicLog) {
        if (doNewRelicLog) {
            NewRelic.noticeError(t);
        }
        this.changeConversionPattern(ERROR);
        this.logger.error("UNHANDLED EXCEPTION", t);
    }

    @Override
    public void warn(Object message) {
        this.changeConversionPattern(WARN);
        this.logger.warn(message);
    }

    @Override
    public void warn(Object message, Throwable t) {
        this.changeConversionPattern(WARN);
        this.logger.warn(message, t);
    }

    @Override
    public void debug(Object message) {
        this.changeConversionPattern(DEBUG);
        this.logger.debug(message);
    }

    @Override
    public void fatal(Object message) {
        this.changeConversionPattern(FATAL);
        this.logger.fatal(message);
    }

    public void finfo(String format, Object... args) {
        this.info(MessageFormatter.arrayFormat(format, args).getMessage());
    }

    public void ferror(String format, Throwable t, Object... args) {
        this.error(MessageFormatter.arrayFormat(format, args).getMessage(), t);
    }

    public void ferrorWithoutException(String format, Object... args) {
        this.error(MessageFormatter.arrayFormat(format, args).getMessage());
    }

    public void fwarn(String format, Object... args) {
        this.warn(MessageFormatter.arrayFormat(format, args).getMessage());
    }
}