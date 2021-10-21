package com.ensolvers.fox.spring.lightweightcontainer;

import com.ensolvers.fox.services.logging.Logger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Vector;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

//public class GlobalApplicationContext {
@Component
public class GlobalApplicationContext implements ApplicationContextAware {
    public ApplicationContext getApplicationContext() {
        return context;
    }

    public void setApplicationContext( ApplicationContext ac ) throws BeansException {
        context = ac;
    }

    private static ApplicationContext context;

    public static void setGlobalContext( ApplicationContext globalContext ) {
        Logger.info(GlobalApplicationContext.class, "setGlobalContext [" + globalContext + "]" );
        context = globalContext;
    }

    public static Object getBean( String name ) {
        if ( context == null ) {
            Logger.error( GlobalApplicationContext.class, "No Spring Context available" );
            throw new ApplicationContextException( "No Spring Context available" );
        }
        return getInstance().getBean(name);
    }

    public static ApplicationContext getInstance() {
        return context;
    }

    public static Collection<String> getContextsFromProperty( InputStream stream ) throws IOException {
        Vector contexts = new Vector();
        if ( stream != null ) {
            BufferedReader reader = new BufferedReader( new InputStreamReader( stream ) );
            String line;
            while ( (line = reader.readLine() ) != null ) {
                contexts.add( line );
                Logger.debug( GlobalApplicationContext.class, "context line read [" + line + "]" );
            }
        }

        return contexts;
    }

    public static Collection<String> getContextsFromProperty( String propertyClassPath ) throws IOException {
        InputStream stream = GlobalApplicationContext.class.getResourceAsStream( propertyClassPath );
        if ( stream == null ) {
            Logger.error( GlobalApplicationContext.class, "context property not found at ClassPath with path definided as [" + propertyClassPath + "]" );
            return new Vector();
        }
        return getContextsFromProperty( stream );
    }

    public static String[] applyDevelopContextFilter(String[] paths) throws ApplicationContextException {
        Vector<String> filtered = new Vector();
        try {
            for (String path : paths) {
                Logger.info(GlobalApplicationContext.class, "configLocations [" + path + "]");
                Resource[] resources = new PathMatchingResourcePatternResolver().getResources(path);
                Logger.debug(GlobalApplicationContext.class, "resources obtenidos segun locations [" + resources.length + "]");
                for (Resource resource : resources) {
                    Logger.info(GlobalApplicationContext.class, "subLocation from [" + path + "], [" + resource + "]");
                    if (resource.getFilename().toString().indexOf("develop") == -1) {
                        /* *
                         * ClassPathResource o FileSystemResource suported, both interfaces with getPath method
                         */
                        Method method = resource.getClass().getDeclaredMethod("getPath", null);
                        filtered.add("classpath*:" + method.invoke(resource, null));
                        Logger.info(GlobalApplicationContext.class, "context to apply " + resource);
                    } else {
                        Logger.info(GlobalApplicationContext.class, "context filtered " + resource);
                    }
                }
            }
        } catch(Exception e ) {
            throw new ApplicationContextException(e.getMessage(),e );
        }

        return filtered.toArray( new String[ filtered.size() ] );
    }

//    public static ApplicationContext build(List<String> list) {
//        setGlobalContext( new ClassPathXmlApplicationContext( applyDevelopContextFilter( list.toArray( new String[ list.size() ] ) ) ) );
//        return getInstance();
//    }
    public static ApplicationContext build( String config ) {
        setGlobalContext( new ClassPathXmlApplicationContext( config ) );
        return getInstance();
    }
}