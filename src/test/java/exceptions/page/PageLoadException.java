package exceptions.page;

import exceptions.base.FrameworkException;

public class PageLoadException extends FrameworkException {

    public PageLoadException(String message, Throwable cause) {
        super(message, cause);
    }

}
