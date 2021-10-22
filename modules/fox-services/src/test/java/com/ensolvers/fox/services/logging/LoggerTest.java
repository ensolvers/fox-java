package com.ensolvers.fox.services.logging;

import org.junit.jupiter.api.Test;


public class LoggerTest {

    @Test
    public void magicTest() {
        Logger.setInfoLevel( getClass() );
        Logger.debug( this, "you won't see this" );
        Logger.setDebugLevel( getClass() );
        Logger.debug( this, "Now you see me" );
    }


    @Test
    public void clazzTest() {
        Logger.info( this, "this is me" );
        Logger.info( Logger.class, "Now I am something else" );
    }

}
