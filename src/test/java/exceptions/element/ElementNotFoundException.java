package exceptions.element;

import exceptions.base.FrameworkException;

public class ElementNotFoundException extends FrameworkException {

    public ElementNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
