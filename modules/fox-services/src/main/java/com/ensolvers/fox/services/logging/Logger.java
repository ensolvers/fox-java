package com.ensolvers.fox.services.logging;


public class Logger {

    private static String customizeMsg( Object msg ) {
        return msg.toString();
    }

    public static void info( Object category, String msg ) {                info( category.getClass(), msg );                           }
    public static void info( Class category, String msg ) {
        org.slf4j.Logger c = getCategory( category );
        if ( c.isInfoEnabled() ) {
            c.info( customizeMsg( msg ) );
        }
    }
    public static void info( Object category, Object msg, Throwable e ) {   info( category.getClass(), msg, e );                        }
    public static void info( Class category, Object msg, Throwable e ) {
        org.slf4j.Logger c = getCategory( category );
        if ( c.isInfoEnabled() ) {
            c.info( customizeMsg( msg ), e );
        }
    }
    public static void info( Object category, Object msg ) {                info( category.getClass(), msg );                           }
    public static void info( Class category, Object msg ) {
        org.slf4j.Logger c = getCategory( category );
        if ( c.isInfoEnabled() ) {
            c.info( customizeMsg( msg ) );
        }
    }

    public static void debug( Object category, Object msg ) {               debug( category.getClass(), msg );                          }
    public static void debug( Class category, Object msg ) {
        org.slf4j.Logger c = getCategory( category );
        if ( c.isDebugEnabled() ) {
            c.debug( customizeMsg( msg ) );
        }
    }
    public static void debug( Object category, Object msg, Throwable e ) {  debug( category.getClass(), msg, e );                       }
    public static void debug( Class category, Object msg, Throwable e ) {
        org.slf4j.Logger c = getCategory( category );
        if ( c.isDebugEnabled() ) {
            c.debug( customizeMsg( msg ), e );
        }
    }
    public static void debug( Object category, String msg ) {               debug( category.getClass(), msg );                          }
    public static void debug( Class category, String msg ) {
        org.slf4j.Logger c = getCategory( category );
        if ( c.isDebugEnabled() ) {
            c.debug( customizeMsg( msg ) );
        }
    }

    public static void error( Object category, Object msg, Throwable e ) {  error( category.getClass(), msg, e );                       }
    public static void error( Class category, Object msg, Throwable e ) {   getCategory( category ).error( customizeMsg( msg ), e );                    }
    public static void error( Object category, Object msg ) {               error( category.getClass(), msg );                          }
    public static void error( Class category, Object msg ) {                getCategory( category ).error( customizeMsg( msg ) );                       }
    public static void error( Object category, String msg ) {               error( category.getClass(), msg );                          }
    public static void error( Class category, String msg ) {                getCategory( category ).error( msg );                       }

    public static void warn( Object category, Object msg ) {                warn( category.getClass(), msg );      }
    public static void warn( Class category, Object msg ) {                 getCategory( category ).warn( customizeMsg( msg ) );                        }
    public static void warn( Object category, Object msg, Throwable e ) {   warn( category.getClass(), msg, e );                        }
    public static void warn( Class category, Object msg, Throwable e ) {    getCategory( category ).warn( customizeMsg( msg ), e );                     }


    public static boolean isInfoEnabled( Object category ) {                return getCategory( category.getClass() ).isInfoEnabled();  }
    public static boolean isInfoEnabled( Class category ) {                 return getCategory( category ).isInfoEnabled();             }

    public static boolean isDebugEnabled( Object category ) {               return getCategory( category.getClass() ).isDebugEnabled(); }
    public static boolean isDebugEnabled( Class category ) {                return getCategory( category ).isDebugEnabled();            }

    public static void initInfo( Object o, String methodName ) {            initInfo( o.getClass(), methodName );                       }
    public static void initInfo( Class o, String methodName ) {             getCategory( o ).info( methodName + " [Inicia]" );          }

    public static void endInfo( Object o, String methodName ) {             endInfo( o.getClass(), methodName );                        }
    public static void endInfo( Class o, String methodName ) {              getCategory( o ).info( methodName + " [Finaliza]" );        }

    public static org.slf4j.Logger getCategory( Class clazz ) {             return org.slf4j.LoggerFactory.getLogger( clazz );          }
    public static org.slf4j.Logger getCategory( String clazz ) {            return org.slf4j.LoggerFactory.getLogger( clazz );          }

//    wip: explore a way to runtime set log level for a category
//    import org.slf4j.event.Level;
//    public static void setDebugLevel( Class clazz ) {                       ((ch.qos.logback.classic.Logger)getCategory( clazz )).setLevel( Level.DEBUG ); }
//    public static void setDebugLevel( String clazz ) {                      ((ch.qos.logback.classic.Logger)getCategory( clazz )).setLevel( Level.DEBUG ); }
//    public static void setInfoLevel( Class clazz ) {                        ((ch.qos.logback.classic.Logger)getCategory( clazz )).setLevel( Level.INFO );  }
//    public static void setInfoLevel( String clazz ) {                       ((ch.qos.logback.classic.Logger)getCategory( clazz )).setLevel( Level.INFO );  }
//    public static void setWarnLevel( Class clazz ) {                        ((ch.qos.logback.classic.Logger)getCategory( clazz )).setLevel( Level.WARN );  }
//    public static void setWarnLevel( String clazz ) {                       ((ch.qos.logback.classic.Logger)getCategory( clazz )).setLevel( Level.WARN );  }

}
