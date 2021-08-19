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

package com.ensolvers.fox.logging;

import com.newrelic.api.agent.NewRelic;
import java.util.Collections;
import org.apache.log4j.Category;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.slf4j.helpers.MessageFormatter;

/** Provides utilities for adding logging with New Relic support. */
public class FoxLogger extends Category {
    private Logger logger;

    public static FoxLogger getLogger(String name) {
        return new FoxLogger(LogManager.getLogger(name));
    }

    public FoxLogger(Logger logger) {
        super(logger.getName());
        this.logger = logger;
    }

    @Override
    public void info(Object message) {
        this.logger.info(message);
    }

    @Override
    public void error(Object message) {
        NewRelic.noticeError(String.valueOf(message));
        this.logger.error(message);
    }

    public void error(String message) {
        NewRelic.noticeError(message);
        this.logger.error(message);
    }

    public void error(String message, Throwable t) {
        NewRelic.noticeError(t, Collections.singletonMap("message", message));
        this.logger.error(message, t);
    }

    public void error(String message, boolean doNewRelicLog) {
        if (doNewRelicLog) {
            NewRelic.noticeError(message);
        }
        this.logger.error(message);
    }

    public void error(Throwable t, boolean doNewRelicLog) {
        if (doNewRelicLog) {
            NewRelic.noticeError(t);
        }
        this.logger.error("UNHANDLED EXCEPTION", t);
    }

    @Override
    public void warn(Object message) {
        this.logger.warn(message);
    }

    @Override
    public void warn(Object message, Throwable t) {
        this.logger.warn(message, t);
    }

    @Override
    public void debug(Object message) {
        this.logger.debug(message);
    }

    @Override
    public void fatal(Object message) {
        this.logger.fatal(message);
    }

    /**
     * Log a message at different levels according to the specified format and argument.
     *
     * @param format   like "This {} is {} a text"
     * @param args like "first word, another word"
     */

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