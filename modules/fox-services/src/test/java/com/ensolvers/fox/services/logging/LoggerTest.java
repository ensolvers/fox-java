package com.ensolvers.fox.services.logging;

import org.junit.jupiter.api.Test;


public class LoggerTest {

    /**   show use of level up/downgrade     */
    @Test public void magicTest() {
        Logger.setInfoLevel( getClass() );
        Logger.debug( this, "you won't see this" );
        Logger.setDebugLevel( getClass() );
        Logger.debug( this, "Now you see me" );
    }

    /**   show use logging within instance and class context    */
    @Test public void clazzTest() {
        Logger.info( this, "this is me" );
        Logger.info( Logger.class, "Now I am something else" );
    }

    /**   show specific category creation and use   */
    @Test public void onTheFlyCategoryTest() {
        org.slf4j.Logger myLogger = Logger.getCategory( "ensolvers.especialCategory" );
        myLogger.debug( "Hey new category!");
    }

}
