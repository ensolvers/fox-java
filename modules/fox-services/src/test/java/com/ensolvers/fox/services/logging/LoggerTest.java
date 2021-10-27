package com.ensolvers.fox.services.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;


public class LoggerTest {

    /**   shows use of level up/downgrade     */
    @Test public void magicTest() {
        Logger.initInfo( this, "magicTest" );

        Logger.setInfoLevel( getClass() );
        Logger.debug( this, "you won't see this" );
        Logger.setDebugLevel( getClass() );
        Logger.debug( this, "Now you see me" );

        Logger.endInfo( this, "magicTest" );
    }

    /**   shows use logging within instance and class context    */
    @Test public void clazzTest() {
        Logger.initInfo( this, "clazzTest" );

        Logger.info( this, "this is me" );
        Logger.info( Math.class, "Now I am something else" );

        Logger.endInfo( this, "clazzTest" );
    }

    /**   shows specific category creation and use   */
    @Test public void onTheFlyCategoryTest() {
        Logger.initInfo( this, "onTheFlyCategoryTest" );

        org.slf4j.Logger myLogger = Logger.getCategory( "ensolvers.especialCategory" );
        myLogger.debug( "Hey new category!");

        Logger.endInfo( this, "onTheFlyCategoryTest" );
    }

    /**   shows collateral cost of logging   */
    @Test public void expensiveLoggingTest() {
        Logger.initInfo( this, "expensiveLoggingTest" );

        ExpensiveToLog witness = new ExpensiveToLog();
        assertEquals("no work done", witness.status);
        Logger.info( this, witness.recurseIntensiveLog() );
        assertEquals("hard work done", witness.status);

        Logger.endInfo( this, "expensiveLoggingTest" );
    }

    /**   shows deferred logging, no resources consumed if log level is inactive  */
    @Test public void deferredLoggingTest() {
        Logger.initInfo( this, "deferredLoggingTest" );

        ExpensiveToLog witness = new ExpensiveToLog();

        Logger.setInfoLevel( this.getClass() );          // ensure logging level is high
        assertFalse( Logger.isDebugEnabled( this ) );

        Logger.debug( this, () -> witness.recurseIntensiveLog() );
        assertEquals("no work done", witness.status);

        Logger.setDebugLevel( this.getClass() );         // ensure logging level is low
        assertTrue( Logger.isDebugEnabled( this ) );

        Logger.debug( this, () -> witness.recurseIntensiveLog() );
        assertEquals("hard work done", witness.status);

        Logger.endInfo( this, "deferredLoggingTest" );
    }

    class ExpensiveToLog {
        String status = "no work done";
        String recurseIntensiveLog() {
            status = "hard work done";
            return "resource intensive log";
        }
    }

}
