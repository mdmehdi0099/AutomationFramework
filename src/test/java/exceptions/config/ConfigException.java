package exceptions.config;

import exceptions.base.CheckedFrameworkException;

public class ConfigException extends CheckedFrameworkException {

    public ConfigException(String message) {
        super(message);
    }

    public ConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
