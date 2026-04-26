package utils;
import exceptions.config.ConfigException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {
    private static final Properties PROPERTIES=new Properties();
    private static volatile boolean loaded=false;

    public ConfigReader() {
    }
    //i will create a get method and by using the get method, i will fetch the data using key and value
    public static String get(String key) throws ConfigException {
       //we will ensure our file is loaded/read or not?
        ensureLoaded();
        //we will fetch the value/read the value from the file
        String value=PROPERTIES.getProperty(key);
        if (value==null){
            throw new ConfigException("value of key is null: key is :"+key);
        }
        return value.trim();
    }
    public static String get(String key,String defaultValue) throws ConfigException {
        //we will ensure our file is loaded/read or not?
        ensureLoaded();
        //we will fetch the value/read the value from the file
        String value=PROPERTIES.getProperty(key);
        return (value==null)?defaultValue:value.trim();
    }

    private static void ensureLoaded() throws ConfigException {
        if (!loaded){
            load();
        }
    }

    public static void load() throws ConfigException {
        if (!loaded){
            //only one thread can execute the critical section at a time.
            synchronized (ConfigReader.class){
                if (!loaded){
                    String resourcePath="config/global.properties";
                    try(InputStream is= ConfigReader.class.getClassLoader().getResourceAsStream(resourcePath)){
                        if (is==null){
                            throw new ConfigException("file not found in the given location");
                        }
                        //load into properties
                        //gets the key/value pair from the file; then the flag marks success
                        PROPERTIES.load(is);
                        loaded=true;

                    } catch (IOException e) {
                        throw new ConfigException("IO exceptions e :"+e);
                    }
                }
            }
        }
    }



}
